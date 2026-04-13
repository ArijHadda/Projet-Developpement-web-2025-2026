package utcapitole.miage.projet_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private SportRepository sportRepository;

    @GetMapping("/{id}/classement")
    public String showClassement(@PathVariable("id") Long challengeId, Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("classementList", challengeService.getClassement(challengeId));

        return "challenge-classement";
    }

    @GetMapping("/list")
    public String listerChallenges(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";

        model.addAttribute("challenges", challengeService.getAllChallenges());
        return "challenge-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";

        model.addAttribute("challenge", new Challenge());

        model.addAttribute("sports", sportRepository.findAll());

        return "challenge-create";
    }

    @PostMapping("/create")
    public String createChallenge(@ModelAttribute("challenge") Challenge challenge, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        challengeService.creerChallenge(challenge, user);
        return "redirect:/challenge/list";
    }

    @PostMapping("/{id}/join")
    public String rejoindreChallenge(@PathVariable("id") Long challengeId, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        try {
            challengeService.rejoindreChallenge(challengeId, user);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        return "redirect:/challenge/" + challengeId + "/classement";
    }

    @PostMapping("/{id}/delete")
    public String supprimerChallenge(@PathVariable("id") Long challengeId, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        try {
            challengeService.supprimerChallenge(challengeId, user);
        } catch (RuntimeException e) {
            System.out.println("Erreur suppression: " + e.getMessage());
        }
        return "redirect:/challenge/list";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long challengeId, Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        Challenge challenge = challengeService.getAllChallenges().stream()
                .filter(c -> c.getId().equals(challengeId))
                .findFirst().orElse(null);

        if (challenge == null || !challenge.getCreateur().getId().equals(user.getId())) {
            return "redirect:/challenge/list";
        }

        model.addAttribute("challenge", challenge);
        return "challenge-edit";
    }

    @PostMapping("/{id}/edit")
    public String modifierChallenge(@PathVariable("id") Long challengeId, @RequestParam("titre") String nouveauTitre, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        try {
            challengeService.modifierTitreChallenge(challengeId, nouveauTitre, user);
        } catch (RuntimeException e) {
            System.out.println("Erreur modification: " + e.getMessage());
        }
        return "redirect:/challenge/list";
    }
}
