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

/**
 * Contrôleur gérant toutes les fonctionnalités liées aux relations d’amitié :
 * recherche d’utilisateurs, envoi de demandes d’amis, consultation des invitations,
 * acceptation et refus des demandes.
 *
 * Toutes les routes sont préfixées par /user/ami.
 */
@Controller
@RequestMapping("/user/ami")
public class AmiController {

    private static final String REDIRECT_LOGIN = "redirect:/user/login";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";
    private static final String ATTR_ERROR = "error";
    private static final String STATUS_PENDING = "PENDING";

    private final UtilisateurService utilisateurService;
    private final DemandeAmiRepository demandeAmiRepository;

    /**
     * Constructeur du contrôleur des relations d’amitié.
     *
     * @param utilisateurService Service gérant les utilisateurs et les demandes d’amis.
     * @param demandeAmiRepository Repository permettant d'accéder aux demandes d’amis.
     */
    public AmiController(UtilisateurService utilisateurService, DemandeAmiRepository demandeAmiRepository) {
        this.utilisateurService = utilisateurService;
        this.demandeAmiRepository = demandeAmiRepository;
    }

    /**
     * Affiche la liste des utilisateurs, avec possibilité de filtrer par mot-clé.
     * Affiche également les demandes d’amis en attente envoyées par l’utilisateur.
     *
     * @param motCle Mot-clé optionnel pour filtrer par nom ou prénom.
     * @param model Modèle contenant les données pour la vue.
     * @param session Session contenant l’utilisateur connecté.
     * @return La vue affichant la liste des utilisateurs ou une redirection vers la page de login.
     */
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

        List<DemandeAmi> mesDemandesEnvoyees =
                demandeAmiRepository.findByExpediteurAndStatut(userDb, STATUS_PENDING);

        List<Long> waitingIds = mesDemandesEnvoyees.stream()
                .map(d -> d.getDestinataire().getId())
                .toList();

        model.addAttribute("utiliste", listeAffichee);
        model.addAttribute("mesAmis", userDb.getAmis());
        model.addAttribute("waitingIds", waitingIds);
        model.addAttribute("motCle", motCle);

        return "usersList";
    }

    /**
     * Envoie une demande d’amitié à un autre utilisateur.
     *
     * @param id Identifiant du destinataire.
     * @param session Session contenant l’utilisateur connecté.
     * @param redirectAttributes Permet d’afficher un message après redirection.
     * @return Redirection vers la page de recherche d’utilisateurs.
     */
    @PostMapping("/demander/{id}")
    public String demander(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Utilisateur current = (Utilisateur) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (current == null) return REDIRECT_LOGIN;

        try {
            utilisateurService.envoyerDemande(current.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Demande envoyée !");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return "redirect:/user/ami/chercher";
    }

    /**
     * Affiche les invitations d’amis reçues par l’utilisateur connecté.
     *
     * @param model Modèle contenant la liste des invitations.
     * @param session Session contenant l’utilisateur connecté.
     * @return La vue affichant les invitations ou une redirection vers la page de login.
     */
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

    /**
     * Accepte une demande d’amitié.
     *
     * @param demandeId Identifiant de la demande d’amitié.
     * @param session Session contenant l’utilisateur connecté.
     * @param redirectAttributes Permet d’afficher un message après redirection.
     * @return Redirection vers la page des invitations.
     */
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

    /**
     * Refuse une demande d’amitié.
     *
     * @param demandeId Identifiant de la demande d’amitié.
     * @param session Session contenant l’utilisateur connecté.
     * @param redirectAttributes Permet d’afficher un message après redirection.
     * @return Redirection vers la page des invitations.
     */
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
