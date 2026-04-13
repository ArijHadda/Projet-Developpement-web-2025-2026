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
import org.springframework.beans.factory.annotation.Autowired;
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
import utcapitole.miage.projet_web.model.jpa.SportRepository;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

@Controller
@RequestMapping("/activite")
public class ActiviteController {

    @Autowired
    private ActiviteService activiteService;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/add-activite")
    public String showAddActiviteForm(Model model, HttpSession session) {
        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(userSession.getId());
        model.addAttribute("activite", new Activite());
        model.addAttribute("sports", sportRepository.findAll());
        model.addAttribute("user", user);
        model.addAttribute("today", LocalDate.now());
        return "add-activite";
    }

    @PostMapping("/add-activite")
    public String addActivite(@ModelAttribute("activite") Activite activite, Model model, HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        if (activite.getDate() != null && activite.getDate().isAfter(LocalDate.now())) {
            model.addAttribute("error", "La date de l'activité ne peut pas être dans le futur.");
            model.addAttribute("activite", activite);
            model.addAttribute("sports", sportRepository.findAll());
            model.addAttribute("user", utilisateurService.getUtilisateurAvecSports(user.getId()));
            model.addAttribute("today", LocalDate.now());
            return "add-activite";
        }

        if (activite.getNote() < 1 || activite.getNote() > 10) {
            model.addAttribute("error", "La note doit être comprise entre 1 et 10.");
            model.addAttribute("activite", activite);
            model.addAttribute("sports", sportRepository.findAll());
            model.addAttribute("user", utilisateurService.getUtilisateurAvecSports(user.getId()));
            model.addAttribute("today", LocalDate.now());
            return "add-activite";
        }

        activite.setUtilisateur(user);
        activiteService.enregistrerActivite(activite);
        return "redirect:/user/profile/" + user.getId();
    }

    @GetMapping("/list")
    public String listActivites(Model model, HttpSession session,
                                @RequestParam(defaultValue = "30j") String periode,
                                @RequestParam(defaultValue = "jour") String regroupement,
                                @RequestParam(required = false) Long sportId) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/user/login";
        }

        if (!isPeriodeValide(periode)) {
            periode = "30j";
        }
        if (!isRegroupementValide(regroupement)) {
            regroupement = "jour";
        }

        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        List<Activite> activites = activiteService.getActivitesByUtilisateur(user);

        LocalDate dateDebut = calculerDateDebut(periode);
        List<Activite> activitesFiltrees = activites.stream()
                .filter(a -> dateDebut == null || (a.getDate() != null && !a.getDate().isBefore(dateDebut)))
                .filter(a -> sportId == null || (a.getSport() != null && sportId.equals(a.getSport().getId())))
            .toList();

        Map<String, Object> progression = construireProgression(activitesFiltrees, regroupement);

        model.addAttribute("activites", activitesFiltrees);
        model.addAttribute("stats", activiteService.getStatsActivites(activitesFiltrees));
        model.addAttribute("sports", sportRepository.findAll());
        model.addAttribute("selectedPeriode", periode);
        model.addAttribute("selectedRegroupement", regroupement);
        model.addAttribute("selectedSportId", sportId);
        model.addAttribute("chartLabels", progression.get("labels"));
        model.addAttribute("chartDurees", progression.get("durees"));
        model.addAttribute("chartCalories", progression.get("calories"));
        return "activiteList";
    }

    public String listActivites(Model model, HttpSession session) {
        return listActivites(model, session, "30j", "jour", null);
    }

    private boolean isPeriodeValide(String periode) {
        return "7j".equals(periode) || "30j".equals(periode) || "12m".equals(periode) || "tout".equals(periode);
    }

    private boolean isRegroupementValide(String regroupement) {
        return "jour".equals(regroupement) || "semaine".equals(regroupement) || "mois".equals(regroupement);
    }

    private LocalDate calculerDateDebut(String periode) {
        LocalDate now = LocalDate.now();
        switch (periode) {
            case "7j":
                return now.minusDays(6);
            case "30j":
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
            if ("semaine".equals(regroupement)) {
                cle = activite.getDate().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            } else if ("mois".equals(regroupement)) {
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
            if ("semaine".equals(regroupement)) {
                int numeroSemaine = key.get(weekFields.weekOfWeekBasedYear());
                label = "S" + numeroSemaine + " (" + key.format(formatterJour) + ")";
            } else if ("mois".equals(regroupement)) {
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
        Utilisateur currentSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentSession == null) {
            return "redirect:/user/login";
        }

        Utilisateur userDb = utilisateurService.findById(currentSession.getId()).get();

        List<Activite> flux = activiteService.getFluxActivitesAmis(userDb);

        model.addAttribute("fluxActivites", flux);

        return "fluxAmis";
    }

    @PostMapping("/flux/kudos/{id}")
    public String liker(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user != null) {
            activiteService.toggleKudos(id, user.getId());
        }
        // obtenir le site avant l'action et le retourner
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    @PostMapping("/flux/comment/{id}")
    public String commenter(@PathVariable Long id, @RequestParam String contenu, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user != null && contenu != null && !contenu.trim().isEmpty()) {
            activiteService.ajouterCommentaire(id, user.getId(), contenu);
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    @GetMapping("/supprimer/{idActivite}")
    public String supprimerActivite(@PathVariable Long idActivite, HttpSession session, Model model) {
        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        activiteService.supprimer(idActivite);
        return "redirect:/activite/list";
    }

    @GetMapping("/modifier/{idActivite}")
    public String ShowModifierActivite(@PathVariable Long idActivite, HttpSession session, Model model) {
        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        Activite act = activiteService.getById(idActivite)
                .orElseThrow(() -> new RuntimeException("Activité introuvable"));
        model.addAttribute("activite", act);
        model.addAttribute("today", LocalDate.now());
        return "modifier-activite";
    }

    @PostMapping("/modifier/{idActivite}")
    public String modifierActivite(@PathVariable Long idActivite, HttpSession session, Model model,
                                   @RequestParam LocalDate date,
                                   @RequestParam int duree,
                                   @RequestParam(required = false) Double distance,
                                   @RequestParam(required = false) Integer niveauIntensite,
                                   @RequestParam int note) {
        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        Activite act = activiteService.getById(idActivite)
                .orElseThrow(() -> new RuntimeException("Activité introuvable"));

        if (date != null && date.isAfter(LocalDate.now())) {
            model.addAttribute("error", "La date de l'activité ne peut pas être dans le futur.");
            model.addAttribute("activite", act);
            model.addAttribute("today", LocalDate.now());
            return "modifier-activite";
        }

        if (note < 1 || note > 10) {
            model.addAttribute("error", "La note doit être comprise entre 1 et 10.");
            model.addAttribute("activite", act);
            model.addAttribute("today", LocalDate.now());
            return "modifier-activite";
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
