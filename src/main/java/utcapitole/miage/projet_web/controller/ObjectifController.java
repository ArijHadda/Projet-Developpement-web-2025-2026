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

/**
 * Contrôleur gérant toutes les fonctionnalités liées aux objectifs sportifs :
 * création, modification, suppression, filtrage et affichage de la progression.
 *
 * Toutes les routes sont préfixées par /objectif.
 */
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

    /**
     * Constructeur du contrôleur des objectifs.
     *
     * @param objectifService Service métier gérant les objectifs.
     * @param sportService Service permettant de récupérer les sports disponibles.
     */
    public ObjectifController(ObjectifService objectifService, SportService sportService) {
        this.objectifService = objectifService;
        this.sportService = sportService;
    }

    /**
     * Affiche la liste des objectifs de l'utilisateur, avec possibilité de filtrer
     * par sport ou par fréquence.
     *
     * @param sportId Identifiant du sport pour filtrer (optionnel).
     * @param frequence Fréquence de l'objectif (optionnelle).
     * @param model Modèle contenant les objectifs filtrés et les données associées.
     * @param session Session contenant l'utilisateur connecté.
     * @return La vue listant les objectifs ou une redirection vers la page de login.
     */
    @GetMapping("/list")
    public String listerObjectifs(
            @RequestParam(required = false) Long sportId,
            @RequestParam(required = false) String frequence,
            Model model,
            HttpSession session) {

        Utilisateur currentUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentUser == null) return REDIRECT_LOGIN;

        List<ObjectifProgressDTO> objectifsProgress = objectifService.getObjectifsAvecProgression(currentUser);

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

    /**
     * Affiche le formulaire de création d’un objectif.
     *
     * @param model Modèle contenant un objectif vide et la liste des sports.
     * @param session Session contenant l'utilisateur connecté.
     * @return La vue du formulaire ou une redirection vers la page de login.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        if (session.getAttribute(ATTR_LOGGED_IN_USER) == null) return REDIRECT_LOGIN;

        model.addAttribute(ATTR_OBJECTIF, new Objectif());
        model.addAttribute(ATTR_SPORTS, sportService.getAll());
        return "objectif-form";
    }

    /**
     * Enregistre un nouvel objectif ou met à jour un objectif existant.
     *
     * @param objectif Objectif soumis par l'utilisateur.
     * @param session Session contenant l'utilisateur connecté.
     * @return Redirection vers la liste des objectifs.
     */
    @SuppressWarnings("java:S4684")
    @PostMapping("/save")
    public String saveObjectif(@ModelAttribute(ATTR_OBJECTIF) Objectif objectif, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (currentUser == null) return REDIRECT_LOGIN;

        objectif.setUtilisateur(currentUser);
        objectifService.enregistrerObjectif(objectif);

        return REDIRECT_OBJECTIF_LIST;
    }

    /**
     * Affiche le formulaire de modification d’un objectif existant.
     * Vérifie que l'objectif appartient bien à l'utilisateur connecté.
     *
     * @param id Identifiant de l'objectif.
     * @param model Modèle contenant l'objectif et la liste des sports.
     * @param session Session contenant l'utilisateur connecté.
     * @return La vue du formulaire ou une redirection si l'accès est interdit.
     */
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

    /**
     * Supprime un objectif si celui-ci appartient à l'utilisateur connecté.
     *
     * @param id Identifiant de l'objectif à supprimer.
     * @param session Session contenant l'utilisateur connecté.
     * @return Redirection vers la liste des objectifs.
     */
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
