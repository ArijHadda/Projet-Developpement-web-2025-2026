package utcapitole.miage.projet_web.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ActiviteService;
import utcapitole.miage.projet_web.model.jpa.BadgeAttributionService;
import utcapitole.miage.projet_web.model.jpa.SportRepository;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

@Controller
@RequestMapping("/activite")
public class ActiviteController {

    private static final String REDIRECT_LOGIN = "redirect:/user/login";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";
    private static final String ATTR_ACTIVITE = "activite";
    private static final String ATTR_SPORTS = "sports";
    private static final String ATTR_TODAY = "today";
    private static final String ATTR_ERROR = "error";
    private static final String VIEW_ADD_ACTIVITE = "add-activite";
    private static final String VIEW_MODIFIER_ACTIVITE = "modifier-activite";

    private static final String REGROUPEMENT_SEMAINE = "semaine";
    private static final String REGROUPEMENT_MOIS = "mois";
    private static final String REGROUPEMENT_JOUR = "jour";
    private static final String PERIODE_30J = "30j";

    private final ActiviteService activiteService;
    private final SportRepository sportRepository;
    private final UtilisateurService utilisateurService;

    public ActiviteController(ActiviteService activiteService,
                              SportRepository sportRepository,
                              UtilisateurService utilisateurService) {
        this.activiteService = activiteService;
        this.sportRepository = sportRepository;
        this.utilisateurService = utilisateurService;
    }

    @Autowired
    private BadgeAttributionService badgeAttributionService;

