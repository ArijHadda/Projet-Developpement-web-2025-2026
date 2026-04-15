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
import java.util.Arrays;
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
        // CORRECTION 1: Ajout de "10KM" pour l'imageName dans le constructeur
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

        // CORRECTION 1: Ajout de "10KM" pour l'imageName
        Badge badge = new Badge(2L, BadgeAttributionService.BADGE_PREMIER_10KM, "10KM");

        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
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
        // CORRECTION 1: Constructeur valide
        Badge badgeExistant = new Badge(100L, BadgeAttributionService.BADGE_PREMIER_10KM, "10KM");

        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));

        // CORRECTION 2: Remplacement de l'ancien appel existsBy... qui n'existe plus dans le Service
        Activite activite = new Activite();
        activite.setDistance(15.0);
        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of(activite));
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(0L);

        when(badgeRepository.findByEntitule(BadgeAttributionService.BADGE_PREMIER_10KM)).thenReturn(Optional.of(badgeExistant));

        List<String> badgesAttribues = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertEquals(1, badgesAttribues.size());
        assertTrue(utilisateur.getBadges().contains(badgeExistant));
        verify(badgeRepository, never()).save(any(Badge.class));
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void attribuerBadgesAutomatiques_MusculationBadge() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of()); // Pas de distance
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(600L); // 10h = 600 min

        Badge badgeMuscu = new Badge(3L, BadgeAttributionService.BADGE_10H_MUSCULATION, "10H");
        when(badgeRepository.findByEntitule(BadgeAttributionService.BADGE_10H_MUSCULATION)).thenReturn(Optional.of(badgeMuscu));

        List<String> badges = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);
        assertEquals(1, badges.size());
        assertEquals(BadgeAttributionService.BADGE_10H_MUSCULATION, badges.get(0));
    }

    @Test
    void attribuerBadgesAutomatiques_DureeMusculationNull_NePlantePas() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of());
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(null); // La DB peut retourner null si vide

        List<String> badges = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertTrue(badges.isEmpty());
    }

    @Test
    void calculerDistanceTotaleEnKm_NormaliseLesGrandesDistances() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));

        Activite activite = new Activite();
        activite.setDistance(10000.0); // > 500, le code va le diviser par 1000 -> 10.0 km

        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(List.of(activite));
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(0L);

        Badge badge = new Badge(1L, "10km", "10KM");
        when(badgeRepository.findByEntitule("10km")).thenReturn(Optional.of(badge));

        List<String> badges = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertEquals(1, badges.size());
        assertEquals("10km", badges.get(0));
    }

    @Test
    void calculerDistanceTotaleEnKm_IgnoreActivitesNullesOuNegatives() {
        when(utilisateurRepository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));

        Activite act1 = null;
        Activite act2 = new Activite();
        act2.setDistance(0.0);
        Activite act3 = new Activite();
        act3.setDistance(-5.0);

        when(activiteRepository.findByUtilisateurId(USER_ID)).thenReturn(Arrays.asList(act1, act2, act3));
        when(activiteRepository.calculerDureeMusculation(USER_ID)).thenReturn(0L);

        List<String> badges = badgeAttributionService.attribuerBadgesAutomatiques(USER_ID);

        assertTrue(badges.isEmpty());
    }

    @Test
    void attribuerBadgeObjectifComplet_AttribueBadge() {
        Badge badgeObj = new Badge(BadgeAttributionService.BADGE_OBJECTIF_COMPLETE, "OBJECTIF");
        when(badgeRepository.findByEntitule(BadgeAttributionService.BADGE_OBJECTIF_COMPLETE)).thenReturn(Optional.of(badgeObj));

        List<String> badges = badgeAttributionService.attribuerBadgeObjectifComplet(utilisateur);

        assertEquals(1, badges.size());
        assertEquals(BadgeAttributionService.BADGE_OBJECTIF_COMPLETE, badges.get(0));
        assertTrue(utilisateur.getBadges().contains(badgeObj));
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void attribuerBadgeChallengeGagne_AttribueBadge() {
        Badge badgeChal = new Badge(BadgeAttributionService.BADGE_CHALLENGE_GAGNE, "CHALLENGE");
        when(badgeRepository.findByEntitule(BadgeAttributionService.BADGE_CHALLENGE_GAGNE)).thenReturn(Optional.of(badgeChal));

        List<String> badges = badgeAttributionService.attribuerBadgeChallengeGagne(utilisateur);

        assertEquals(1, badges.size());
        assertEquals(BadgeAttributionService.BADGE_CHALLENGE_GAGNE, badges.get(0));
        assertTrue(utilisateur.getBadges().contains(badgeChal));
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void getAllBadges_RetourneListe() {
        when(badgeRepository.findAll()).thenReturn(List.of(new Badge(), new Badge()));
        List<Badge> badges = badgeAttributionService.getAllBadges();
        assertEquals(2, badges.size());
        verify(badgeRepository).findAll();
    }
}