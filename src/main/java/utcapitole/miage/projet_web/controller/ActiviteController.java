package utcapitole.miage.projet_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ActiviteService;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;
@Controller
@RequestMapping("/activite")
public class ActiviteController {

    @Autowired
    private ActiviteService activiteService;

    @GetMapping("/add-activite")
    public String showAddActiviteForm(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("activite", new Activite());
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
        model.addAttribute("activites", activiteService.getActivitesByUtilisateur(user));
        return "activiteList";
    }
    
}