    @GetMapping("/add-activite")
    public String showAddActiviteForm(Model model, HttpSession session) {
        Utilisateur userSession = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (userSession == null) {
            return REDIRECT_LOGIN;
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(userSession.getId());
        model.addAttribute(ATTR_ACTIVITE, new Activite());
        model.addAttribute(ATTR_SPORTS, sportRepository.findAll());
        model.addAttribute("user", user);
        model.addAttribute(ATTR_TODAY, LocalDate.now());
        return VIEW_ADD_ACTIVITE;
    }

    @SuppressWarnings("java:S4684") // Suppression de l'alerte DTO pour ne pas casser le code existant
    @PostMapping("/add-activite")
    public String addActivite(@ModelAttribute(ATTR_ACTIVITE) Activite activite, Model model, HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        if (activite.getDate() != null && activite.getDate().isAfter(LocalDate.now())) {
            model.addAttribute(ATTR_ERROR, "La date de l'activité ne peut pas être dans le futur.");
            model.addAttribute(ATTR_ACTIVITE, activite);
            model.addAttribute(ATTR_SPORTS, sportRepository.findAll());
            model.addAttribute("user", utilisateurService.getUtilisateurAvecSports(user.getId()));
            model.addAttribute(ATTR_TODAY, LocalDate.now());
            return VIEW_ADD_ACTIVITE;
        }

        if (activite.getNote() < 1 || activite.getNote() > 10) {
            model.addAttribute(ATTR_ERROR, "La note doit être comprise entre 1 et 10.");
            model.addAttribute(ATTR_ACTIVITE, activite);
            model.addAttribute(ATTR_SPORTS, sportRepository.findAll());
            model.addAttribute("user", utilisateurService.getUtilisateurAvecSports(user.getId()));
            model.addAttribute(ATTR_TODAY, LocalDate.now());
            return VIEW_ADD_ACTIVITE;
        }

        activite.setUtilisateur(user);
        activiteService.enregistrerActivite(activite);
        
        List<String> badgesAttribues = badgeAttributionService.attribuerBadgesAutomatiques(user.getId());
        boolean badgeAttribue = !badgesAttribues.isEmpty();
        
        return "redirect:/user/profile/" + user.getId() + (badgeAttribue ? "?badge=attribue" : "");
    }

    @GetMapping("/list")
    public String listActivites(Model model, HttpSession session,
                                @RequestParam(defaultValue = PERIODE_30J) String periode,
                                @RequestParam(defaultValue = REGROUPEMENT_JOUR) String regroupement,
                                @RequestParam(required = false) Long sportId) {
        if (session.getAttribute(ATTR_LOGGED_IN_USER) == null) {
            return REDIRECT_LOGIN;
        }

        if (!isPeriodeValide(periode)) {
            periode = PERIODE_30J;
        }
        if (!isRegroupementValide(regroupement)) {
            regroupement = REGROUPEMENT_JOUR;
        }

        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        List<Activite> activites = activiteService.getActivitesByUtilisateur(user);

        LocalDate dateDebut = calculerDateDebut(periode);
        List<Activite> activitesFiltrees = activites.stream()
                .filter(a -> dateDebut == null || (a.getDate() != null && !a.getDate().isBefore(dateDebut)))
                .filter(a -> sportId == null || (a.getSport() != null && sportId.equals(a.getSport().getId())))
                .toList();

        Map<String, Object> progression = construireProgression(activitesFiltrees, regroupement);

        model.addAttribute("activites", activitesFiltrees);
        model.addAttribute("stats", activiteService.getStatsActivites(activitesFiltrees));
        model.addAttribute(ATTR_SPORTS, sportRepository.findAll());
        model.addAttribute("selectedPeriode", periode);
        model.addAttribute("selectedRegroupement", regroupement);
        model.addAttribute("selectedSportId", sportId);
        model.addAttribute("chartLabels", progression.get("labels"));
        model.addAttribute("chartDurees", progression.get("durees"));
        model.addAttribute("chartCalories", progression.get("calories"));
        return "activiteList";
    }

    public String listActivites(Model model, HttpSession session) {
        return listActivites(model, session, PERIODE_30J, REGROUPEMENT_JOUR, null);
    }

    private boolean isPeriodeValide(String periode) {
        return "7j".equals(periode) || PERIODE_30J.equals(periode) || "12m".equals(periode) || "tout".equals(periode);
    }

    private boolean isRegroupementValide(String regroupement) {
        return REGROUPEMENT_JOUR.equals(regroupement) || REGROUPEMENT_SEMAINE.equals(regroupement) || REGROUPEMENT_MOIS.equals(regroupement);
    }

    private LocalDate calculerDateDebut(String periode) {
        LocalDate now = LocalDate.now();
        switch (periode) {
            case "7j":
                return now.minusDays(6);
            case PERIODE_30J:
                return now.minusDays(29);
            case "12m":
                return now.minusMonths(12).plusDays(1);
            default:
                return null;
        }
    }

    private Map<String, Object> construireProgression(List<Activite> activites, String regroupement) {
        DateTimeFormatter formatterJour = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter formatterMois = DateTimeFormatter.ofPattern("MM/yyyy");

        Map<LocalDate, double[]> agregats = new LinkedHashMap<>();

        List<Activite> triees = activites.stream()
                .filter(a -> a.getDate() != null)
                .sorted(Comparator.comparing(Activite::getDate))
                .toList();

        for (Activite activite : triees) {
            LocalDate cle;
            if (REGROUPEMENT_SEMAINE.equals(regroupement)) {
                cle = activite.getDate().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            } else if (REGROUPEMENT_MOIS.equals(regroupement)) {
                cle = activite.getDate().withDayOfMonth(1);
            } else {
                cle = activite.getDate();
            }

            double[] valeurs = agregats.computeIfAbsent(cle, k -> new double[]{0.0, 0.0});
            valeurs[0] += activite.getDuree();
            valeurs[1] += activite.getCaloriesConsommees();
        }

        List<String> labels = new ArrayList<>();
        List<Double> durees = new ArrayList<>();
        List<Integer> calories = new ArrayList<>();

        WeekFields weekFields = WeekFields.of(Locale.FRANCE);
        for (Map.Entry<LocalDate, double[]> entry : agregats.entrySet()) {
            LocalDate key = entry.getKey();
            String label;
            if (REGROUPEMENT_SEMAINE.equals(regroupement)) {
                int numeroSemaine = key.get(weekFields.weekOfWeekBasedYear());
                label = "S" + numeroSemaine + " (" + key.format(formatterJour) + ")";
            } else if (REGROUPEMENT_MOIS.equals(regroupement)) {
                label = key.format(formatterMois);
            } else {
                label = key.format(formatterJour);
            }
            labels.add(label);
            durees.add(Math.round(entry.getValue()[0] * 100.0) / 100.0);
            calories.add((int) Math.round(entry.getValue()[1]));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("durees", durees);
        result.put("calories", calories);
        return result;
    }

    @GetMapping("/flux-amis")
    public String afficherFluxAmis(Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur currentSession = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentSession == null) {
            return REDIRECT_LOGIN;
        }

        Utilisateur userDb = utilisateurService.findById(currentSession.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Activite> flux = activiteService.getFluxActivitesAmis(userDb);

        model.addAttribute("fluxActivites", flux);

        return "fluxAmis";
    }

    @PostMapping("/flux/kudos/{id}")
    public String liker(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user != null) {
            activiteService.toggleKudos(id, user.getId());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    @PostMapping("/flux/comment/{id}")
    public String commenter(@PathVariable Long id, @RequestParam String contenu, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user != null && contenu != null && !contenu.trim().isEmpty()) {
            activiteService.ajouterCommentaire(id, user.getId(), contenu);
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    @GetMapping("/supprimer/{idActivite}")
    public String supprimerActivite(@PathVariable Long idActivite, HttpSession session, Model model) {
        Utilisateur userSession = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (userSession == null) {
            return REDIRECT_LOGIN;
        }
        activiteService.supprimer(idActivite);
        return "redirect:/activite/list";
    }

    @GetMapping("/modifier/{idActivite}")
    public String showModifierActivite(@PathVariable Long idActivite, HttpSession session, Model model) {
        Utilisateur userSession = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (userSession == null) {
            return REDIRECT_LOGIN;
        }
        Activite act = activiteService.getById(idActivite)
                .orElseThrow(() -> new RuntimeException("Activité introuvable"));
        model.addAttribute(ATTR_ACTIVITE, act);
        model.addAttribute(ATTR_TODAY, LocalDate.now());
        return VIEW_MODIFIER_ACTIVITE;
    }

    @PostMapping("/modifier/{idActivite}")
    public String modifierActivite(@PathVariable Long idActivite, HttpSession session, Model model,
                                   @RequestParam LocalDate date,
                                   @RequestParam int duree,
                                   @RequestParam(required = false) Double distance,
                                   @RequestParam(required = false) Integer niveauIntensite,
                                   @RequestParam int note) {
        Utilisateur userSession = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (userSession == null) {
            return REDIRECT_LOGIN;
        }
        Activite act = activiteService.getById(idActivite)
                .orElseThrow(() -> new RuntimeException("Activité introuvable"));

        if (date != null && date.isAfter(LocalDate.now())) {
            model.addAttribute(ATTR_ERROR, "La date de l'activité ne peut pas être dans le futur.");
            model.addAttribute(ATTR_ACTIVITE, act);
            model.addAttribute(ATTR_TODAY, LocalDate.now());
            return VIEW_MODIFIER_ACTIVITE;
        }

        if (note < 1 || note > 10) {
            model.addAttribute(ATTR_ERROR, "La note doit être comprise entre 1 et 10.");
            model.addAttribute(ATTR_ACTIVITE, act);
            model.addAttribute(ATTR_TODAY, LocalDate.now());
            return VIEW_MODIFIER_ACTIVITE;
        }

        act.setDuree(duree);
        act.setDate(date);
        if (distance != null) {
            act.setDistance(distance);
        }
        if (niveauIntensite != null) {
            act.setNiveauIntensite(niveauIntensite);
        }
        act.setNote(note);
        activiteService.enregistrerActivite(act);
        return "redirect:/activite/list";
    }
}