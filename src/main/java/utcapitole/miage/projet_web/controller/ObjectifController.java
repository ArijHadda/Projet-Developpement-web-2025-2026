package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ObjectifService;
import utcapitole.miage.projet_web.model.jpa.SportService;

import java.util.Optional;

@Controller
@RequestMapping("/objectif")
public class ObjectifController {

    @Autowired
    private ObjectifService objectifService;

    @Autowired
    private SportService sportService;

    @GetMapping("/list")
    public String listerObjectifs(Model model, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        model.addAttribute("objectifsProgress", objectifService.getObjectifsAvecProgression(currentUser));
        return "objectif-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";

        model.addAttribute("objectif", new Objectif());
        model.addAttribute("sports", sportService.getAll());
        return "objectif-form";
    }

    // Créer/Mettre à jour
    @PostMapping("/save")
    public String saveObjectif(@ModelAttribute("objectif") Objectif objectif, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        objectif.setUtilisateur(currentUser);
        objectifService.enregistrerObjectif(objectif);

        return "redirect:/objectif/list";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        Optional<Objectif> objectifOpt = objectifService.getObjectifById(id);
        if (objectifOpt.isPresent()) {
            Objectif objectif = objectifOpt.get();

            if (!objectif.getUtilisateur().getId().equals(currentUser.getId())) {
                return "redirect:/objectif/list";
            }

            model.addAttribute("objectif", objectif);
            model.addAttribute("sports", sportService.getAll());
            return "objectif-form";
        }
        return "redirect:/objectif/list";
    }

    @GetMapping("/{id}/delete")
    public String deleteObjectif(@PathVariable("id") Long id, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        Optional<Objectif> objectifOpt = objectifService.getObjectifById(id);
        if (objectifOpt.isPresent()) {
            Objectif objectif = objectifOpt.get();

            if (objectif.getUtilisateur().getId().equals(currentUser.getId())) {
                objectifService.supprimerObjectif(id);
            }
        }
        return "redirect:/objectif/list";
    }
}