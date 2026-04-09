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
        mockChallenge.setCreateur(mockUser);
        mockChallenge.setParticipations(new ArrayList<>());
    }

    @Test
    void testGetClassement() {
        Participation p = new Participation();
        p.setUtilisateur(mockUser);
        mockChallenge.getParticipations().add(p);

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        when(activiteRepository.calculerCaloriesPourChallenge(anyLong(), any(), any(), any())).thenReturn(500);

        List<ClassementDTO> result = challengeService.getClassement(10L);

        assertEquals(1, result.size());
        assertEquals("Jean Dupont", result.get(0).getNomComplet());
        assertEquals(500, result.get(0).getTotalCalories());
    }

    @Test
    void testRejoindreChallengeSuccess() {
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        when(participationRepository.existsByUtilisateurAndChallenge(mockUser, mockChallenge)).thenReturn(false);

        assertDoesNotThrow(() -> challengeService.rejoindreChallenge(10L, mockUser));
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    void testRejoindreChallengeAlreadyJoinedShouldThrow() {
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));
        when(participationRepository.existsByUtilisateurAndChallenge(mockUser, mockChallenge)).thenReturn(true);

        Exception ex = assertThrows(RuntimeException.class, () -> challengeService.rejoindreChallenge(10L, mockUser));
        assertEquals("Vous participez déjà à ce challenge !", ex.getMessage());
    }

    @Test
    void testSupprimerChallengeNonAuthorise() {
        Utilisateur otherUser = new Utilisateur();
        otherUser.setId(2L);
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(mockChallenge));

        Exception ex = assertThrows(RuntimeException.class, () -> challengeService.supprimerChallenge(10L, otherUser));
        assertTrue(ex.getMessage().contains("Non autorisé"));
    }
}
