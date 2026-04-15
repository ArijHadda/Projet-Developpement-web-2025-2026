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
import utcapitole.miage.projet_web.model.jpa.ActiviteService;
import utcapitole.miage.projet_web.model.jpa.ObjectifService;
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

    private SportService sportService;
    private SportNiveauPratiqueService sportNiveauPratiqueService;
    private ActiviteService activiteService;
    private ObjectifService objectifService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        utilisateurService = new FakeUtilisateurService();
        badgeService = new FakeBadgeAttributionService();
        sportService = mock(SportService.class);
        sportNiveauPratiqueService = mock(SportNiveauPratiqueService.class);
        activiteService = mock(ActiviteService.class);
        objectifService = mock(ObjectifService.class);

        // Injection propre par constructeur (répondant au standard java:S6813)
        controller = new UtilisateurController(
                utilisateurService,
                sportService,
                sportNiveauPratiqueService,
                new BCryptPasswordEncoder(),
                badgeService,
                activiteService,
                objectifService
        );

        restTemplate = mock(RestTemplate.class);
        setField(controller, "restTemplate", restTemplate);
    }

    @Test
    void testShowLoginAndRegisterForms() {
        assertEquals("login", controller.showLoginForm());

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

    // NOUVEAU TEST POUR LA COUVERTURE : Profil de l'ami
    @Test
    void testAfficherProfileAmi() {
        Utilisateur logged = user(1L, "moi@test.fr", "pwd");
        Utilisateur ami = user(2L, "ami@test.fr", "pwd");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        utilisateurService.byId.put(2L, ami);

        // Mock des activités de l'ami (plus de 5 pour couvrir la condition subList)
        List<Activite> acts = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            acts.add(new Activite());
        }
        when(activiteService.getActivitesByUtilisateur(ami)).thenReturn(acts);

        // Mock des objectifs
        when(objectifService.getObjectifsAvecProgression(ami)).thenReturn(new ArrayList<>());

        Model model = new ExtendedModelMap();
        String view = controller.afficherProfile(2L, session, model);

        assertEquals("ami-profile", view);
        assertTrue(model.containsAttribute("ami"));

        @SuppressWarnings("unchecked")
        List<Activite> recents = (List<Activite>) model.getAttribute("activitesRecentes");
        assertEquals(5, recents.size()); // Vérifie que subList a bien coupé à 5
    }

    @Test
    void testModifierProfile() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        String view = controller.modifierProfile(5L, "n@test.fr", "F", 20, 1.6f, 50f,  session);
        assertEquals("redirect:/user/profile/5", view);
        assertEquals("n@test.fr", utilisateurService.lastModifiedMail);

        // Test hacker
        String denied = controller.modifierProfile(99L, "n@test.fr", "F", 20, 1.6f, 50f, session);
        assertEquals("redirect:/user/login", denied);
    }

    @Test
    void testVoirListUtilisateurAndLogout() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        String view = controller.voirListUtilisateur(new ExtendedModelMap(), session);
        assertEquals("redirect:/user/ami/chercher", view);

        assertEquals("redirect:/user/login", controller.voirListUtilisateur(new ExtendedModelMap(), new MockHttpSession()));

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

        SportNiveauPratique existing = new SportNiveauPratique();
        when(sportNiveauPratiqueService.findByUtilisateurIdAndSportId(5L, 1L)).thenReturn(Optional.of(existing));

        String viewExisting = controller.ajouterNiveauratique(new ExtendedModelMap(), session, 1L, NiveauPratique.DEBUTANT);
        assertEquals("redirect:/user/profile/5", viewExisting);
        verify(sportNiveauPratiqueService).save(existing);

        when(sportNiveauPratiqueService.findByUtilisateurIdAndSportId(5L, 2L)).thenReturn(Optional.empty());
        String viewNew = controller.ajouterNiveauratique(new ExtendedModelMap(), session, 2L, NiveauPratique.EXPERT);
        assertEquals("redirect:/user/profile/5", viewNew);
        assertEquals(1, logged.getListSportNivPratique().size());
        assertTrue(utilisateurService.saved);

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

        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        ipApiResponse.put("city", "Toulouse");
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);

        Map<String, Object> geoResponse = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> cityData = new HashMap<>();
        cityData.put("name", "Toulouse-Centre");
        results.add(cityData);
        geoResponse.put("results", results);
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class))).thenReturn(geoResponse);

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

        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        ipApiResponse.put("city", "Toulouse-IP");
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);

        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class)))
                .thenThrow(new RuntimeException("API Geocoding Down"));

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

        @Override
        public List<utcapitole.miage.projet_web.model.Badge> getAllBadges() {
            return new ArrayList<>();
        }
    }

    @Test
    void testAjouterNiveauPratiqueUserNull() {
        Utilisateur logged = user(99L, "ghost@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        String view = controller.ajouterNiveauratique(new ExtendedModelMap(), session, 1L, NiveauPratique.DEBUTANT);
        assertEquals("redirect:/user/login", view);
    }

    @Test
    void testAfficherProfileMeteoGeocodingEmptyResults() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        ipApiResponse.put("city", "Ville-IP");
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);

        Map<String, Object> geoResponse = new HashMap<>();
        geoResponse.put("results", new ArrayList<>());
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class))).thenReturn(geoResponse);

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

        assertEquals("Ville-IP", model.getAttribute("meteoVille"));
    }

    @Test
    void testAfficherProfileMeteoMissingKeys() {
        Utilisateur logged = user(1L, "p@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);
        utilisateurService.byId.put(1L, logged);

        Model model = new ExtendedModelMap();

        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(new HashMap<>());
        controller.afficherProfile(1L, session, model);
        assertEquals("Meteo indisponible", model.getAttribute("meteoTemperature"));

        Map<String, Object> ipApiResponse = new HashMap<>();
        ipApiResponse.put("lat", 43.6);
        ipApiResponse.put("lon", 1.4);
        when(restTemplate.getForObject("http://ip-api.com/json/", Map.class)).thenReturn(ipApiResponse);
        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class))).thenReturn(new HashMap<>());
        when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class))).thenReturn(new HashMap<>());

        controller.afficherProfile(1L, session, model);
        assertEquals("Meteo indisponible", model.getAttribute("meteoTemperature"));

        Map<String, Object> badWeatherResponse = new HashMap<>();
        badWeatherResponse.put("current_weather", new HashMap<>());
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

        int[] codes = {0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99, 999};

        for (int i = 0; i < codes.length; i++) {
            Map<String, Object> weatherResponse = new HashMap<>();
            Map<String, Object> currentWeather = new HashMap<>();

            currentWeather.put("temperature", 15.0);
            currentWeather.put("weathercode", codes[i]);
            currentWeather.put("windspeed", 10.0);

            currentWeather.put("winddirection", (i * 45) % 360);

            currentWeather.put("is_day", i % 2);

            weatherResponse.put("current_weather", currentWeather);
            when(restTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class)))
                    .thenReturn(weatherResponse);

            Model model = new ExtendedModelMap();
            controller.afficherProfile(1L, session, model);

            assertTrue(model.containsAttribute("meteoEtatCiel"));
        }
    }
}