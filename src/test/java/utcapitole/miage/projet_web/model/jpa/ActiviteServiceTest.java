package utcapitole.miage.projet_web.model.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

@ExtendWith(MockitoExtension.class)
class ActiviteServiceTest {

    @Mock
    private ActiviteRepository activiteRepository;

    @Mock
    private RestTemplate restTemplate;

    private ActiviteService activiteService;

    @BeforeEach
    void setUp() {
        activiteService = new ActiviteService(activiteRepository);
        ReflectionTestUtils.setField(activiteService, "restTemplate", restTemplate);
    }

    @Test
    void testEnregistrerActivite_Success() {
        Activite activite = new Activite();
        activite.setDuree(60);
        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 48.8566);
        ipApiResponse.put("lon", 2.3522);

        Map<String, Object> currentWeather = new HashMap<>();
        currentWeather.put("temperature", 20.5);
        Map<String, Object> meteoResponse = new HashMap<>();
        meteoResponse.put("current_weather", currentWeather);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(meteoResponse);
        when(activiteRepository.save(any(Activite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Activite savedActivite = activiteService.enregistrerActivite(activite);
        assertEquals(600, savedActivite.getCaloriesConsommees());
        assertEquals("Température: 20.5°C", savedActivite.getConditionsMeteo());
        verify(activiteRepository).save(activite);
    }

    @Test
    void testEnregistrerActivite_CoordonneesException() {
        Activite activite = new Activite();
        activite.setDuree(30);
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenThrow(new RestClientException("IP API Error"));
        Map<String, Object> currentWeather = new HashMap<>();
        currentWeather.put("temperature", 15.0);
        Map<String, Object> meteoResponse = new HashMap<>();
        meteoResponse.put("current_weather", currentWeather);
        when(restTemplate.getForObject(contains("latitude=43.6047&longitude=1.4442"), eq(Map.class))).thenReturn(meteoResponse);
        when(activiteRepository.save(any(Activite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Activite savedActivite = activiteService.enregistrerActivite(activite);
        assertEquals(300, savedActivite.getCaloriesConsommees());
        assertEquals("Température: 15.0°C", savedActivite.getConditionsMeteo());
    }

    @Test
    void testEnregistrerActivite_MeteoException() {
        Activite activite = new Activite();
        activite.setDuree(45);
        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 48.8566);
        ipApiResponse.put("lon", 2.3522);
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenThrow(new RestClientException("Weather API Error"));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Activite savedActivite = activiteService.enregistrerActivite(activite);
        assertEquals(450, savedActivite.getCaloriesConsommees());
        assertEquals("Météo indisponible", savedActivite.getConditionsMeteo());
    }

    @Test
    void testGetToutesLesActivites() {
        Activite a1 = new Activite();
        Activite a2 = new Activite();
        when(activiteRepository.findAll()).thenReturn(Arrays.asList(a1, a2));
        List<Activite> activites = activiteService.getToutesLesActivites();
        assertEquals(2, activites.size());
        verify(activiteRepository).findAll();
    }

    @Test
    void testGetProgresUtilisateur() {
        Activite a1 = new Activite();
        when(activiteRepository.findByUtilisateurIdOrderByDateDesc(1L)).thenReturn(Arrays.asList(a1));
        List<Activite> activites = activiteService.getProgresUtilisateur(1L);
        assertEquals(1, activites.size());
        verify(activiteRepository).findByUtilisateurIdOrderByDateDesc(1L);
    }

    @Test
    void testGetActivitesByUtilisateur() {
        Utilisateur user = mock(Utilisateur.class);
        Activite a1 = new Activite();
        when(activiteRepository.findByUtilisateur(user)).thenReturn(Arrays.asList(a1));
        List<Activite> activites = activiteService.getActivitesByUtilisateur(user);
        assertEquals(1, activites.size());
        verify(activiteRepository).findByUtilisateur(user);
    }

}
