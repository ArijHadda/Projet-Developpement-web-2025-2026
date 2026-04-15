package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import utcapitole.miage.projet_web.model.NiveauPratique;
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.SportNiveauPratique;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.*;
import utcapitole.miage.projet_web.model.Activite;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/user")
@SuppressWarnings({"java:S4684", "java:S2441"}) // Suppression des alertes SonarQube sur DTO et Sérialisation
public class UtilisateurController {

    private static final String METEO_TEMPERATURE = "temperature";
    private static final String METEO_ICONE = "icone";
    private static final String METEO_VILLE = "ville";
    private static final String METEO_ETAT_CIEL = "etatCiel";
    private static final String METEO_VENT_VITESSE = "ventVitesse";
    private static final String METEO_VENT_DIRECTION = "ventDirection";
    private static final String METEO_MOMENT = "moment";

    private static final String REDIRECT_LOGIN = "redirect:/user/login";
    private static final String REDIRECT_USER_PROFILE = "redirect:/user/profile/";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";
    private static final String ATTR_ERROR = "error";
    private static final String VIEW_UPDATE_PASSWORD = "update-password";
    private static final String ATTR_USER_ID = "userId";

    // Variable non finale pour permettre l'injection Mock dans les tests
    private RestTemplate restTemplate = new RestTemplate();

    private final UtilisateurService utilisateurService;
    private final SportService sportService;
    private final SportNiveauPratiqueService sportNiveauPratiqueService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BadgeAttributionService badgeAttributionService;
    private final ActiviteService activiteService;
    private final ObjectifService objectifService;

