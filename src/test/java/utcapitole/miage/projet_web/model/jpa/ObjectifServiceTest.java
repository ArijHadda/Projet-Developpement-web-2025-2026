package utcapitole.miage.projet_web.model.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utcapitole.miage.projet_web.dto.ObjectifProgressDTO;
import utcapitole.miage.projet_web.model.Frequence;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

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

    @Mock
    private BadgeAttributionService badgeAttributionService; // <-- Ajout du mock manquant

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

        mockObj = new Objectif("100km Velo", Frequence.MENSUEL, 600, 100.0, mockUser, mockSport);
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

        // Non complété, donc pas de badge
        verify(badgeAttributionService, never()).attribuerBadgeObjectifComplet(any());
    }

    @Test
    void testGetObjectifsAvecProgression_PlafonneA100PourcentEtAttribueBadge() {
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

        // L'objectif est complété, le badge doit être attribué
        verify(badgeAttributionService).attribuerBadgeObjectifComplet(mockUser);
    }

    @Test
    void testGetObjectifsAvecProgression_DistanceUniquement_AttribueBadge() {
        // Un objectif basé uniquement sur la distance
        Objectif objDist = new Objectif("100km Velo", Frequence.MENSUEL, 0, 100.0, mockUser, mockSport);
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(objDist));
        when(activiteRepository.calculerDistanceTotale(anyLong(), anyLong(), any(), any())).thenReturn(100.0);

        objectifService.getObjectifsAvecProgression(mockUser);

        verify(badgeAttributionService).attribuerBadgeObjectifComplet(mockUser);
    }

    @Test
    void testGetObjectifsAvecProgression_DureeUniquement_AttribueBadge() {
        // Un objectif basé uniquement sur la durée
        Objectif objDur = new Objectif("600min Velo", Frequence.MENSUEL, 600, 0.0, mockUser, mockSport);
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(objDur));
        when(activiteRepository.calculerDureeTotale(anyLong(), anyLong(), any(), any())).thenReturn(600L);

        objectifService.getObjectifsAvecProgression(mockUser);

        verify(badgeAttributionService).attribuerBadgeObjectifComplet(mockUser);
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
        Objectif objZero = new Objectif("Rien faire", Frequence.MENSUEL, 0, 0.0, mockUser, mockSport);
        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(objZero));

        List<ObjectifProgressDTO> result = objectifService.getObjectifsAvecProgression(mockUser);

        // Vérification des résultats (tout doit rester à 0)
        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0).getDistanceActuelle());
        assertEquals(0.0, result.get(0).getPourcentageDistance());
        assertEquals(0.0, result.get(0).getDureeActuelle());
        assertEquals(0.0, result.get(0).getPourcentageDuree());
        // Vérification CRUCIALE : On s'assure que la base de données n'a JAMAIS été appelée,
        // car la condition ">0" a empêché l'exécution des requêtes.
        verify(activiteRepository, never()).calculerDistanceTotale(anyLong(), anyLong(), any(), any());
        verify(activiteRepository, never()).calculerDureeTotale(anyLong(), anyLong(), any(), any());
    }

    @Test
    void testGetObjectifsAvecProgression_CalculDynamiqueDates() {
        Objectif objHebdo = new Objectif("Hebdo", Frequence.HEBDOMADAIRE, 0, 50.0, mockUser, mockSport);
        Objectif objDaily = new Objectif("Daily", Frequence.QUOTIDIEN, 0, 5.0, mockUser, mockSport);

        when(objectifRepository.findByUtilisateur(mockUser)).thenReturn(List.of(objHebdo, objDaily));

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        objectifService.getObjectifsAvecProgression(mockUser);

        verify(activiteRepository, times(2)).calculerDistanceTotale(
                eq(mockUser.getId()), eq(mockSport.getId()), dateCaptor.capture(), dateCaptor.capture());

        List<LocalDate> dates = dateCaptor.getAllValues();

        assertTrue(dates.get(0).isBefore(dates.get(1)) || dates.get(0).isEqual(dates.get(1)));
        assertEquals(dates.get(2), dates.get(3));
    }

    @Test
    void testGetObjectifsAvecProgression_ToutesFrequences_Complet() {
        Objectif daily = new Objectif("Daily", Frequence.QUOTIDIEN, 60, 10.0, mockUser, mockSport);
        Objectif weekly = new Objectif("Weekly", Frequence.HEBDOMADAIRE, 300, 50.0, mockUser, mockSport);
        Objectif monthly = new Objectif("Monthly", Frequence.MENSUEL, 1200, 200.0, mockUser, mockSport);
        Objectif yearly = new Objectif("Yearly", Frequence.ANNUEL, 5000, 1000.0, mockUser, mockSport);

        when(objectifRepository.findByUtilisateur(mockUser))
                .thenReturn(List.of(daily, weekly, monthly, yearly));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        objectifService.getObjectifsAvecProgression(mockUser);

        verify(activiteRepository, times(4)).calculerDistanceTotale(anyLong(), anyLong(), startCaptor.capture(), endCaptor.capture());
        verify(activiteRepository, times(4)).calculerDureeTotale(anyLong(), anyLong(), any(), any());

        List<LocalDate> starts = startCaptor.getAllValues();
        List<LocalDate> ends = endCaptor.getAllValues();
        LocalDate now = LocalDate.now();

        assertAll("QUOTIDIEN",
                () -> assertEquals(now, starts.get(0)),
                () -> assertEquals(now, ends.get(0))
        );
        assertAll("HEBDOMADAIRE",
                () -> assertEquals(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), starts.get(1)),
                () -> assertEquals(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)), ends.get(1))
        );
        assertAll("MENSUEL",
                () -> assertEquals(now.withDayOfMonth(1), starts.get(2)),
                () -> assertEquals(now.withDayOfMonth(now.lengthOfMonth()), ends.get(2))
        );
        assertAll("ANNUEL",
                () -> assertEquals(now.with(TemporalAdjusters.firstDayOfYear()), starts.get(3)),
                () -> assertEquals(now.with(TemporalAdjusters.lastDayOfYear()), ends.get(3))
        );
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

    @Test
    void testConstructeurAlternatif() {
        // Appeler explicitement le constructeur à 2 paramètres pour couvrir 100% des méthodes
        ObjectifService serviceAlternatif = new ObjectifService(objectifRepository, activiteRepository);
        assertNotNull(serviceAlternatif);
    }
}