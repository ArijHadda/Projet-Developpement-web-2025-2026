package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.DemandeAmiRepository;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

import java.util.List;

@Controller
@RequestMapping("/user/ami")
public class AmiController {

    private static final String REDIRECT_LOGIN = "redirect:/user/login";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";
    private static final String ATTR_ERROR = "error";
    private static final String STATUS_PENDING = "PENDING";

    private final UtilisateurService utilisateurService;
    private final DemandeAmiRepository demandeAmiRepository;

    public AmiController(UtilisateurService utilisateurService, DemandeAmiRepository demandeAmiRepository) {
        this.utilisateurService = utilisateurService;
        this.demandeAmiRepository = demandeAmiRepository;
    }

    // afficher tous les utilisateurs
    @GetMapping("/chercher")
    public String chercherAmis(@RequestParam(value = "motCle", required = false) String motCle,
                               Model model, HttpSession session) {
        Utilisateur current = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (current == null) return REDIRECT_LOGIN;

        Utilisateur userDb = utilisateurService.findById(current.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Utilisateur> listeAffichee;
        if (motCle != null && !motCle.trim().isEmpty()) {
            listeAffichee = utilisateurService.rechercherParNomOuPrenom(motCle.trim());
        } else {
            listeAffichee = utilisateurService.findAll();
        }

        // obtenir la liste des demande en attentes que j'ai envoyees
        List<DemandeAmi> mesDemandesEnvoyees = demandeAmiRepository.findByExpediteurAndStatut(userDb, STATUS_PENDING);

        // enregistrer just les id de les destinateurs afin de faciliter la gestion dans frontend
        List<Long> waitingIds = mesDemandesEnvoyees.stream()
                .map(d -> d.getDestinataire().getId())
                .toList();

        model.addAttribute("utiliste", listeAffichee);
        model.addAttribute("mesAmis", userDb.getAmis());
        model.addAttribute("waitingIds", waitingIds);
        model.addAttribute("motCle", motCle);

        return "usersList";
    }

    @PostMapping("/demander/{id}")
    public String demander(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Utilisateur current = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (current == null) return REDIRECT_LOGIN;

        try {
            utilisateurService.envoyerDemande(current.getId(), id);
            // redirectAttributes: pour afficher les messages apres autoriser la page
            redirectAttributes.addFlashAttribute("success", "Demande envoyée !");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return "redirect:/user/ami/chercher";
    }

    @GetMapping("/invitations")
    public String voirInvitations(Model model, HttpSession session) {
        Utilisateur current = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (current == null) return REDIRECT_LOGIN;

        Utilisateur userDb = utilisateurService.findById(current.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<DemandeAmi> list = demandeAmiRepository.findByDestinataireAndStatut(userDb, STATUS_PENDING);

        model.addAttribute("invitations", list);
        return "invitations";
    }

    @PostMapping("/accepter/{demandeId}")
    public String accepter(@PathVariable Long demandeId, HttpSession session, RedirectAttributes redirectAttributes) {

        Utilisateur current = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (current == null) return REDIRECT_LOGIN;

        try {
            utilisateurService.accepterDemande(demandeId);
            redirectAttributes.addFlashAttribute("accepter", "Demande acceptée ! Vous êtes maintenant amis.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, "Erreur : Cette demande n'existe plus ou a déjà été traitée.");
        }
        return "redirect:/user/ami/invitations";
    }

    @PostMapping("/refuser/{demandeId}")
    public String refuser(@PathVariable Long demandeId, HttpSession session, RedirectAttributes redirectAttributes) {

        Utilisateur current = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (current == null) return REDIRECT_LOGIN;

        try {
            utilisateurService.refuserDemande(demandeId);
            redirectAttributes.addFlashAttribute("refuser", "Demande refusée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, "Erreur lors du refus de la demande.");
        }
        return "redirect:/user/ami/invitations";
    }
}