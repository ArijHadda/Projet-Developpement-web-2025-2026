package utcapitole.miage.projet_web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/flux-amis")
    public String afficherFluxAmis(Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur currentSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentSession == null) {
            return "redirect:/user/login";
        }

        Utilisateur userDb = utilisateurService.findById(currentSession.getId()).get();

        List<Activite> flux = activiteService.getFluxActivitesAmis(userDb);

        model.addAttribute("fluxActivites", flux);

        return "fluxAmis";
    }

    @PostMapping("/flux/kudos/{id}")
    public String liker(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user != null) {
            activiteService.toggleKudos(id, user.getId());
        }
        // obtenir le site avant l'action et le retourner
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    @PostMapping("/flux/comment/{id}")
    public String commenter(@PathVariable Long id, @RequestParam String contenu, HttpSession session, HttpServletRequest request) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user != null && contenu != null && !contenu.trim().isEmpty()) {
            activiteService.ajouterCommentaire(id, user.getId(), contenu);
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/activite/flux-amis");
    }

    @GetMapping("/supprimer/{idActivite}")
    public String supprimerActivite(@PathVariable Long idActivite, HttpSession session, Model model) {
        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        activiteService.supprimer(idActivite);
        //model.addAttribute("msg", "Suppression reussie!!");
        return "redirect:/activite/list";
    }

    @GetMapping("/modifier/{idActivite}")
    public String ShowModifierActivite(@PathVariable Long idActivite, HttpSession session, Model model){
        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        Activite act = activiteService.getById(idActivite)
                .orElseThrow(() -> new RuntimeException("Activité introuvable"));
        model.addAttribute("activite",act);
        return "modifier-activite";
    }
    @PostMapping("/modifier/{idActivite}")
    public String modifierActivite(@PathVariable Long idActivite, HttpSession session, Model model,
                                   @RequestParam LocalDate date,
                                   @RequestParam int duree,
                                   @RequestParam double distance,
                                   @RequestParam int note){

        Utilisateur userSession = (Utilisateur) session.getAttribute("loggedInUser");
        if (userSession == null) {
            return "redirect:/user/login";
        }
        Activite act = activiteService.getById(idActivite)
                .orElseThrow(() -> new RuntimeException("Activité introuvable"));

        act.setDuree(duree);
        act.setDate(date);
        act.setDistance(distance);
        act.setNote(note);
        activiteService.enregistrerActivite(act);
        return "redirect:/activite/list";
    }
}