    public UtilisateurController(UtilisateurService utilisateurService,
                                 SportService sportService,
                                 SportNiveauPratiqueService sportNiveauPratiqueService,
                                 BCryptPasswordEncoder passwordEncoder,
                                 BadgeAttributionService badgeAttributionService,
                                 ActiviteService activiteService,
                                 ObjectifService objectifService) {
        this.utilisateurService = utilisateurService;
        this.sportService = sportService;
        this.sportNiveauPratiqueService = sportNiveauPratiqueService;
        this.passwordEncoder = passwordEncoder;
        this.badgeAttributionService = badgeAttributionService;
        this.activiteService = activiteService;
        this.objectifService = objectifService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        Optional<Utilisateur> userOpt = utilisateurService.findByMail(email);

        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getMdp())) {
            session.setAttribute(ATTR_LOGGED_IN_USER, userOpt.get());
            return REDIRECT_USER_PROFILE + userOpt.get().getId();
        } else {
            model.addAttribute(ATTR_ERROR, "L'email ou le mot de passe est incorrect !");
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute Utilisateur utilisateur, Model model) {
        if (utilisateurService.findByMail(utilisateur.getMail()).isPresent()) {
            model.addAttribute(ATTR_ERROR, "Cet email est déjà utilisé !");
            return "register";
        }

        utilisateurService.registerUser(utilisateur);
        return REDIRECT_LOGIN;
    }

    // Renommage IdU en idU (SonarQube java:S117)
    @GetMapping("/profile/{idU}")
    public String afficherProfile(@PathVariable Long idU, HttpSession session, Model model) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null) {
            return REDIRECT_LOGIN;
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(idU);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        List<utcapitole.miage.projet_web.model.Badge> allBadges = badgeAttributionService.getAllBadges();
        model.addAttribute("allBadges", allBadges);

        if (loggedInUser.getId().equals(idU)) {
            Map<String, Object> infosMeteo = recupererInfosMeteo();
            model.addAttribute("meteoTemperature", infosMeteo.get(METEO_TEMPERATURE));
            model.addAttribute("meteoIcone", infosMeteo.get(METEO_ICONE));
            model.addAttribute("meteoVille", infosMeteo.get(METEO_VILLE));
            model.addAttribute("meteoEtatCiel", infosMeteo.get(METEO_ETAT_CIEL));
            model.addAttribute("meteoVentVitesse", infosMeteo.get(METEO_VENT_VITESSE));
            model.addAttribute("meteoVentDirection", infosMeteo.get(METEO_VENT_DIRECTION));
            model.addAttribute("meteoMoment", infosMeteo.get(METEO_MOMENT));
            model.addAttribute("userProfile", user);
            return "profile";
        }

        model.addAttribute("ami", user);
        List<Activite> activitesAmi = activiteService.getActivitesByUtilisateur(user);
        if (activitesAmi != null && activitesAmi.size() > 5) {
            activitesAmi = activitesAmi.subList(0, 5);
        }
        model.addAttribute("activitesRecentes", activitesAmi);
        model.addAttribute("objectifsEnCours", objectifService.getObjectifsAvecProgression(user));

        return "ami-profile";
    }

    private Map<String, Object> recupererInfosMeteo() {
        Map<String, Object> meteo = Map.of(
                METEO_TEMPERATURE, "Meteo indisponible",
                METEO_ICONE, "🌤️",
                METEO_VILLE, "Ville inconnue",
                METEO_ETAT_CIEL, "Indisponible",
                METEO_VENT_VITESSE, "-",
                METEO_VENT_DIRECTION, "-",
                METEO_MOMENT, "-"
        );

        try {
            Map<?, ?> localisation = restTemplate.getForObject("http://ip-api.com/json/", Map.class);
            if (localisation == null || localisation.get("lat") == null || localisation.get("lon") == null) {
                return meteo;
            }

            double latitude = ((Number) localisation.get("lat")).doubleValue();
            double longitude = ((Number) localisation.get("lon")).doubleValue();
            String ville = recupererVille(latitude, longitude, localisation.get("city"));

            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&current_weather=true";
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !(response.get("current_weather") instanceof Map<?, ?> currentWeather)) {
                return meteo;
            }

            Object temperature = currentWeather.get(METEO_TEMPERATURE);
            Object weatherCodeObj = currentWeather.get("weathercode");
            Object windSpeedObj = currentWeather.get("windspeed");
            Object windDirectionObj = currentWeather.get("winddirection");
            Object isDayObj = currentWeather.get("is_day");
            if (temperature == null || weatherCodeObj == null || windSpeedObj == null || windDirectionObj == null || isDayObj == null) {
                return meteo;
            }

            int weatherCode = ((Number) weatherCodeObj).intValue();
            double windSpeed = ((Number) windSpeedObj).doubleValue();
            int windDirection = ((Number) windDirectionObj).intValue();
            int isDay = ((Number) isDayObj).intValue();

            return Map.of(
                    METEO_TEMPERATURE, temperature + "°C",
                    METEO_ICONE, getWeatherIcon(weatherCode, isDay == 1),
                    METEO_VILLE, ville,
                    METEO_ETAT_CIEL, getWeatherLabel(weatherCode),
                    METEO_VENT_VITESSE, String.format("%.0f km/h", windSpeed),
                    METEO_VENT_DIRECTION, getWindDirectionLabel(windDirection) + " (" + windDirection + "°)",
                    METEO_MOMENT, isDay == 1 ? "Jour (1 = Oui)" : "Nuit (1 = Non)"
            );
        } catch (Exception e) {
            return meteo;
        }
    }

    private String recupererVille(double latitude, double longitude, Object fallbackCity) {
        try {
            String url = "https://geocoding-api.open-meteo.com/v1/reverse?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&count=1&language=fr&format=json";
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("results") instanceof List<?> results && !results.isEmpty()) {
                Object first = results.get(0);
                if (first instanceof Map<?, ?> villeData && villeData.get("name") != null) {
                    return villeData.get("name").toString();
                }
            }
        } catch (Exception ignored) {
            // Ignoré intentionnellement : en cas d'erreur de l'API, on utilisera la ville de secours (fallbackCity)
        }
        return fallbackCity != null ? fallbackCity.toString() : "Ville inconnue";
    }

    private String getWeatherLabel(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "Ensoleille (Code 0)";
            case 1 -> "Principalement degage";
            case 2 -> "Partiellement nuageux";
            case 3 -> "Couvert";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55 -> "Bruine";
            case 56, 57 -> "Bruine verglaçante";
            case 61, 63, 65 -> "Pluie";
            case 66, 67 -> "Pluie verglaçante";
            case 71, 73, 75, 77 -> "Neige";
            case 80, 81, 82 -> "Averses de pluie";
            case 85, 86 -> "Averses de neige";
            case 95 -> "Orage";
            case 96, 99 -> "Orage avec grele";
            default -> "Meteo inconnue (Code " + weatherCode + ")";
        };
    }

    private String getWeatherIcon(int weatherCode, boolean isDay) {
        if (weatherCode == 0) {
            return isDay ? "☀️" : "🌙";
        }
        return switch (weatherCode) {
            case 1, 2 -> "🌤️";
            case 3 -> "☁️";
            case 45, 48 -> "🌫️";
            case 51, 53, 55, 56, 57 -> "🌦️";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️";
            case 71, 73, 75, 77, 85, 86 -> "❄️";
            case 95, 96, 99 -> "⛈️";
            default -> "🌤️";
        };
    }

    private String getWindDirectionLabel(int degrees) {
        String[] directions = {"Nord", "Nord-Est", "Est", "Sud-Est", "Sud", "Sud-Ouest", "Ouest", "Nord-Ouest"};
        int index = (int) Math.round((degrees % 360) / 45.0) % 8;
        return directions[index];
    }

    @PostMapping("/profile/update/{idU}")
    public String modifierProfile(@PathVariable Long idU, @RequestParam String mailU,
                                  @RequestParam String sexeU,
                                  @RequestParam int ageU, @RequestParam float tailleU,
                                  @RequestParam float poidsU, HttpSession session) {

        Utilisateur currentUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentUser == null || !currentUser.getId().equals(idU)) {
            return REDIRECT_LOGIN;
        }
        utilisateurService.modifierProfile(idU, mailU, sexeU, ageU, tailleU, poidsU);
        return REDIRECT_USER_PROFILE + currentUser.getId();
    }

    @GetMapping("/profile/update/{idU}")
    public String updateProfile(@PathVariable Long idU, HttpSession session, Model model) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null || !loggedInUser.getId().equals(idU)) {
            return REDIRECT_LOGIN;
        }

        Utilisateur user = utilisateurService.getUtilisateurAvecSports(idU);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("userUpdate", user);
        return "update";
    }

    @GetMapping("/profile/update-password/{idU}")
    public String showUpdatePasswordForm(@PathVariable Long idU, HttpSession session, Model model) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null || !loggedInUser.getId().equals(idU)) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute(ATTR_USER_ID, idU);
        return VIEW_UPDATE_PASSWORD;
    }

    @PostMapping("/profile/update-password/{idU}")
    public String processUpdatePassword(@PathVariable Long idU,
                                        @RequestParam String ancienMdp,
                                        @RequestParam String nouveauMdp,
                                        @RequestParam String confirmMdp,
                                        HttpSession session,
                                        Model model) {

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null || !loggedInUser.getId().equals(idU)) {
            return REDIRECT_LOGIN;
        }

        try {
            utilisateurService.changerMotDePasse(idU, ancienMdp, nouveauMdp, confirmMdp);
            return REDIRECT_USER_PROFILE + idU + "?success=passwordChanged";
        } catch (IllegalArgumentException e) {
            model.addAttribute(ATTR_ERROR, e.getMessage());
            model.addAttribute(ATTR_USER_ID, idU);
            return VIEW_UPDATE_PASSWORD;
        } catch (Exception e) {
            model.addAttribute(ATTR_ERROR, "Une erreur inattendue est survenue.");
            model.addAttribute(ATTR_USER_ID, idU);
            return VIEW_UPDATE_PASSWORD;
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return REDIRECT_LOGIN;
    }

    @GetMapping("/profile/voirUtilisateur")
    public String voirListUtilisateur(Model model, HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null) {
            return REDIRECT_LOGIN;
        }
        List<Utilisateur> listU = utilisateurService.findAll();
        model.addAttribute("utiliste", listU);
        return "redirect:/user/ami/chercher";
    }

    @PostMapping("/admin/users/{idUtilisateur}/activites")
    public String enregistrerActiviteEtAttribuerBadges(@PathVariable Long idUtilisateur,
                                                       @RequestParam String type,
                                                       @RequestParam LocalDate date,
                                                       @RequestParam int duree,
                                                       @RequestParam double distance,
                                                       HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null) {
            return REDIRECT_LOGIN;
        }

        Activite activite = new Activite();
        activite.setNom(type);
        activite.setDate(date);
        activite.setDuree(duree);
        activite.setDistance(distance);

        List<String> badgesAttribues = badgeAttributionService.enregistrerActiviteEtAttribuerBadges(idUtilisateur, activite);
        boolean badgeAttribue = !badgesAttribues.isEmpty();

        return REDIRECT_USER_PROFILE + idUtilisateur + (badgeAttribue ? "?badge=attribue" : "");
    }

    @PostMapping("/admin/users/{idUtilisateur}/badges/auto")
    public String attribuerBadgesAutomatiques(@PathVariable Long idUtilisateur,
                                              HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null) {
            return REDIRECT_LOGIN;
        }

        badgeAttributionService.attribuerBadgesAutomatiques(idUtilisateur);
        return REDIRECT_USER_PROFILE + idUtilisateur;
    }

    @GetMapping("/profile/ajouterNivPratique")
    public String ajouterNivPratique(Model model, HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null) {
            return REDIRECT_LOGIN;
        }
        model.addAttribute("sports", sportService.getAll());
        model.addAttribute("niveaux", NiveauPratique.values());
        return "setSportNivPratique";
    }

    @PostMapping("/nivPratique")
    public String ajouterNiveauratique(Model model, HttpSession session, @RequestParam Long sport, @RequestParam NiveauPratique niveau) {
        Utilisateur sessionUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (sessionUser == null) {
            return REDIRECT_LOGIN;
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(sessionUser.getId());
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        Sport sports = sportService.getById(sport);
        Optional<SportNiveauPratique> existing = sportNiveauPratiqueService.findByUtilisateurIdAndSportId(user.getId(), sport);
        if (existing.isPresent()) {
            SportNiveauPratique sn = existing.get();
            sn.setNiveau(niveau);
            sportNiveauPratiqueService.save(sn);
        } else {
            SportNiveauPratique sn = new SportNiveauPratique();
            sn.setSport(sports);
            sn.setNiveau(niveau);
            sn.setUtilisateur(user);
            user.getListSportNivPratique().add(sn);
            utilisateurService.save(user);
        }
        return REDIRECT_USER_PROFILE + user.getId();
    }

    @GetMapping("/deleteSportNiveau/{idSn}")
    public String deleteSportNiveau(@PathVariable Long idSn, HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (loggedInUser == null) {
            return REDIRECT_LOGIN;
        }
        sportNiveauPratiqueService.deleteById(idSn);
        return REDIRECT_USER_PROFILE + loggedInUser.getId();
    }
}