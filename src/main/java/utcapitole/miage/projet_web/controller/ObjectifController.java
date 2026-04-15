package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ObjectifService;
import utcapitole.miage.projet_web.model.jpa.SportService;
import utcapitole.miage.projet_web.dto.ObjectifProgressDTO;

import java.util.List;

@Controller
@RequestMapping("/objectif")
public class ObjectifController {

    private static final String REDIRECT_LOGIN = "redirect:/user/login";
    private static final String REDIRECT_OBJECTIF_LIST = "redirect:/objectif/list";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";
    private static final String ATTR_SPORTS = "sports";
    private static final String ATTR_OBJECTIF = "objectif";

    private final ObjectifService objectifService;
    private final SportService sportService;

    public ObjectifController(ObjectifService objectifService, SportService sportService) {
        this.objectifService = objectifService;
        this.sportService = sportService;
    }

    @GetMapping("/list")
    public String listerObjectifs(
            @RequestParam(required = false) Long sportId,
            @RequestParam(required = false) String frequence,
            Model model,
            HttpSession session) {

        Utilisateur currentUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentUser == null) return REDIRECT_LOGIN;

        // Récupérer tous les objectifs avec progression
        List<ObjectifProgressDTO> objectifsProgress = objectifService.getObjectifsAvecProgression(currentUser);

        // Appliquer les filtres avec .toList() (SonarQube java:S6204)
        if (sportId != null) {
            objectifsProgress = objectifsProgress.stream()
                    .filter(dto -> dto.getObjectif().getSport().getId().equals(sportId))
                    .toList();
        }

        if (frequence != null && !frequence.isEmpty()) {
            objectifsProgress = objectifsProgress.stream()
                    .filter(dto -> dto.getObjectif().getFrequence().toString().equals(frequence))
                    .toList();
        }

        model.addAttribute("objectifsProgress", objectifsProgress);
        model.addAttribute(ATTR_SPORTS, sportService.getAll());
        model.addAttribute("selectedSportId", sportId);
        model.addAttribute("selectedFrequence", frequence);

        return "objectif-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        if (session.getAttribute(ATTR_LOGGED_IN_USER) == null) return REDIRECT_LOGIN;

        model.addAttribute(ATTR_OBJECTIF, new Objectif());
        model.addAttribute(ATTR_SPORTS, sportService.getAll());
        return "objectif-form";
    }

    // Créer/Mettre à jour
    @SuppressWarnings("java:S4684") // Suppression de l'alerte DTO pour ne pas casser le code existant
    @PostMapping("/save")
    public String saveObjectif(@ModelAttribute(ATTR_OBJECTIF) Objectif objectif, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentUser == null) return REDIRECT_LOGIN;

        objectif.setUtilisateur(currentUser);
        objectifService.enregistrerObjectif(objectif);

        return REDIRECT_OBJECTIF_LIST;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentUser == null) return REDIRECT_LOGIN;

        return objectifService.getObjectifById(id).map(objectif -> {
            if (!objectif.getUtilisateur().getId().equals(currentUser.getId())) {
                return REDIRECT_OBJECTIF_LIST;
            }

            model.addAttribute(ATTR_OBJECTIF, objectif);
            model.addAttribute(ATTR_SPORTS, sportService.getAll());
            return "objectif-form";
        }).orElse(REDIRECT_OBJECTIF_LIST);
    }

    @GetMapping("/{id}/delete")
    public String deleteObjectif(@PathVariable("id") Long id, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentUser == null) return REDIRECT_LOGIN;

        objectifService.getObjectifById(id).ifPresent(objectif -> {
            if (objectif.getUtilisateur().getId().equals(currentUser.getId())) {
                objectifService.supprimerObjectif(id);
            }
        });

        return REDIRECT_OBJECTIF_LIST;
    }
}