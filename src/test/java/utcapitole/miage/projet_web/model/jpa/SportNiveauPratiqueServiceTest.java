package utcapitole.miage.projet_web.model.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utcapitole.miage.projet_web.model.SportNiveauPratique;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SportNiveauPratiqueServiceTest {

    @Mock
    private SportNiveauPratiqueRepository sportNiveauPratiqueRepository;
    @InjectMocks
    private SportNiveauPratiqueService sportNiveauPratiqueService;

    private SportNiveauPratique sportNiveauPratique;

    private static Long idSportNiveauPratique = 1L;
    private static Long idSport = 2L;
    @BeforeEach
    void setUp(){
        sportNiveauPratique = new SportNiveauPratique();
        sportNiveauPratique.setId(1L);
    }

    @Test
    void testDeleteById(){
        sportNiveauPratiqueService.deleteById(idSportNiveauPratique);
        verify(sportNiveauPratiqueRepository).deleteById(idSportNiveauPratique);
    }

    @Test
    void testFindByUtilisateurIdAndSportId_Found() {
        when(sportNiveauPratiqueRepository.findByUtilisateurIdAndSportId(idSportNiveauPratique, idSport))
                .thenReturn(Optional.of(sportNiveauPratique));

        Optional<SportNiveauPratique> result =
                sportNiveauPratiqueService.findByUtilisateurIdAndSportId(idSportNiveauPratique, idSport);

        assertTrue(result.isPresent());
        assertEquals(sportNiveauPratique, result.get());
        verify(sportNiveauPratiqueRepository).findByUtilisateurIdAndSportId(idSportNiveauPratique, idSport);
    }
    @Test
    void testFindByUtilisateurIdAndSportId_NotFound(){
        when(sportNiveauPratiqueRepository.findByUtilisateurIdAndSportId
                (idSportNiveauPratique,idSport)).thenReturn(Optional.empty());

        Optional<SportNiveauPratique> resultat = sportNiveauPratiqueService
                .findByUtilisateurIdAndSportId(idSportNiveauPratique,idSport);

        assertFalse(resultat.isPresent());
        verify(sportNiveauPratiqueRepository).findByUtilisateurIdAndSportId(idSportNiveauPratique,idSport);
    }

    @Test
    void testSave() {
        sportNiveauPratiqueService.save(sportNiveauPratique);
        verify(sportNiveauPratiqueRepository).save(sportNiveauPratique);
    }

    @Test
    void testConstructor_InjectsRepositoryCorrectly() {
        SportNiveauPratiqueRepository mockRepo = mock(SportNiveauPratiqueRepository.class);

        SportNiveauPratiqueService service =
                new SportNiveauPratiqueService(mockRepo);

        assertNotNull(service);
        service.deleteById(5L);

        verify(mockRepo).deleteById(5L);
    }
}