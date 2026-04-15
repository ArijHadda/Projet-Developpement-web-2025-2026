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
import utcapitole.miage.projet_web.dto.ObjectifProgressDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/objectif")
public class ObjectifController {

    @Autowired
    private ObjectifService objectifService;

    @Autowired
    private SportService sportService;

    @GetMapping("/list")
    public String listerObjectifs(
            @RequestParam(required = false) Long sportId,
            @RequestParam(required = false) String frequence,
            Model model, 
            HttpSession session) {
        
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        // Récupérer tous les objectifs avec progression
        List<ObjectifProgressDTO> objectifsProgress = objectifService.getObjectifsAvecProgression(currentUser);
        
        // Appliquer les filtres
        if (sportId != null) {
            objectifsProgress = objectifsProgress.stream()
                    .filter(dto -> dto.getObjectif().getSport().getId().equals(sportId))
                    .collect(Collectors.toList());
        }
        
        if (frequence != null && !frequence.isEmpty()) {
            objectifsProgress = objectifsProgress.stream()
                    .filter(dto -> dto.getObjectif().getFrequence().toString().equals(frequence))
                    .collect(Collectors.toList());
        }
        
        // Ajouter les attributs au modèle
        model.addAttribute("objectifsProgress", objectifsProgress);
        model.addAttribute("sports", sportService.getAll());
        model.addAttribute("selectedSportId", sportId);
        model.addAttribute("selectedFrequence", frequence);
        
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
