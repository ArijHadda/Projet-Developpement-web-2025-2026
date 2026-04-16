package utcapitole.miage.projet_web.model.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utcapitole.miage.projet_web.dto.ClassementDTO;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Participation;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private ActiviteRepository activiteRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private BadgeAttributionService badgeAttributionService; // <-- Ajout du mock manquant

    @InjectMocks
    private ChallengeService challengeService;

    private Challenge mockChallenge;
    private Utilisateur mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new Utilisateur();
        mockUser.setId(1L);
        mockUser.setPrenom("Jean");
        mockUser.setNom("Dupont");

        mockChallenge = new Challenge();
        mockChallenge.setId(10L);
        mockChallenge.setTitre("Ancien Titre");
        mockChallenge.setCreateur(mockUser);
        mockChallenge.setParticipations(new ArrayList<>());
    }

    @Test
    void testGetClassementSuccess() {
        Participation p = new Participation();
        p.setUtilisateur(mockUser);
        mockChallenge.getParticipations().add(p);

        // Date de fin dans le futur pour ne pas déclencher le badge de fin
        mockChallenge.setDateFin(LocalDate.now().plusDays(5));

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        when(activiteRepository.calculerCaloriesPourChallenge(anyLong(), any(), any(), any())).thenReturn(500);

        List<ClassementDTO> result = challengeService.getClassement(10L);

        assertEquals(1, result.size());
        assertEquals("Jean Dupont", result.get(0).getNomComplet());
        assertEquals(500, result.get(0).getTotalCalories());

        // Le challenge n'est pas fini, on ne doit pas attribuer de badge
        verify(badgeAttributionService, never()).attribuerBadgeChallengeGagne(any());
    }

    @Test
    void testGetClassement_ChallengeFini_AttribueBadgeGagnant() {
        Participation p = new Participation();
        p.setUtilisateur(mockUser);
        mockChallenge.getParticipations().add(p);

        // Le challenge est terminé (dateFin dans le passé)
        mockChallenge.setDateFin(LocalDate.now().minusDays(1));

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        when(activiteRepository.calculerCaloriesPourChallenge(anyLong(), any(), any(), any())).thenReturn(500);

        List<ClassementDTO> result = challengeService.getClassement(10L);

        assertEquals(1, result.size());

        // Le challenge est fini et l'utilisateur est 1er, il doit recevoir le badge
        verify(badgeAttributionService).attribuerBadgeChallengeGagne(mockUser);
    }

    @Test
    void testGetClassement_ChallengeFini_WinnerExplicitlyMatches() {
        // Test covering line 170-176 loop and match
        Utilisateur winner = new Utilisateur();
        winner.setId(2L);
        winner.setPrenom("Alice");
        winner.setNom("Wonderland");

        Participation p1 = new Participation();
        p1.setUtilisateur(mockUser); // Jean Dupont

        Participation p2 = new Participation();
        p2.setUtilisateur(winner); // Alice Wonderland

        mockChallenge.getParticipations().add(p1);
        mockChallenge.getParticipations().add(p2);
        mockChallenge.setDateFin(LocalDate.now().minusDays(1));

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        // Mock Alice as winner (600 cal) and Jean as loser (500 cal)
        when(activiteRepository.calculerCaloriesPourChallenge(1L, null, null, mockChallenge.getDateFin())).thenReturn(500);
        when(activiteRepository.calculerCaloriesPourChallenge(2L, null, null, mockChallenge.getDateFin())).thenReturn(600);

        List<ClassementDTO> result = challengeService.getClassement(10L);

        assertEquals(2, result.size());
        assertEquals("Alice Wonderland", result.get(0).getNomComplet());
        
        // Ensure winner got the badge
        verify(badgeAttributionService).attribuerBadgeChallengeGagne(winner);
        verify(badgeAttributionService, never()).attribuerBadgeChallengeGagne(mockUser);
    }

    @Test
    void testGetClassement_ChallengeFini_NoParticipants_NoBadge() {
        // Test covering line 167 branch (!classement.isEmpty() == false)
        mockChallenge.setDateFin(LocalDate.now().minusDays(1));
        mockChallenge.setParticipations(new ArrayList<>()); // empty

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));

        List<ClassementDTO> result = challengeService.getClassement(10L);

        assertTrue(result.isEmpty());
        verify(badgeAttributionService, never()).attribuerBadgeChallengeGagne(any());
    }

    @Test
    void testGetClassementChallengeIntrouvable() {
        when(challengeRepository.findById(99L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> challengeService.getClassement(99L));
        assertEquals("Challenge introuvable", ex.getMessage());
    }

    @Test
    void testGetAllChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        challenges.add(mockChallenge);
        when(challengeRepository.findAll()).thenReturn(challenges);

        List<Challenge> result = challengeService.getAllChallenges();

        assertEquals(1, result.size());
        verify(challengeRepository).findAll();
    }

    @Test
    void testCreerChallenge() {
        when(challengeRepository.save(any(Challenge.class))).thenReturn(mockChallenge);

        Challenge nouveauChallenge = new Challenge();
        Challenge result = challengeService.creerChallenge(nouveauChallenge, mockUser);

        assertEquals(mockChallenge, result);

        assertEquals(mockUser, nouveauChallenge.getCreateur());
        verify(challengeRepository).save(nouveauChallenge);
    }

    @Test
    void testCreerChallenge_DateFinPassee_ThrowsException() {
        Challenge challenge = new Challenge();
        challenge.setDateFin(LocalDate.now().minusDays(1));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> challengeService.creerChallenge(challenge, mockUser));
        assertEquals("La date de fin du challenge ne peut pas être dans le passé.", ex.getMessage());
    }

    @Test
    void testCreerChallenge_DateDebutApresDateFin_ThrowsException() {
        Challenge challenge = new Challenge();
        challenge.setDateDebut(LocalDate.now().plusDays(2));
        challenge.setDateFin(LocalDate.now().plusDays(1));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> challengeService.creerChallenge(challenge, mockUser));
        assertEquals("La date de début ne peut pas être après la date de fin.", ex.getMessage());
    }

    @Test
    void testRejoindreChallengeSuccess() {
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        when(participationRepository.existsByUtilisateurAndChallenge(mockUser, mockChallenge)).thenReturn(false);

        assertDoesNotThrow(() -> challengeService.rejoindreChallenge(10L, mockUser));
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    void testRejoindreChallenge_ChallengeFini_ThrowsException() {
        mockChallenge.setDateFin(LocalDate.now().minusDays(1));
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));

        Exception ex = assertThrows(IllegalStateException.class, () -> challengeService.rejoindreChallenge(10L, mockUser));
        assertEquals("Ce challenge est déjà terminé. Vous ne pouvez plus le rejoindre.", ex.getMessage());
    }

    @Test
    void testRejoindreChallengeAlreadyJoinedShouldThrow() {
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        when(participationRepository.existsByUtilisateurAndChallenge(mockUser, mockChallenge)).thenReturn(true);

        Exception ex = assertThrows(IllegalStateException.class, () -> challengeService.rejoindreChallenge(10L, mockUser));
        assertEquals("Vous participez déjà à ce challenge !", ex.getMessage());
    }

    @Test
    void testSupprimerChallengeSuccess() {
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));

        assertDoesNotThrow(() -> challengeService.supprimerChallenge(10L, mockUser));
        verify(challengeRepository).delete(mockChallenge);
    }

    @Test
    void testSupprimerChallengeNonAuthorise() {
        Utilisateur otherUser = new Utilisateur();
        otherUser.setId(2L);
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));

        Exception ex = assertThrows(IllegalStateException.class, () -> challengeService.supprimerChallenge(10L, otherUser));
        assertTrue(ex.getMessage().contains("Non autorisé à supprimer ce challenge."));
        verify(challengeRepository, never()).delete(any(Challenge.class));
    }

    @Test
    void testModifierTitreChallengeSuccess() {
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));

        assertDoesNotThrow(() -> challengeService.modifierTitreChallenge(10L, "Nouveau Titre", mockUser));

        assertEquals("Nouveau Titre", mockChallenge.getTitre());
        verify(challengeRepository).save(mockChallenge);
    }

    @Test
    void testModifierTitreChallengeNonAuthorise() {
        Utilisateur otherUser = new Utilisateur();
        otherUser.setId(2L);
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));

        Exception ex = assertThrows(IllegalStateException.class, () -> challengeService.modifierTitreChallenge(10L, "Nouveau Titre", otherUser));
        assertTrue(ex.getMessage().contains("Non autorisé à modifier ce challenge."));
        verify(challengeRepository, never()).save(any(Challenge.class));
    }
}