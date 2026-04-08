package utcapitole.miage.projet_web.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.DemandeAmiRepository;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

import java.util.List;

@Controller
@RequestMapping("/user/ami")
public class AmiController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private DemandeAmiRepository demandeAmiRepository;

    // afficher tous les utilisateurs
    @GetMapping("/chercher")
    public String chercherAmis(Model model, HttpSession session) {
        Utilisateur current = (Utilisateur) session.getAttribute("loggedInUser");
        if (current == null) return "redirect:/user/login";
        //userBase de donnees, current comme une image pour l'instant, il n'autorise pas quand on ajout une demande
        Utilisateur userDb = utilisateurService.findById(current.getId()).get();
        List<Utilisateur> tous = utilisateurService.findAll();

        // obtenir la liste des demande en attentes que j'ai envoyees
        List<DemandeAmi> mesDemandesEnvoyees = demandeAmiRepository.findByExpediteurAndStatut(userDb, "PENDING");

        // enregistrer just les id de les destinateurs afin de faciliter la gestion dans frontend
        List<Long> waitingIds = mesDemandesEnvoyees.stream()
                .map(d -> d.getDestinataire().getId())
                .toList();

        model.addAttribute("utiliste", tous);
        model.addAttribute("mesAmis", userDb.getAmis());
        model.addAttribute("waitingIds", waitingIds);

        return "usersList";
    }

    @PostMapping("/demander/{id}")
    public String demander(@PathVariable Long id, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Utilisateur current = (Utilisateur) session.getAttribute("loggedInUser");
        if (current == null) return "redirect:/user/login";

        try {
            utilisateurService.envoyerDemande(current.getId(), id);
            // redirectAttributes: pour afficher les messages apres autoriser la page
            redirectAttributes.addFlashAttribute("success", "Demande envoyée !");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/ami/chercher";
    }

    @GetMapping("/invitations")
    public String voirInvitations(Model model, HttpSession session) {
        Utilisateur current = (Utilisateur) session.getAttribute("loggedInUser");
        if (current == null) return "redirect:/user/login";

        Utilisateur userDb = utilisateurService.findById(current.getId()).get();
        List<DemandeAmi> list = demandeAmiRepository.findByDestinataireAndStatut(userDb, "PENDING");

        model.addAttribute("invitations", list);
        return "invitations";
    }

    @PostMapping("/accepter/{demandeId}")
    public String accepter(@PathVariable Long demandeId, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Utilisateur current = (Utilisateur) session.getAttribute("loggedInUser");
        if (current == null) return "redirect:/user/login";

        try {
            utilisateurService.accepterDemande(demandeId);
            redirectAttributes.addFlashAttribute("accepter", "Demande acceptée ! Vous êtes maintenant amis.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : Cette demande n'existe plus ou a déjà été traitée.");
        }
        return "redirect:/user/ami/invitations";
    }

    @PostMapping("/refuser/{demandeId}")
    public String refuser(@PathVariable Long demandeId, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Utilisateur current = (Utilisateur) session.getAttribute("loggedInUser");
        if (current == null) return "redirect:/user/login";

        try {
            utilisateurService.refuserDemande(demandeId);
            redirectAttributes.addFlashAttribute("refuser", "Demande refusée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du refus de la demande.");
        }

        return "redirect:/user/ami/invitations";
    }
}
