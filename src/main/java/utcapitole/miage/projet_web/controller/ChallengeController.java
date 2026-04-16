package utcapitole.miage.projet_web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ChallengeService;
import utcapitole.miage.projet_web.model.jpa.SportRepository;

/**
 * Contrôleur gérant toutes les fonctionnalités liées aux challenges :
 * création, participation, suppression, modification, affichage du classement
 * et consultation de la liste des challenges.
 *
 * Toutes les routes sont préfixées par /challenge.
 */
@Controller
@RequestMapping("/challenge")
public class ChallengeController {

    /** Logger utilisé pour remplacer System.out et satisfaire SonarQube. */
    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

    private static final String REDIRECT_LOGIN = "redirect:/user/login";
    private static final String REDIRECT_CHALLENGE_LIST = "redirect:/challenge/list";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";

    private final ChallengeService challengeService;
    private final SportRepository sportRepository;

    /**
     * Constructeur du contrôleur des challenges.
     *
     * @param challengeService Service métier gérant les challenges.
     * @param sportRepository Repository permettant d'accéder aux sports.
     */
    public ChallengeController(ChallengeService challengeService, SportRepository sportRepository) {
        this.challengeService = challengeService;
        this.sportRepository = sportRepository;
    }

    /**
     * Affiche le classement d’un challenge donné.
     *
     * @param challengeId Identifiant du challenge.
     * @param model Modèle contenant la liste des participants classés.
     * @param session Session contenant l’utilisateur connecté.
     * @return La vue du classement ou une redirection vers la page de login.
     */
    @GetMapping("/{id}/classement")
    public String showClassement(@PathVariable("id") Long challengeId, Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("classementList", challengeService.getClassement(challengeId));
        return "challenge-classement";
    }

    /**
     * Affiche la liste de tous les challenges disponibles.
     *
     * @param model Modèle contenant la liste des challenges.
     * @param session Session contenant l’utilisateur connecté.
     * @return La vue listant les challenges ou une redirection vers la page de login.
     */
    @GetMapping("/list")
    public String listerChallenges(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute(ATTR_LOGGED_IN_USER) == null) return REDIRECT_LOGIN;

        model.addAttribute("challenges", challengeService.getAllChallenges());
        return "challenge-list";
    }

    /**
     * Affiche le formulaire de création d’un challenge.
     *
     * @param model Modèle contenant un challenge vide et la liste des sports.
     * @param session Session contenant l’utilisateur connecté.
     * @return La vue du formulaire de création ou une redirection vers la page de login.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute(ATTR_LOGGED_IN_USER) == null) return REDIRECT_LOGIN;

        model.addAttribute("challenge", new Challenge());
        model.addAttribute("sports", sportRepository.findAll());

        return "challenge-create";
    }

    /**
     * Traite la création d’un nouveau challenge.
     *
     * @param challenge Challenge soumis par l’utilisateur.
     * @param session Session contenant l’utilisateur connecté.
     * @return Redirection vers la liste des challenges.
     */
    @SuppressWarnings("java:S4684")
    @PostMapping("/create")
    public String createChallenge(@ModelAttribute("challenge") Challenge challenge, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) return REDIRECT_LOGIN;

        challengeService.creerChallenge(challenge, user);
        return REDIRECT_CHALLENGE_LIST;
    }

    /**
     * Permet à un utilisateur de rejoindre un challenge.
     *
     * @param challengeId Identifiant du challenge.
     * @param session Session contenant l’utilisateur connecté.
     * @return Redirection vers la page du classement du challenge.
     */
    @PostMapping("/{id}/join")
    public String rejoindreChallenge(@PathVariable("id") Long challengeId, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) return REDIRECT_LOGIN;

        try {
            challengeService.rejoindreChallenge(challengeId, user);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la participation au challenge: {}", e.getMessage());
        }

        return "redirect:/challenge/" + challengeId + "/classement";
    }

    /**
     * Supprime un challenge si l’utilisateur en est le créateur.
     *
     * @param challengeId Identifiant du challenge.
     * @param session Session contenant l’utilisateur connecté.
     * @return Redirection vers la liste des challenges.
     */
    @PostMapping("/{id}/delete")
    public String supprimerChallenge(@PathVariable("id") Long challengeId, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) return REDIRECT_LOGIN;

        try {
            challengeService.supprimerChallenge(challengeId, user);
        } catch (RuntimeException e) {
            logger.error("Erreur suppression: {}", e.getMessage());
        }
        return REDIRECT_CHALLENGE_LIST;
    }

    /**
     * Affiche le formulaire de modification d’un challenge.
     * Seul le créateur du challenge peut le modifier.
     *
     * @param challengeId Identifiant du challenge.
     * @param model Modèle contenant les données du challenge.
     * @param session Session contenant l’utilisateur connecté.
     * @return La vue de modification ou une redirection si l’accès est interdit.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long challengeId, Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) return REDIRECT_LOGIN;

        Challenge challenge = challengeService.getAllChallenges().stream()
                .filter(c -> c.getId().equals(challengeId))
                .findFirst().orElse(null);

        if (challenge == null || !challenge.getCreateur().getId().equals(user.getId())) {
            return REDIRECT_CHALLENGE_LIST;
        }

        model.addAttribute("challenge", challenge);
        return "challenge-edit";
    }

    /**
     * Traite la modification du titre d’un challenge.
     *
     * @param challengeId Identifiant du challenge.
     * @param nouveauTitre Nouveau titre du challenge.
     * @param session Session contenant l’utilisateur connecté.
     * @return Redirection vers la liste des challenges.
     */
    @PostMapping("/{id}/edit")
    public String modifierChallenge(@PathVariable("id") Long challengeId, @RequestParam("titre") String nouveauTitre, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) return REDIRECT_LOGIN;

        try {
            challengeService.modifierTitreChallenge(challengeId, nouveauTitre, user);
        } catch (RuntimeException e) {
            logger.error("Erreur modification: {}", e.getMessage());
        }
        return REDIRECT_CHALLENGE_LIST;
    }
}
