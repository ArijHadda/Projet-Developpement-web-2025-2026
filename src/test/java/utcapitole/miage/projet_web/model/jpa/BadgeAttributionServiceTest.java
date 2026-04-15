package utcapitole.miage.projet_web.model.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Badge;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeAttributionServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ActiviteRepository activiteRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private BadgeAttributionService badgeAttributionService;

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(USER_ID);
        utilisateur.setBadges(new ArrayList<>());
    }

    @Test
    void attribuerBadgesAutomatiquesAttribuePremier10Km() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        Activite activite = new Activite();
        activite.setDistance(10.0);
        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of(activite));
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(0L);
        when(badgeRepository.findByEntitule(BadgeAttributionService.BADGE_PREMIER_10KM)).thenReturn(Optional.empty());
        when(badgeRepository.save(any(Badge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> badgesAttribues = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertEquals(1, badgesAttribues.size());
        assertEquals(BadgeAttributionService.BADGE_PREMIER_10KM, badgesAttribues.get(0));
        assertEquals(1, utilisateur.getBadges().size());
        assertEquals(BadgeAttributionService.BADGE_PREMIER_10KM, utilisateur.getBadges().get(0).getEntitule());
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void attribuerBadgesAutomatiquesNAttribuePasSiPalierNonAtteint() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        Activite activite = new Activite();
        activite.setDistance(9.9);
        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of(activite));
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(0L);

        List<String> badgesAttribues = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertTrue(badgesAttribues.isEmpty());
        assertTrue(utilisateur.getBadges().isEmpty());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void attribuerBadgesAutomatiquesNAttribuePasDeuxFoisLeMemeBadge() {
        Badge dejaAttribue = new Badge(1L, BadgeAttributionService.BADGE_PREMIER_10KM, "10KM");
        utilisateur.setBadges(new ArrayList<>(List.of(dejaAttribue)));

        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        Activite activite = new Activite();
        activite.setDistance(10.0);
        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of(activite));
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(0L);

        List<String> badgesAttribues = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertTrue(badgesAttribues.isEmpty());
        assertEquals(1, utilisateur.getBadges().size());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void enregistrerActiviteEtAttribuerBadgesSauvegardeActiviteEtRetourneBadges() {
        Activite activite = new Activite();
        activite.setNom("Course");
        activite.setDistance(10.2);

        Badge badge = new Badge(2L, BadgeAttributionService.BADGE_PREMIER_10KM, "10KM");

        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        when(activiteRepository.save(activite)).thenReturn(activite);
        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of(activite));
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(0L);
        when(badgeRepository.findByEntitule(BadgeAttributionService.BADGE_PREMIER_10KM)).thenReturn(Optional.of(badge));

        List<String> badgesAttribues = badgeAttributionService.enregistrerActiviteEtAttribuerBadges(USER_ID, activite);

        assertEquals(1, badgesAttribues.size());
        assertEquals(BadgeAttributionService.BADGE_PREMIER_10KM, badgesAttribues.get(0));
        verify(activiteRepository).save(activite);
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void testEnregistrerActiviteEtAttribuerBadgesUtilisateurIntrouvable() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.empty());
        Activite activite = new Activite();
        assertThrows(IllegalArgumentException.class, () ->
                badgeAttributionService.enregistrerActiviteEtAttribuerBadges(USER_ID, activite)
        );
    }

    @Test
    void testAttribuerBadgesAutomatiquesUtilisateurIntrouvable() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                badgeAttributionService.attribuerBadgesAutomatiques(USER_ID)
        );
    }

    @Test
    void attribuerBadgesAutomatiquesUtiliseBadgeExistantEnBdd() {
        Badge badgeExistant = new Badge(100L, BadgeAttributionService.BADGE_PREMIER_10KM);

        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        when(activiteRepository.existsByUtilisateurIdAndDistanceGreaterThanEqual(USER_ID, 10.0)).thenReturn(true);
        when(badgeRepository.findByEntitule(BadgeAttributionService.BADGE_PREMIER_10KM)).thenReturn(Optional.of(badgeExistant));

        List<String> badgesAttribues = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertEquals(1, badgesAttribues.size());
        assertTrue(utilisateur.getBadges().contains(badgeExistant));
        verify(badgeRepository, never()).save(any(Badge.class));
        verify(utilisateurRepository).save(utilisateur);
    }
}
