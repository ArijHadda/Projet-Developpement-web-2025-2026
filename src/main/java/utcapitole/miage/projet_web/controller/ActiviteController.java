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

/**
 * Contrôleur gérant toutes les opérations liées aux activités sportives :
 * ajout, modification, suppression, affichage, filtrage, statistiques,
 * flux des amis, commentaires et réactions (kudos).
 *
 * Ce contrôleur s'appuie sur :
 * - {@link ActiviteService} pour la gestion métier des activités
 * - {@link SportRepository} pour la récupération des sports disponibles
 * - {@link UtilisateurService} pour la gestion des utilisateurs
 * - {@link BadgeAttributionService} pour l'attribution automatique de badges
 *
 * Toutes les routes sont préfixées par /activite.
 */
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
    private final BadgeAttributionService badgeAttributionService;

    /**
     * Constructeur du contrôleur des activités.
     *
     * @param activiteService Service métier gérant les activités.
     * @param sportRepository Repository permettant d'accéder aux sports.
     * @param utilisateurService Service gérant les utilisateurs.
     * @param badgeAttributionService Service attribuant automatiquement les badges.
     */
    public ActiviteController(ActiviteService activiteService,
                              SportRepository sportRepository,
                              UtilisateurService utilisateurService, BadgeAttributionService badgeAttributionService) {
        this.activiteService = activiteService;
        this.sportRepository = sportRepository;
        this.utilisateurService = utilisateurService;
        this.badgeAttributionService = badgeAttributionService;
    }


    /**
     * Affiche le formulaire d'ajout d'une activité.
     *
     * @param model Modèle utilisé pour transmettre les données à la vue.
     * @param session Session HTTP contenant l'utilisateur connecté.
     * @return La vue du formulaire ou une redirection vers la page de login.
     */
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

    /**
     * Traite la soumission du formulaire d'ajout d'activité.
     * Vérifie la validité de la date et de la note, enregistre l'activité,
     * puis attribue automatiquement les badges éventuels.
     *
     * @param activite Activité soumise par l'utilisateur.
     * @param model Modèle pour renvoyer des erreurs éventuelles.
     * @param session Session contenant l'utilisateur connecté.
     * @return Redirection vers le profil utilisateur ou retour au formulaire en cas d'erreur.
     */
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

    /**
     * Affiche la liste filtrée et regroupée des activités de l'utilisateur.
     *
     * @param model Modèle contenant les données pour la vue.
     * @param session Session contenant l'utilisateur connecté.
     * @param periode Période de filtrage (7j, 30j, 12m, tout).
     * @param regroupement Type de regroupement (jour, semaine, mois).
     * @param sportId Filtre optionnel par sport.
     * @return La vue affichant la liste des activités.
     */
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

    /**
     * Vérifie si la période fournie est valide.
     *
     * @param periode Période à vérifier.
     * @return true si la période est reconnue, false sinon.
     */
    private boolean isPeriodeValide(String periode) {
        return "7j".equals(periode) || PERIODE_30J.equals(periode) || "12m".equals(periode) || "tout".equals(periode);
    }

    /**
     * Vérifie si le regroupement fourni est valide.
     *
     * @param regroupement Type de regroupement.
     * @return true si le regroupement est valide, false sinon.
     */
    private boolean isRegroupementValide(String regroupement) {
        return REGROUPEMENT_JOUR.equals(regroupement) || REGROUPEMENT_SEMAINE.equals(regroupement) || REGROUPEMENT_MOIS.equals(regroupement);
    }

    /**
     * Calcule la date de début correspondant à la période sélectionnée.
     *
     * @param periode Période choisie (7j, 30j, 12m, tout).
     * @return La date de début ou null si toutes les activités doivent être affichées.
     */
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

    /**
     * Construit les données nécessaires aux graphiques de progression :
     * labels, durées cumulées et calories cumulées.
     *
     * @param activites Liste des activités filtrées.
     * @param regroupement Type de regroupement (jour, semaine, mois).
     * @return Une map contenant les labels, durées et calories.
     */
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

    /**
     * Affiche le flux des activités des amis de l'utilisateur connecté.
     *
     * @param model Modèle contenant les activités des amis.
     * @param session Session contenant l'utilisateur connecté.
     * @return La vue du flux ou une redirection vers la page de login.
     */
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

    /**
     * Ajoute ou retire un kudos sur une activité.
     *
     * @param id Identifiant de l'activité.
     * @param session Session contenant l'utilisateur connecté.
     * @param request Requête HTTP pour récupérer la page précédente.
     * @return Redirection vers la page d'origine.
     */
    @PostMapping("/flux/kudos/{id}")
    public String liker(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user != null) {
            activiteService.toggleKudos(id, user.getId());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    /**
     * Ajoute un commentaire sur une activité.
     *
     * @param id Identifiant de l'activité.
     * @param contenu Contenu du commentaire.
     * @param session Session contenant l'utilisateur connecté.
     * @param request Requête HTTP pour récupérer la page précédente.
     * @return Redirection vers la page d'origine.
     */
    @PostMapping("/flux/comment/{id}")
    public String commenter(@PathVariable Long id, @RequestParam String contenu, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user != null && contenu != null && !contenu.trim().isEmpty()) {
            activiteService.ajouterCommentaire(id, user.getId(), contenu);
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    /**
     * Supprime une activité de l'utilisateur.
     *
     * @param idActivite Identifiant de l'activité à supprimer.
     * @param session Session contenant l'utilisateur connecté.
     * @param model Modèle pour la vue.
     * @return Redirection vers la liste des activités.
     */
    @GetMapping("/supprimer/{idActivite}")
    public String supprimerActivite(@PathVariable Long idActivite, HttpSession session, Model model) {
        Utilisateur userSession = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (userSession == null) {
            return REDIRECT_LOGIN;
        }
        activiteService.supprimer(idActivite);
        return "redirect:/activite/list";
    }

    /**
     * Affiche le formulaire de modification d'une activité.
     *
     * @param idActivite Identifiant de l'activité.
     * @param session Session contenant l'utilisateur connecté.
     * @param model Modèle contenant l'activité à modifier.
     * @return La vue de modification ou une redirection vers la page de login.
     */
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

    /**
     * Traite la modification d'une activité existante.
     * Vérifie la validité de la date et de la note avant enregistrement.
     *
     * @param idActivite Identifiant de l'activité.
     * @param session Session contenant l'utilisateur connecté.
     * @param model Modèle pour renvoyer les erreurs éventuelles.
     * @param date Nouvelle date.
     * @param duree Nouvelle durée.
     * @param distance Nouvelle distance (optionnelle).
     * @param niveauIntensite Nouveau niveau d'intensité (optionnel).
     * @param note Nouvelle note.
     * @return Redirection vers la liste des activités ou retour au formulaire en cas d'erreur.
     */
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