package utcapitole.miage.projet_web.model.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utcapitole.miage.projet_web.dto.ObjectifProgressDTO;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObjectifServiceTest {

    @Mock
    private ObjectifRepository objectifRepository;

    @Mock
    private ActiviteRepository activiteRepository;

    @InjectMocks
    private ObjectifService objectifService;

    private Utilisateur mockUser;
    private Sport mockSport;
    private Objectif mockObj;

    @BeforeEach
    void setUp() {
        mockUser = new Utilisateur();
        mockUser.setId(1L);

        mockSport = new Sport();
        mockSport.setId(2L);

        mockObj = new Objectif("100km Velo", "Mensuel", 600, 100.0, mockUser, mockSport);
        mockObj.setId(10L);
    }

    @Test
    void testEnregistrerEtSupprimer() {
        when(objectifRepository.save(any(Objectif.class))).thenReturn(mockObj);

        Objectif saved = objectifService.enregistrerObjectif(mockObj);
        assertEquals("100km Velo", saved.getTitre());

        objectifService.supprimerObjectif(10L);
        verify(objectifRepository).deleteById(10L);
    }

    @Test
    void testGetObjectifById() {
        when(objectifRepository.findById(10L)).thenReturn(Optional.of(mockObj));

        Optional<Objectif> result = objectifService.getObjectifById(10L);
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void testGetObjectifsAvecProgression_CalculeCorrectement() {
        // Préparation : Un objectif de 100km et 600 min
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(mockObj));

        // L'utilisateur a fait 50km et 300 min (donc 50% de progression)
        when(activiteRepository.calculerDistanceTotale(eq(1L), eq(2L), any(), any())).thenReturn(50.0);
        when(activiteRepository.calculerDureeTotale(eq(1L), eq(2L), any(), any())).thenReturn(300L);

        List<ObjectifProgressDTO> result = objectifService.getObjectifsAvecProgression(mockUser);

        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0).getDistanceActuelle());
        assertEquals(50.0, result.get(0).getPourcentageDistance());
        assertEquals(300.0, result.get(0).getDureeActuelle());
        assertEquals(50.0, result.get(0).getPourcentageDuree());
    }

    @Test
    void testGetObjectifsAvecProgression_PlafonneA100Pourcent() {
        // Préparation
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(mockObj));

        // L'utilisateur a explosé son objectif : 150km au lieu de 100km, et 1200 min au lieu de 600
        when(activiteRepository.calculerDistanceTotale(eq(1L), eq(2L), any(), any())).thenReturn(150.0);
        when(activiteRepository.calculerDureeTotale(eq(1L), eq(2L), any(), any())).thenReturn(1200L);

        List<ObjectifProgressDTO> result = objectifService.getObjectifsAvecProgression(mockUser);

        assertEquals(1, result.size());
        assertEquals(150.0, result.get(0).getDistanceActuelle());
        // Le pourcentage doit être plafonné à 100%
        assertEquals(100.0, result.get(0).getPourcentageDistance());
        assertEquals(100.0, result.get(0).getPourcentageDuree());
    }

    @Test
    void testGetObjectifsByUtilisateur() {
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(mockObj));

        List<Objectif> result = objectifService.getObjectifsByUtilisateur(mockUser);

        assertEquals(1, result.size());
        assertEquals(mockObj, result.get(0));
    }

    @Test
    void testGetObjectifsAvecProgression_AvecZeroObjectif() {
        // Préparation : Un objectif où la distance et la durée sont égales à 0
        Objectif objZero = new Objectif("Rien faire", "Mensuel", 0, 0.0, mockUser, mockSport);
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(objZero));

        List<ObjectifProgressDTO> result = objectifService.getObjectifsAvecProgression(mockUser);

        // Vérification des résultats (tout doit rester à 0)
        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0).getDistanceActuelle());
        assertEquals(0.0, result.get(0).getPourcentageDistance());
        assertEquals(0.0, result.get(0).getDureeActuelle());
        assertEquals(0.0, result.get(0).getPourcentageDuree());

        // Vérification CRUCIALE : On s'assure que la base de données n'a JAMAIS été appelée,
        // (car les if(>0) ont empêché l'exécution des requêtes)
        verify(activiteRepository, never()).calculerDistanceTotale(anyLong(), anyLong(), any(), any());
        verify(activiteRepository, never()).calculerDureeTotale(anyLong(), anyLong(), any(), any());
    }

    @Test
    void testGetObjectifsAvecProgression_RetourRepositoryNull() {
        // Préparation : On utilise le mockObj normal (100km, 600min)
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(mockObj));

        // On simule le cas où calculerDureeTotale retourne NULL (l'utilisateur n'a aucune activité)
        when(activiteRepository.calculerDistanceTotale(eq(1L), eq(2L), any(), any())).thenReturn(0.0);
        when(activiteRepository.calculerDureeTotale(eq(1L), eq(2L), any(), any())).thenReturn(null);

        List<ObjectifProgressDTO> result = objectifService.getObjectifsAvecProgression(mockUser);

        // Vérification : Le code doit remplacer null par 0.0 sans planter (NullPointerException)
        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0).getDureeActuelle());
        assertEquals(0.0, result.get(0).getPourcentageDuree());
    }
}