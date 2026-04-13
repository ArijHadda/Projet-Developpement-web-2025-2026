package utcapitole.miage.projet_web.model.jpa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

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
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.Commentaire;

@ExtendWith(MockitoExtension.class)
class ActiviteServiceTest {

    @Mock
    private ActiviteRepository activiteRepository;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private CommentaireRepository commentaireRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private RestTemplate restTemplate;

    private ActiviteService activiteService;

    // ─────────────────── Helpers communs aux mocks météo ───────────────────

    private Map<String, Object> buildIpApiResponse(double lat, double lon) {
        Map<String, Object> r = new HashMap<>();
        r.put("lat", lat);
        r.put("lon", lon);
        return r;
    }

    private Map<String, Object> buildMeteoResponse(double temperature) {
        Map<String, Object> currentWeather = new HashMap<>();
        currentWeather.put("temperature", temperature);
        Map<String, Object> meteoResponse = new HashMap<>();
        meteoResponse.put("current_weather", currentWeather);
        return meteoResponse;
    }

    private Utilisateur buildUser(float poids) {
        Utilisateur user = new Utilisateur();
        user.setPoids(poids);
        return user;
    }

    @BeforeEach
    void setUp() {
        activiteService = new ActiviteService(activiteRepository, sportRepository, commentaireRepository, utilisateurRepository);
        ReflectionTestUtils.setField(activiteService, "restTemplate", restTemplate);

        lenient().when(sportRepository.findByNom(any())).thenAnswer(invocation -> {
            String nom = invocation.getArgument(0);
            if ("Course".equals(nom)) return new Sport("Course", "Endurance", 0.0, 1.0, true);
            if ("Cyclisme".equals(nom)) return new Sport("Cyclisme", "Endurance", 2.0, 0.4, true);
            if ("Marche".equals(nom)) return new Sport("Marche", "Endurance", 2.0, 0.3, true);
            if ("Natation".equals(nom)) return new Sport("Natation", "Endurance", 4.0, 1.0, false);
            return null;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // [Test] #60 – Déclencheur de calcul automatique
    // ═══════════════════════════════════════════════════════════════════════
    @Test
    void testEnregistrerActivite_caloriesCalculeesEtStockeesDansDB() {
        // Initialisation
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Course");
        activite.setDuree(60);
        activite.setDistance(10.0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(20.5));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Action
        Activite savedActivite = activiteService.enregistrerActivite(activite);

        // Then
        assertTrue(savedActivite.getCaloriesConsommees() > 0);
        verify(activiteRepository).save(activite);

        // vitesse = 10 km/h → MET = 0.0 + 1.0 * 10 = 10.0
        // calories = 10.0 * 70 * 1.0 = 700
        assertEquals(700, savedActivite.getCaloriesConsommees());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // [Test] #61 – Dépendance des paramètres de calcul
    // ═══════════════════════════════════════════════════════════════════════
    @Test
    void testEnregistrerActivite_poissDifferentsDonnentCaloriesDifferentes() {
        // Initialisation
        Utilisateur userA = buildUser(60.0f);
        Utilisateur userB = buildUser(90.0f);

        Activite activiteA = new Activite();
        activiteA.setNom("Natation");
        activiteA.setDuree(60);
        activiteA.setDistance(0);
        activiteA.setNiveauIntensite(3);
        activiteA.setUtilisateur(userA);

        Activite activiteB = new Activite();
        activiteB.setNom("Natation");
        activiteB.setDuree(60);
        activiteB.setDistance(0);
        activiteB.setNiveauIntensite(3);
        activiteB.setUtilisateur(userB);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(22.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Activite savedA = activiteService.enregistrerActivite(activiteA);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(22.0));

        Activite savedB = activiteService.enregistrerActivite(activiteB);

        assertTrue(savedA.getCaloriesConsommees() > 0);
        assertTrue(savedB.getCaloriesConsommees() > 0);
        assertNotEquals(savedA.getCaloriesConsommees(), savedB.getCaloriesConsommees());

        // Natation (base 4.0, coeff 1.0, niveau 3) -> MET = 7.0
        // A = 7.0*60*1 = 420. B = 7.0*90*1 = 630
        assertEquals(420, savedA.getCaloriesConsommees());
        assertEquals(630, savedB.getCaloriesConsommees());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tests de couverture 
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testCalculerCalories_utilisateurNull_retourne0() {
        Activite activite = new Activite();
        activite.setNom("Course");
        activite.setDuree(60);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(18.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        assertEquals(0, saved.getCaloriesConsommees());
    }

    @Test
    void testCalculerCalories_dureeNulle_retourne0() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Course");
        activite.setDuree(0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(18.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        assertEquals(0, saved.getCaloriesConsommees());
    }

    @Test
    void testCalculerCalories_poidsNul_utilisePoidsDefaut() {
        Utilisateur user = buildUser(0.0f);  // Défaut -> 70.0f
        Activite activite = new Activite();
        activite.setNom("Natation");
        activite.setDuree(60);
        activite.setNiveauIntensite(3); // MET = 4.0 + 1.0*3 = 7.0
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(20.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        // 7.0 * 70 * 1 = 490
        assertEquals(490, saved.getCaloriesConsommees());
    }

    @Test
    void testCalculerCalories_courseVitessesDifferentes() {
        // MET = 0.0 + 1.0 * v. Cal = MET * 70 * 1 = v * 70
        assertCaloriesCourse(5.0, 70.0f, 350);
        assertCaloriesCourse(7.0, 70.0f, 490);
        assertCaloriesCourse(9.0, 70.0f, 630);
        assertCaloriesCourse(11.0, 70.0f, 770);
        assertCaloriesCourse(14.0, 70.0f, 980);
    }

    @Test
    void testCalculerCalories_courseSansDistance_MET0() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Course");
        activite.setDuree(60);
        activite.setDistance(0); // v=0 -> MET=0
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(20.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        assertEquals(0, saved.getCaloriesConsommees());
    }

    @Test
    void testCalculerCalories_cyclismeSansDistance() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Cyclisme");
        activite.setDuree(60);
        activite.setDistance(0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(20.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        // MET = 2.0 + 0.4*0 = 2.0 => 2.0*70*1=140
        assertEquals(140, saved.getCaloriesConsommees());
    }

    @Test
    void testCalculerCalories_cyclismeVitesses() {
        // MET = 2.0 + 0.4 * v
        assertCaloriesCyclisme(10.0, 70.0f, 420); // 2+4=6 => 420
        assertCaloriesCyclisme(17.0, 70.0f, 616); // 2+6.8=8.8 => 616
        assertCaloriesCyclisme(20.0, 70.0f, 700); // 2+8=10 => 700
        assertCaloriesCyclisme(23.0, 70.0f, 784); // 2+9.2=11.2 => 784
        assertCaloriesCyclisme(30.0, 70.0f, 980); // 2+12=14 => 980
    }

    @Test
    void testCalculerCalories_marcheVitesses() {
        // MET = 2.0 + 0.3 * v
        assertCaloriesMarche(2.0, 70.0f, 182); // 2 + 0.6 = 2.6 => 182
        assertCaloriesMarche(4.0, 70.0f, 224); // 2 + 1.2 = 3.2 => 224
        assertCaloriesMarche(6.0, 70.0f, 266); // 2 + 1.8 = 3.8 => 266
    }

    @Test
    void testCalculerCalories_typeInconnu_METdefaut() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Yoga"); // non mocké -> fallback -> 4.0
        activite.setDuree(60);
        activite.setDistance(0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(20.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        // MET=4.0, poids=70, 1h → 4*70*1=280
        assertEquals(280, saved.getCaloriesConsommees());
    }

    @Test
    void testCalculerCalories_nomNull_METdefaut() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom(null);
        activite.setDuree(60);
        activite.setDistance(0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(20.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        assertEquals(280, saved.getCaloriesConsommees());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tests API météo / coordonnnees
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testEnregistrerActivite_CoordonneesException_utiliseCoordonneesToulouse() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Natation");
        activite.setNiveauIntensite(3);
        activite.setDuree(60);
        activite.setDistance(0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenThrow(new RestClientException("IP API Error"));
        when(restTemplate.getForObject(contains("latitude=43.6047&longitude=1.4442"), eq(Map.class))).thenReturn(buildMeteoResponse(15.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        assertEquals(490, saved.getCaloriesConsommees()); // 7.0 * 70 * 1
        assertEquals("Température: 15.0°C", saved.getConditionsMeteo());
    }

    @Test
    void testEnregistrerActivite_MeteoException_retourneMeteoIndisponible() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Natation");
        activite.setNiveauIntensite(3);
        activite.setDuree(60);
        activite.setDistance(0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenThrow(new RestClientException("Weather API Error"));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        assertEquals(490, saved.getCaloriesConsommees());
        assertEquals("Météo indisponible", saved.getConditionsMeteo());
    }

    @Test
    void testEnregistrerActivite_appelleApiMeteoEtStockeLesDonneesMeteo() {
        Utilisateur user = buildUser(70.0f);
        Activite activite = new Activite();
        activite.setNom("Course");
        activite.setDate(java.time.LocalDate.now());
        activite.setDuree(60);
        activite.setDistance(10.0);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(43.6047, 1.4442));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(26.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);

        verify(restTemplate).getForObject("http://ip-api.com/json/", Map.class);
        verify(restTemplate).getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class));
        verify(activiteRepository).save(activite);
        assertEquals("Température: 26.0°C", saved.getConditionsMeteo());
        assertEquals(700, saved.getCaloriesConsommees());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tests repository
    // ═══════════════════════════════════════════════════════════════════════

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
        when(user.getId()).thenReturn(1L);
        Activite a1 = new Activite();
        when(activiteRepository.findByUtilisateurIdOrderByDateDesc(1L)).thenReturn(Arrays.asList(a1));
        List<Activite> activites = activiteService.getActivitesByUtilisateur(user);
        assertEquals(1, activites.size());
        verify(activiteRepository).findByUtilisateurIdOrderByDateDesc(1L);
    }

    @Test
    void testGetStatsActivites_calculeCorrectementLesAggregats() {
        // Initialisation
        Activite a1 = new Activite();
        a1.setDuree(30);
        a1.setDistance(5.0);
        a1.setCaloriesConsommees(300);

        Activite a2 = new Activite();
        a2.setDuree(60);
        a2.setDistance(10.0);
        a2.setCaloriesConsommees(700);

        List<Activite> list = Arrays.asList(a1, a2);

        // Action
        Map<String, Object> stats = activiteService.getStatsActivites(list);

        // Then
        assertEquals(2, stats.get("count"));
        assertEquals(90.0, stats.get("totalDuree"));
        assertEquals(15.0, stats.get("totalDistance"));
        assertEquals(1000, stats.get("totalCalories"));
    }

    // ─────────────────── Méthodes utilitaires ────────────────────────────────

    private void assertCaloriesCyclisme(double distanceKm, float poids, int expectedCalories) {
        assertCaloriesGenerique("Cyclisme", distanceKm, poids, expectedCalories);
    }
    
    private void assertCaloriesCourse(double distanceKm, float poids, int expectedCalories) {
        assertCaloriesGenerique("Course", distanceKm, poids, expectedCalories);
    }
    
    private void assertCaloriesMarche(double distanceKm, float poids, int expectedCalories) {
        assertCaloriesGenerique("Marche", distanceKm, poids, expectedCalories);
    }

    private void assertCaloriesGenerique(String nomSport, double distanceKm, float poids, int expectedCalories) {
        Utilisateur user = buildUser(poids);
        Activite activite = new Activite();
        activite.setNom(nomSport);
        activite.setDuree(60);
        activite.setDistance(distanceKm);
        activite.setUtilisateur(user);

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(buildIpApiResponse(48.8566, 2.3522));
        when(restTemplate.getForObject(contains("https://api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(buildMeteoResponse(20.0));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> inv.getArgument(0));

        Activite saved = activiteService.enregistrerActivite(activite);
        assertEquals(expectedCalories, saved.getCaloriesConsommees(),
                nomSport + " à " + distanceKm + " km/h devrait donner " + expectedCalories + " cal");
    }
    // ═══════════════════════════════════════════════════════════════════════
    // [Test] Nouveaux tests pour Flux, Kudos et Commentaires
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testGetFluxActivitesAmis_sansAmis_retourneListeVide() {
        Utilisateur user = new Utilisateur();
        user.setAmis(new java.util.ArrayList<>()); // Pas d'amis

        List<Activite> flux = activiteService.getFluxActivitesAmis(user);
        assertTrue(flux.isEmpty());
    }

    @Test
    void testToggleKudos_ajouteEtSupprime() {
        Activite activite = new Activite();
        activite.setId(10L);
        activite.setLikers(new java.util.ArrayList<>());

        Utilisateur user = new Utilisateur();
        user.setId(5L);

        when(activiteRepository.findById(10L)).thenReturn(java.util.Optional.of(activite));
        when(utilisateurRepository.findById(5L)).thenReturn(java.util.Optional.of(user));

        // 1er clic : Ajoute le Kudo
        activiteService.toggleKudos(10L, 5L);
        assertEquals(1, activite.getNbKudos());
        assertTrue(activite.getLikers().contains(user));
        verify(activiteRepository).save(activite);

        // 2ème clic : Retire le Kudo
        activiteService.toggleKudos(10L, 5L);
        assertEquals(0, activite.getNbKudos());
        assertFalse(activite.getLikers().contains(user));
    }

    @Test
    void testAjouterCommentaire() {
        Activite activite = new Activite();
        activite.setId(10L);

        Utilisateur user = new Utilisateur();
        user.setId(5L);

        when(activiteRepository.findById(10L)).thenReturn(java.util.Optional.of(activite));
        when(utilisateurRepository.findById(5L)).thenReturn(java.util.Optional.of(user));

        activiteService.ajouterCommentaire(10L, 5L, "Super course !");

        verify(commentaireRepository).save(any(Commentaire.class));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // [Test] #30 – Statistiques globales
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testGetStatsActivites_listeVide_retourneZeros() {
        Map<String, Object> stats = activiteService.getStatsActivites(java.util.Collections.emptyList());

        assertEquals(0, stats.get("count"));
        assertEquals(0.0, stats.get("totalDuree"));
        assertEquals(0.0, stats.get("totalDistance"));
        assertEquals(0, stats.get("totalCalories"));
    }

    @Test
    void testGetStatsActivites_uneSeuleActivite_retourneSesValeurs() {
        Activite a = new Activite();
        a.setDuree(45);
        a.setDistance(5.5);
        a.setCaloriesConsommees(320);

        Map<String, Object> stats = activiteService.getStatsActivites(Arrays.asList(a));

        assertEquals(1, stats.get("count"));
        assertEquals(45.0, stats.get("totalDuree"));
        assertEquals(5.5, stats.get("totalDistance"));
        assertEquals(320, stats.get("totalCalories"));
    }

    @Test
    void testGetStatsActivites_plusieursActivites_sommeCaloriesEtDistance() {
        Activite a1 = new Activite();
        a1.setDuree(30);
        a1.setDistance(4.2);
        a1.setCaloriesConsommees(210);

        Activite a2 = new Activite();
        a2.setDuree(60);
        a2.setDistance(10.0);
        a2.setCaloriesConsommees(560);

        Activite a3 = new Activite();
        a3.setDuree(45);
        a3.setDistance(6.8);
        a3.setCaloriesConsommees(380);

        Map<String, Object> stats = activiteService.getStatsActivites(Arrays.asList(a1, a2, a3));

        assertEquals(3, stats.get("count"));
        assertEquals(135.0, stats.get("totalDuree"));
        assertEquals(21.0, stats.get("totalDistance"));
        assertEquals(1150, stats.get("totalCalories"));
    }

    @Test
    void testGetStatsActivites_totalCaloriesCorrespondAuCalculMet() {
        Activite a = new Activite();
        a.setDuree(90);
        a.setDistance(12.0);
        a.setCaloriesConsommees(840);

        Map<String, Object> stats = activiteService.getStatsActivites(Arrays.asList(a));

        assertEquals(840, stats.get("totalCalories"));
    }

    @Test
    void testGetStatsActivites_distanceTotaleEstExacte() {
        Activite a1 = new Activite();
        a1.setDuree(30);
        a1.setDistance(3.45);
        a1.setCaloriesConsommees(200);

        Activite a2 = new Activite();
        a2.setDuree(60);
        a2.setDistance(8.75);
        a2.setCaloriesConsommees(500);

        Map<String, Object> stats = activiteService.getStatsActivites(Arrays.asList(a1, a2));

        assertEquals(12.2, (double) stats.get("totalDistance"), 0.01);
    }
}
