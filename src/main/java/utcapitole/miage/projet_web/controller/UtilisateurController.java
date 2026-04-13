package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import utcapitole.miage.projet_web.model.NiveauPratique;
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.SportNiveauPratique;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.SportNiveauPratiqueService;
import utcapitole.miage.projet_web.model.jpa.SportService;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.jpa.BadgeAttributionService;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UtilisateurController {
    private static final String METEO_TEMPERATURE = "temperature";
    private static final String METEO_ICONE = "icone";
    private static final String METEO_VILLE = "ville";
    private static final String METEO_ETAT_CIEL = "etatCiel";
    private static final String METEO_VENT_VITESSE = "ventVitesse";
    private static final String METEO_VENT_DIRECTION = "ventDirection";
    private static final String METEO_MOMENT = "moment";

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private SportService sportService;
    @Autowired
    private SportNiveauPratiqueService sportNiveauPratiqueService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private BadgeAttributionService badgeAttributionService;

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
            session.setAttribute("loggedInUser", userOpt.get());

            return "redirect:/user/profile/" + userOpt.get().getId();
        } else {
            model.addAttribute("error", "L'email ou le mot de passe est incorrect !");
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
            model.addAttribute("error", "Cet email est déjà utilisé !");
            return "register";
        }

        utilisateurService.registerUser(utilisateur);
        return "redirect:/user/login";
    }


    @GetMapping("/profile/{IdU}")
    public String afficherProfile(@PathVariable Long IdU, HttpSession session, Model model) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(IdU);
        if (user == null) {
            return "redirect:/user/login";
        }

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
            // L'API de reverse geocoding peut échouer; on garde alors la ville issue de l'IP.
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

    @PostMapping("/profile/update/{IdU}")
    public String modifierProfile(@PathVariable Long IdU,@RequestParam String mailU,
                                  @RequestParam String sexeU,
                                  @RequestParam int ageU, @RequestParam float tailleU,
                                  @RequestParam float poidsU,@RequestParam String niveauPratique,HttpSession session){

        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null || !currentUser.getId().equals(IdU)) {
            return "redirect:/user/login";
        }
        utilisateurService.modifierProfile(IdU,mailU,sexeU,ageU,tailleU,poidsU,niveauPratique);
        return "redirect:/user/profile/" + currentUser.getId();
    }

    @GetMapping("/profile/update/{IdU}")
    public String updateProfile(@PathVariable Long IdU, HttpSession session, Model model){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getId().equals(IdU)) {
            return "redirect:/user/login";
        }

        // Charger l'utilisateur AVEC ses sports
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(IdU);
        if (user == null) {
            return "redirect:/user/login";
        }


        model.addAttribute("userUpdate", user);
        return "update";
    }

    // changer mot de passe
    @GetMapping("/profile/update-password/{IdU}")
    public String showUpdatePasswordForm(@PathVariable Long IdU, HttpSession session, Model model) {

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getId().equals(IdU)) {
            return "redirect:/user/login";
        }

        // envoyer userId a frontend
        model.addAttribute("userId", IdU);
        return "update-password";
    }

    @PostMapping("/profile/update-password/{IdU}")
    public String processUpdatePassword(@PathVariable Long IdU,
                                        @RequestParam String ancienMdp,
                                        @RequestParam String nouveauMdp,
                                        @RequestParam String confirmMdp,
                                        HttpSession session,
                                        Model model) {

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getId().equals(IdU)) {
            return "redirect:/user/login";
        }

        try {
            // essayer de changer mot de passe
            utilisateurService.changerMotDePasse(IdU, ancienMdp, nouveauMdp, confirmMdp);

            // change succes, retourne profile
            return "redirect:/user/profile/" + IdU + "?success=passwordChanged";

        } catch (IllegalArgumentException e) {
            // catch erreur (ex. ancienmdp!= input, nouveaumdp!=confirmmdp)
            // e.getMessage() -> le texte dans la methode changerMotDePasse (Service)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userId", IdU);
            return "update-password";
        } catch (Exception e) {
            // catch erreur surprise (ex. BD est perdu)
            model.addAttribute("error", "Une erreur inattendue est survenue.");
            model.addAttribute("userId", IdU);
            return "update-password";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }

    @GetMapping("/profile/voirUtilisateur")
    public String voirListUtilisateur(Model model, HttpSession session){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
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
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        Activite activite = new Activite();
        activite.setNom(type);
        activite.setDate(date);
        activite.setDuree(duree);
        activite.setDistance(distance);

        List<String> badgesAttribues = badgeAttributionService.enregistrerActiviteEtAttribuerBadges(idUtilisateur, activite);
        boolean badgeAttribue = !badgesAttribues.isEmpty();

        return "redirect:/user/profile/" + idUtilisateur + (badgeAttribue ? "?badge=attribue" : "");
    }

    @PostMapping("/admin/users/{idUtilisateur}/badges/auto")
    public String attribuerBadgesAutomatiques(@PathVariable Long idUtilisateur,
                                              HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        badgeAttributionService.attribuerBadgesAutomatiques(idUtilisateur);
        return "redirect:/user/profile/" + idUtilisateur;
    }
    @GetMapping("/profile/ajouterNivPratique")
    public String ajouterNivPratique(Model model, HttpSession session){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("sports", sportService.getAll());
        model.addAttribute("niveaux", NiveauPratique.values());
        return "setSportNivPratique";
    }
    @PostMapping("/nivPratique")
    public String ajouterNiveauratique(Model model, HttpSession session, @RequestParam Long sport,@RequestParam NiveauPratique niveau){
        Utilisateur sessionUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/user/login";
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(sessionUser.getId());
        if (user == null){ return "redirect:/user/login";}
        Sport sports = sportService.getById(sport);
        Optional<SportNiveauPratique> existing = sportNiveauPratiqueService.findByUtilisateurIdAndSportId(user.getId(),sport);
        if(existing.isPresent()){
            SportNiveauPratique sn = existing.get();
            sn.setNiveau(niveau);
            sportNiveauPratiqueService.save(sn);
        }
        else{
            SportNiveauPratique sn = new SportNiveauPratique();
            sn.setSport(sports);
            sn.setNiveau(niveau);
            sn.setUtilisateur(user);
            user.getListSportNivPratique().add(sn);
            utilisateurService.save(user);
        }
        return "redirect:/user/profile/"+ user.getId();
    }
    @GetMapping("/deleteSportNiveau/{idSn}")
    public String deleteSportNiveau(@PathVariable Long idSn, HttpSession session){

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        sportNiveauPratiqueService.deleteById(idSn);
        return "redirect:/user/profile/" + loggedInUser.getId();
    }


}
