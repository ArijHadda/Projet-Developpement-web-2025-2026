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

@Controller
@RequestMapping("/challenge")
public class ChallengeController {

    // corriger erreur de sonar : Logger pour remplacer System.out
    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

    private static final String REDIRECT_LOGIN = "redirect:/user/login";
    private static final String REDIRECT_CHALLENGE_LIST = "redirect:/challenge/list";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";

    private final ChallengeService challengeService;
    private final SportRepository sportRepository;

    public ChallengeController(ChallengeService challengeService, SportRepository sportRepository) {
        this.challengeService = challengeService;
        this.sportRepository = sportRepository;
    }

    @GetMapping("/{id}/classement")
    public String showClassement(@PathVariable("id") Long challengeId, Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("classementList", challengeService.getClassement(challengeId));

        return "challenge-classement";
    }

    @GetMapping("/list")
    public String listerChallenges(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute(ATTR_LOGGED_IN_USER) == null) return REDIRECT_LOGIN;

        model.addAttribute("challenges", challengeService.getAllChallenges());
        return "challenge-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute(ATTR_LOGGED_IN_USER) == null) return REDIRECT_LOGIN;

        model.addAttribute("challenge", new Challenge());
        model.addAttribute("sports", sportRepository.findAll());

        return "challenge-create";
    }

    @SuppressWarnings("java:S4684") // Suppression de l'alerte DTO pour ne pas casser le code existant
    @PostMapping("/create")
    public String createChallenge(@ModelAttribute("challenge") Challenge challenge, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) return REDIRECT_LOGIN;

        challengeService.creerChallenge(challenge, user);
        return REDIRECT_CHALLENGE_LIST;
    }

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