package utcapitole.miage.projet_web.controller;

import java.util.List;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String showAddActiviteForm(Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(userSession.getId());
        model.addAttribute("activite", new Activite());
        model.addAttribute("sports", sportRepository.findAll());
        model.addAttribute("user", user);
        return "add-activite";
    }

    @PostMapping("/add-activite")
    public String addActivite(@ModelAttribute("activite") Activite activite, Model model, jakarta.servlet.http.HttpSession session) {
        
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login"; 
        }

        activite.setUtilisateur(user);
        activiteService.enregistrerActivite(activite);
        return "redirect:/user/profile/" + user.getId();
    }

    @GetMapping("/list")
    public String listActivites(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/user/login";
        }
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        List<Activite> activites = activiteService.getActivitesByUtilisateur(user);
        model.addAttribute("activites", activites);
        model.addAttribute("stats", activiteService.getStatsActivites(activites));
        return "activiteList";
    }
    
}
