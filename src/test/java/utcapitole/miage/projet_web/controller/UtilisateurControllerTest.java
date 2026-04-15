package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.NiveauPratique;
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.SportNiveauPratique;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.BadgeAttributionService;
import utcapitole.miage.projet_web.model.jpa.SportNiveauPratiqueService;
import utcapitole.miage.projet_web.model.jpa.SportService;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UtilisateurControllerTest {

    private UtilisateurController controller;
    private FakeUtilisateurService utilisateurService;
    private FakeBadgeAttributionService badgeService;

    // Utilisation de Mockito pour les nouveaux services afin d'éviter les problèmes de constructeur
    private SportService sportService;
    private SportNiveauPratiqueService sportNiveauPratiqueService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        controller = new UtilisateurController();
        utilisateurService = new FakeUtilisateurService();
        badgeService = new FakeBadgeAttributionService();
        sportService = mock(SportService.class);
        sportNiveauPratiqueService = mock(SportNiveauPratiqueService.class);

        setField(controller, "utilisateurService", utilisateurService);
        setField(controller, "passwordEncoder", new BCryptPasswordEncoder());
        setField(controller, "badgeAttributionService", badgeService);
        setField(controller, "sportService", sportService);
        setField(controller, "sportNiveauPratiqueService", sportNiveauPratiqueService);

        restTemplate = mock(RestTemplate.class);
        setField(controller, "restTemplate", restTemplate);
    }

    @Test
    void testShowLoginAndRegisterForms() {
        // Test de la route GET /login
        assertEquals("login", controller.showLoginForm());

        // Test de la route GET /register
        Model model = new ExtendedModelMap();
        assertEquals("register", controller.showRegisterForm(model));
        assertTrue(model.containsAttribute("utilisateur"));
    }

    @Test
    void loginFlowSuccessAndFailure() {
        String plainPassword = "secret";
        Utilisateur user = user(1L, "a@test.fr", new BCryptPasswordEncoder().encode(plainPassword));
        utilisateurService.byMail.put("a@test.fr", user);

        HttpSession session = new MockHttpSession();
        Model model = new ExtendedModelMap();

        String success = controller.processLogin("a@test.fr", plainPassword, session, model);
        assertEquals("redirect:/user/profile/" + user.getId(), success);

        String failure = controller.processLogin("a@test.fr", "bad", new MockHttpSession(), new ExtendedModelMap());
        assertEquals("login", failure);
    }

    @Test
    void registerFlowDuplicateThenSuccess() {
        Utilisateur duplicate = user(2L, "dup@test.fr", "pwd");
        utilisateurService.byMail.put("dup@test.fr", duplicate);

        Model model = new ExtendedModelMap();
        String duplicateView = controller.processRegister(duplicate, model);
        assertEquals("register", duplicateView);
        assertEquals("Cet email est déjà utilisé !", model.getAttribute("error"));

        Utilisateur fresh = user(3L, "new@test.fr", "pwd");
        String success = controller.processRegister(fresh, new ExtendedModelMap());
        assertEquals("redirect:/user/login", success);
        assertEquals(fresh, utilisateurService.lastRegisteredUser);
    }

    @Test
    void profileAndUpdateRoutesCoverSessionAndNotFoundBranches() {
        Utilisateur logged = user(10L, "u@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        utilisateurService.byId.put(10L, logged);
        String profileOk = controller.afficherProfile(10L, session, new ExtendedModelMap());
        assertEquals("profile", profileOk);

        String profileMissing = controller.afficherProfile(999L, session, new ExtendedModelMap());
        assertEquals("redirect:/user/login", profileMissing);

        String updateOk = controller.updateProfile(10L, session, new ExtendedModelMap());
        assertEquals("update", updateOk);

        String updateMissing = controller.updateProfile(999L, session, new ExtendedModelMap());
        assertEquals("redirect:/user/login", updateMissing);

        String noSession = controller.afficherProfile(10L, new MockHttpSession(), new ExtendedModelMap());
        assertEquals("redirect:/user/login", noSession);
    }

    @Test
    void testModifierProfile() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        String view = controller.modifierProfile(5L, "n@test.fr", "F", 20, 1.6f, 50f,  session);
        assertEquals("redirect:/user/profile/5", view);
        assertEquals("n@test.fr", utilisateurService.lastModifiedMail);

        // Test hacker (tentative de modification du profil d'un autre)
        String denied = controller.modifierProfile(99L, "n@test.fr", "F", 20, 1.6f, 50f, session);
        assertEquals("redirect:/user/login", denied);
    }

    @Test
    void testVoirListUtilisateurAndLogout() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        // Test voir utilisateurs
        String view = controller.voirListUtilisateur(new ExtendedModelMap(), session);
        assertEquals("redirect:/user/ami/chercher", view);

        // Sans session
        assertEquals("redirect:/user/login", controller.voirListUtilisateur(new ExtendedModelMap(), new MockHttpSession()));

        // Test logout
        controller.logout(session);
        assertTrue(session.isInvalid());
    }

    @Test
    void testAjouterNivPratiqueForm() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        Model model = new ExtendedModelMap();
        String view = controller.ajouterNivPratique(model, session);

        assertEquals("setSportNivPratique", view);
        assertTrue(model.containsAttribute("sports"));
        assertTrue(model.containsAttribute("niveaux"));

        // Sans session
        assertEquals("redirect:/user/login", controller.ajouterNivPratique(model, new MockHttpSession()));
    }

    @Test
    void testAjouterNiveauPratique() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        logged.setListSportNivPratique(new ArrayList<>());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(5L, logged);

        Sport mockSport = new Sport();
        when(sportService.getById(1L)).thenReturn(mockSport);

        // Cas 1 : Niveau de pratique existant, on met à jour
        SportNiveauPratique existing = new SportNiveauPratique();
        when(sportNiveauPratiqueService.findByUtilisateurIdAndSportId(5L, 1L)).thenReturn(Optional.of(existing));

        String viewExisting = controller.ajouterNiveauratique(new ExtendedModelMap(), session, 1L, NiveauPratique.DEBUTANT);
        assertEquals("redirect:/user/profile/5", viewExisting);
        verify(sportNiveauPratiqueService).save(existing);

        // Cas 2 : Nouveau sport, on crée la relation
        when(sportNiveauPratiqueService.findByUtilisateurIdAndSportId(5L, 2L)).thenReturn(Optional.empty());
        String viewNew = controller.ajouterNiveauratique(new ExtendedModelMap(), session, 2L, NiveauPratique.EXPERT);
        assertEquals("redirect:/user/profile/5", viewNew);
        assertEquals(1, logged.getListSportNivPratique().size());
        assertTrue(utilisateurService.saved); // Vérifie que la modification a été sauvegardée

        // Cas sans session
        assertEquals("redirect:/user/login", controller.ajouterNiveauratique(new ExtendedModelMap(), new MockHttpSession(), 1L, NiveauPratique.EXPERT));
    }

    @Test
    void testDeleteSportNiveau() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        String view = controller.deleteSportNiveau(100L, session);
        assertEquals("redirect:/user/profile/5", view);
        verify(sportNiveauPratiqueService).deleteById(100L);

        // Sans session
        assertEquals("redirect:/user/login", controller.deleteSportNiveau(100L, new MockHttpSession()));
    }

    @Test
    void updatePasswordFlowCoversSuccessAndErrors() {
        Utilisateur logged = user(1L, "p@test.fr", "oldpwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        assertEquals("update-password", controller.showUpdatePasswordForm(1L, session, new ExtendedModelMap()));
        assertEquals("redirect:/user/login", controller.showUpdatePasswordForm(2L, session, new ExtendedModelMap()));

        utilisateurService.passwordException = null;
        String success = controller.processUpdatePassword(1L, "old", "new", "new", session, new ExtendedModelMap());
        assertEquals("redirect:/user/profile/1?success=passwordChanged", success);

        utilisateurService.passwordException = new IllegalArgumentException("Message test");
        Model model = new ExtendedModelMap();
        String illegal = controller.processUpdatePassword(1L, "old", "new", "new", session, model);
        assertEquals("update-password", illegal);
        assertEquals("Message test", model.getAttribute("error"));

        utilisateurService.passwordException = new RuntimeException("boom");
        Model model2 = new ExtendedModelMap();
        String generic = controller.processUpdatePassword(1L, "old", "new", "new", session, model2);
        assertEquals("update-password", generic);
        assertEquals("Une erreur inattendue est survenue.", model2.getAttribute("error"));
    }

    @Test
    void testProcessUpdatePasswordWhenNotLogginRturnedValueShouldBeRedirectToLoggin(){
        MockHttpSession session = new MockHttpSession();
        Model model = new ExtendedModelMap();

        String returnValue = controller.processUpdatePassword(1L, "old", "new", "new", session, model);

        assertEquals("redirect:/user/login",returnValue);
    }

    @Test
    void testEnregistrerActiviteEtAttribuerBadgesWhenNotLogginReturnedValueShouldBeRedirectToLoggin(){
        MockHttpSession session = new MockHttpSession();

        LocalDate date = LocalDate.of(2026, 4, 12);
        String returnValue = controller.enregistrerActiviteEtAttribuerBadges(1L, "old", date, 20,1.5,session);

        assertEquals("redirect:/user/login",returnValue);
    }



    @Test
    void testUpdateProfileWhenNotFindUserReturnedValueShouldBeRedirectToLoggin(){
        MockHttpSession session = new MockHttpSession();
        Model model = new ExtendedModelMap();
        Utilisateur loggedInUser = new Utilisateur();
        loggedInUser.setId(1L);
        session.setAttribute("loggedInUser", loggedInUser);
        when(utilisateurService.getUtilisateurAvecSports(1L)).thenReturn(null);
        String result = controller.updateProfile(1L, session, model);
        assertEquals("redirect:/user/login", result);
    }

    @Test
    void badgeEndpointsCoverSessionAndAwardBranches() {
        Utilisateur logged = user(8L, "b@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        badgeService.autoBadgesToReturn = List.of();
        String noBadge = controller.enregistrerActiviteEtAttribuerBadges(
                8L, "Course", LocalDate.of(2026, 4, 1), 40, 9.0, session
        );
        assertEquals("redirect:/user/profile/8", noBadge);
        assertEquals("Course", badgeService.lastActivite.getNom());

        badgeService.autoBadgesToReturn = List.of("1er 10km");
        String withBadge = controller.enregistrerActiviteEtAttribuerBadges(
                8L, "Course", LocalDate.of(2026, 4, 1), 40, 10.0, session
        );
        assertEquals("redirect:/user/profile/8?badge=attribue", withBadge);

        String auto = controller.attribuerBadgesAutomatiques(8L, session);
        assertEquals("redirect:/user/profile/8", auto);

        String blocked = controller.attribuerBadgesAutomatiques(8L, new MockHttpSession());
        assertEquals("redirect:/user/login", blocked);
    }

    @Test
    void testAfficherProfileAvecMeteoSucces() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        // 1. Mock IP-API (Localisation)
        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        ipApiResponse.put("city", "Toulouse");
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);

        // 2. Mock Reverse Geocoding (Ville précise)
        Map<String, Object> geoResponse = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> cityData = new HashMap<>();
        cityData.put("name", "Toulouse-Centre");
        results.add(cityData);
        geoResponse.put("results", results);
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class))).thenReturn(geoResponse);

        // 3. Mock Weather API
        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> currentWeather = new HashMap<>();
        currentWeather.put("temperature", 20.5);
        currentWeather.put("weathercode", 0);
        currentWeather.put("windspeed", 15.0);
        currentWeather.put("winddirection", 0);
        currentWeather.put("is_day", 1);
        weatherResponse.put("current_weather", currentWeather);
        when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(weatherResponse);

        Model model = new ExtendedModelMap();
        String view = controller.afficherProfile(1L, session, model);

        assertEquals("profile", view);
        assertEquals("20.5°C", model.getAttribute("meteoTemperature"));
        assertEquals("☀️", model.getAttribute("meteoIcone"));
        assertEquals("Toulouse-Centre", model.getAttribute("meteoVille"));
        assertEquals("Ensoleille (Code 0)", model.getAttribute("meteoEtatCiel"));
        assertEquals("15 km/h", model.getAttribute("meteoVentVitesse"));
        assertEquals("Nord (0°)", model.getAttribute("meteoVentDirection"));
        assertEquals("Jour (1 = Oui)", model.getAttribute("meteoMoment"));
    }

    @Test
    void testAfficherProfileMeteoEchecLocalisation() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        // Mock failure de localisation (retourne null)
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        Model model = new ExtendedModelMap();
        controller.afficherProfile(1L, session, model);

        assertEquals("Meteo indisponible", model.getAttribute("meteoTemperature"));
        assertEquals("🌤️", model.getAttribute("meteoIcone"));
        assertEquals("Ville inconnue", model.getAttribute("meteoVille"));
    }

    @Test
    void testAfficherProfileMeteoFallbackVille() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        // 1. Mock IP-API (Succès)
        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        ipApiResponse.put("city", "Toulouse-IP");
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);

        // 2. Mock Reverse Geocoding (Échec - On lance une exception)
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class)))
                .thenThrow(new RuntimeException("API Geocoding Down"));

        // 3. Mock Weather API (Succès)
        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> currentWeather = new HashMap<>();
        currentWeather.put("temperature", 10.0);
        currentWeather.put("weathercode", 3);
        currentWeather.put("windspeed", 5.0);
        currentWeather.put("winddirection", 90);
        currentWeather.put("is_day", 0);
        weatherResponse.put("current_weather", currentWeather);
        when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(weatherResponse);

        Model model = new ExtendedModelMap();
        controller.afficherProfile(1L, session, model);

        // Vérification du fallback : utilise la ville de l'IP car la géocodage a échoué
        assertEquals("Toulouse-IP", model.getAttribute("meteoVille"));
        assertEquals("10.0°C", model.getAttribute("meteoTemperature"));
        assertEquals("☁️", model.getAttribute("meteoIcone"));
        assertEquals("Nuit (1 = Non)", model.getAttribute("meteoMoment"));
    }

    private Utilisateur user(Long id, String mail, String mdp) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setMail(mail);
        u.setMdp(mdp);
        return u;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Injection de test impossible pour le champ: " + fieldName, e);
        }
    }

    private static class FakeUtilisateurService extends UtilisateurService {
        Map<String, Utilisateur> byMail = new HashMap<>();
        Map<Long, Utilisateur> byId = new HashMap<>();
        List<Utilisateur> allUsers = new ArrayList<>();

        Utilisateur lastRegisteredUser;
        String lastModifiedMail;
        RuntimeException passwordException;
        boolean saved = false;

        FakeUtilisateurService() {
            super(null, new BCryptPasswordEncoder(), null);
        }

        @Override
        public void save(Utilisateur user) {
            this.saved = true;
        }

        @Override
        public Optional<Utilisateur> findByMail(String mailU) {
            return Optional.ofNullable(byMail.get(mailU));
        }

        @Override
        public Optional<Utilisateur> findById(Long idU) {
            return Optional.ofNullable(byId.get(idU));
        }

        @Override
        public Utilisateur registerUser(Utilisateur utilisateur) {
            this.lastRegisteredUser = utilisateur;
            return utilisateur;
        }

        @Override
        public Utilisateur modifierProfile(Long idU, String mailU, String sexeU, int ageU, float tailleU, float poidsU) {
            this.lastModifiedMail = mailU;
            return byId.getOrDefault(idU, new Utilisateur());
        }

        @Override
        public void changerMotDePasse(Long idU, String ancienMdp, String nouveauMdp, String confirmMdp) {
            if (passwordException != null) {
                throw passwordException;
            }
        }

        @Override
        public List<Utilisateur> findAll() {
            return allUsers;
        }

        @Override
        public Utilisateur getUtilisateurAvecSports(Long id) {
            return byId.get(id);
        }
    }

    private static class FakeBadgeAttributionService extends BadgeAttributionService {
        List<String> autoBadgesToReturn = new ArrayList<>();
        Activite lastActivite;

        FakeBadgeAttributionService() {
            super(null, null, null);
        }

        @Override
        public List<String> enregistrerActiviteEtAttribuerBadges(Long utilisateurId, Activite activite) {
            this.lastActivite = activite;
            return autoBadgesToReturn;
        }

        @Override
        public List<String> attribuerBadgesAutomatiques(Long utilisateurId) {
            return autoBadgesToReturn;
        }
    }

    @Test
    void testAjouterNiveauPratiqueUserNull() {
        // Simuler un utilisateur en session, mais qui n'existe plus en BD
        Utilisateur logged = user(99L, "ghost@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        // Comme byId ne contient pas 99L, getUtilisateurAvecSports retournera null
        String view = controller.ajouterNiveauratique(new ExtendedModelMap(), session, 1L, NiveauPratique.DEBUTANT);
        assertEquals("redirect:/user/login", view);
    }

    @Test
    void testAfficherProfileMeteoGeocodingEmptyResults() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        // 1. IP API (Succès)
        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        ipApiResponse.put("city", "Ville-IP");
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);

        // 2. Geocoding retourne une liste vide (Coverage: results.isEmpty())
        Map<String, Object> geoResponse = new HashMap<>();
        geoResponse.put("results", new ArrayList<>());
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class))).thenReturn(geoResponse);

        // 3. Weather API
        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> currentWeather = new HashMap<>();
        currentWeather.put("temperature", 10.0);
        currentWeather.put("weathercode", 0);
        currentWeather.put("windspeed", 5.0);
        currentWeather.put("winddirection", 90);
        currentWeather.put("is_day", 1);
        weatherResponse.put("current_weather", currentWeather);
        when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(weatherResponse);

        Model model = new ExtendedModelMap();
        controller.afficherProfile(1L, session, model);

        // Vérifie qu'il fallback bien sur la ville de l'IP
        assertEquals("Ville-IP", model.getAttribute("meteoVille"));
    }

    @Test
    void testAfficherProfileMeteoMissingKeys() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        Model model = new ExtendedModelMap();

        // SCENARIO 1 : IP API retourne une map vide (sans lat ni lon)
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(new HashMap<>());
        controller.afficherProfile(1L, session, model);
        assertEquals("Meteo indisponible", model.getAttribute("meteoTemperature"));

        // SCENARIO 2 : IP API Ok, mais Weather API ne retourne pas "current_weather"
        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class))).thenReturn(new HashMap<>());
        when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(new HashMap<>());

        controller.afficherProfile(1L, session, model);
        assertEquals("Meteo indisponible", model.getAttribute("meteoTemperature"));

        // SCENARIO 3 : Weather API retourne "current_weather" mais il manque la température
        Map<String, Object> badWeatherResponse = new HashMap<>();
        badWeatherResponse.put("current_weather", new HashMap<>()); // map interne vide
        when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(badWeatherResponse);

        controller.afficherProfile(1L, session, model);
        assertEquals("Meteo indisponible", model.getAttribute("meteoTemperature"));
    }

    @Test
    void testAfficherProfileAllWeatherCodesAndWindDirections() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class))).thenReturn(new HashMap<>());

        // Liste de tous les codes météo du switch pour forcer JaCoCo à valider toutes les branches
        int[] codes = {0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99, 999};

        for (int i = 0; i < codes.length; i++) {
            Map<String, Object> weatherResponse = new HashMap<>();
            Map<String, Object> currentWeather = new HashMap<>();

            currentWeather.put("temperature", 15.0);
            currentWeather.put("weathercode", codes[i]);
            currentWeather.put("windspeed", 10.0);

            // Fait varier la direction du vent pour couvrir tous les cas (0, 45, 90, 135, etc...)
            currentWeather.put("winddirection", (i * 45) % 360);

            // Alterne Jour (1) et Nuit (0) pour tester l'icone soleil/lune du code 0
            currentWeather.put("is_day", i % 2);

            weatherResponse.put("current_weather", currentWeather);
            when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class)))
                    .thenReturn(weatherResponse);

            Model model = new ExtendedModelMap();
            controller.afficherProfile(1L, session, model);

            // Si l'exécution arrive ici sans erreur, la branche du switch est couverte
            assertTrue(model.containsAttribute("meteoEtatCiel"));
        }
    }



}