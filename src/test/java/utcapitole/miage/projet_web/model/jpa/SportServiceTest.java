package utcapitole.miage.projet_web.model.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utcapitole.miage.projet_web.model.Sport;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SportServiceTest {

    @Mock
    private SportRepository sportRepository;
    @InjectMocks
    private SportService sportService;

    private Sport sport;

    private static Long idSport = 1L;
    @BeforeEach
    void setUp() {
        sport = new Sport();
        sport.setId(1L);
        sport.setNom("Football");
    }
    @Test
    void testGetAllShouldReturnList(){
        List<Sport> sports = List.of(sport);
        when(sportRepository.findAll()).thenReturn(sports);

        List<Sport> result = sportService.getAll();
        assertEquals(1,result.size());
        assertEquals("Football",result.get(0).getNom());

        verify(sportRepository).findAll();

    }
    @Test
    void testGetByIdFound(){
        when(sportRepository.findById(idSport)).thenReturn(Optional.of(sport));
        Sport resultat = sportService.getById(idSport);

        assertEquals(sport,resultat);
        verify(sportRepository).findById(idSport);

    }

    @Test
    void testGetByIdNotFound(){
        when(sportRepository.findById(idSport)).thenReturn(Optional.empty());
        Sport resultat = sportService.getById(idSport);

        assertNull(resultat);
        verify(sportRepository).findById(idSport);

    }


}