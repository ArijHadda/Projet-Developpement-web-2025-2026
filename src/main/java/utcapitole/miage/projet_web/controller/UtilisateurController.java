package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utcapitole.miage.projet_web.model.NiveauPratique;
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.SportNiveauPratique;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.SportNiveauPratiqueService;
import utcapitole.miage.projet_web.model.jpa.SportService;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.jpa.BadgeAttributionService;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;
import java.time.LocalDate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UtilisateurController {
    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private SportService sportService;
    @Autowired
    private SportNiveauPratiqueService sportNiveauPratiqueService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private BadgeAttributionService badgeAttributionService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        Optional<Utilisateur> userOpt = utilisateurService.findByMail(email);

        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getMdp())) {
            session.setAttribute("loggedInUser", userOpt.get());

            return "redirect:/user/profile/" + userOpt.get().getId();
        } else {
            model.addAttribute("error", "L'email ou le mot de passe est incorrect !");
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute Utilisateur utilisateur, Model model) {
        if (utilisateurService.findByMail(utilisateur.getMail()).isPresent()) {
            model.addAttribute("error", "Cet email est déjà utilisé !");
            return "register";
        }

        utilisateurService.registerUser(utilisateur);
        return "redirect:/user/login";
    }


    @GetMapping("/profile/{IdU}")
    public String afficherProfile(@PathVariable Long IdU, HttpSession session, Model model) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        Optional<Utilisateur> targetOpt = utilisateurService.findById(IdU);
        if (targetOpt.isEmpty()) {
            return "redirect:/user/login";
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(loggedInUser.getId());
        if (user == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("userProfile", user);
        return "profile";
    }

    @PostMapping("/profile/update/{IdU}")
    public String modifierProfile(@PathVariable Long IdU,@RequestParam String mailU,
                                  @RequestParam String sexeU,
                                  @RequestParam int ageU, @RequestParam float tailleU,
                                  @RequestParam float poidsU,@RequestParam String niveauPratique,HttpSession session){

        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";
        utilisateurService.modifierProfile(IdU,mailU,sexeU,ageU,tailleU,poidsU,niveauPratique);
        return "redirect:/user/profile/" + currentUser.getId();
    }

    /*@GetMapping("/profile/update/{IdU}")
    public String updateProfile(@PathVariable Long IdU, HttpSession session, Model model){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        Optional<Utilisateur> userOpt = utilisateurService.findById(IdU);
        if (userOpt.isPresent()) {
            model.addAttribute("userUpdate", userOpt.get());
            return "update";
        } else {
            return "redirect:/user/login";
        }

    }*/
    @GetMapping("/profile/update/{IdU}")
    public String updateProfile(@PathVariable Long IdU, HttpSession session, Model model){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        // Charger l'utilisateur AVEC ses sports
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(IdU);
        if (user == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("userUpdate", user);

        return "update";
    }

    // changer mot de passe
    @GetMapping("/profile/update-password/{IdU}")
    public String showUpdatePasswordForm(@PathVariable Long IdU, HttpSession session, Model model) {

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getId().equals(IdU)) {
            return "redirect:/user/login";
        }

        // envoyer userId a frontend
        model.addAttribute("userId", IdU);
        return "update-password";
    }

    @PostMapping("/profile/update-password/{IdU}")
    public String processUpdatePassword(@PathVariable Long IdU,
                                        @RequestParam String ancienMdp,
                                        @RequestParam String nouveauMdp,
                                        @RequestParam String confirmMdp,
                                        HttpSession session,
                                        Model model) {

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getId().equals(IdU)) {
            return "redirect:/user/login";
        }

        try {
            // essayer de changer mot de passe
            utilisateurService.changerMotDePasse(IdU, ancienMdp, nouveauMdp, confirmMdp);

            // change succes, retourne profile
            return "redirect:/user/profile/" + IdU + "?success=passwordChanged";

        } catch (IllegalArgumentException e) {
            // catch erreur (ex. ancienmdp!= input, nouveaumdp!=confirmmdp)
            // e.getMessage() -> le texte dans la methode changerMotDePasse (Service)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userId", IdU);
            return "update-password";
        } catch (Exception e) {
            // catch erreur surprise (ex. BD est perdu)
            model.addAttribute("error", "Une erreur inattendue est survenue.");
            model.addAttribute("userId", IdU);
            return "update-password";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }

    @GetMapping("/profile/voirUtilisateur")
    public String voirListUtilisateur(Model model, HttpSession session){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        List<Utilisateur> listU = utilisateurService.findAll();
        model.addAttribute("utiliste", listU);
        return "redirect:/user/ami/chercher";
    }

    @PostMapping("/admin/users/{idUtilisateur}/activites")
    public String enregistrerActiviteEtAttribuerBadges(@PathVariable Long idUtilisateur,
                                                        @RequestParam String type,
                                                        @RequestParam LocalDate date,
                                                        @RequestParam int duree,
                                                        @RequestParam double distance,
                                                        HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        Activite activite = new Activite();
        activite.setNom(type);
        activite.setDate(date);
        activite.setDuree(duree);
        activite.setDistance(distance);

        List<String> badgesAttribues = badgeAttributionService.enregistrerActiviteEtAttribuerBadges(idUtilisateur, activite);
        boolean badgeAttribue = !badgesAttribues.isEmpty();

        return "redirect:/user/profile/" + idUtilisateur + (badgeAttribue ? "?badge=attribue" : "");
    }

    @PostMapping("/admin/users/{idUtilisateur}/badges/auto")
    public String attribuerBadgesAutomatiques(@PathVariable Long idUtilisateur,
                                              HttpSession session) {
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        badgeAttributionService.attribuerBadgesAutomatiques(idUtilisateur);
        return "redirect:/user/profile/" + idUtilisateur;
    }
    @GetMapping("/profile/ajouterNivPratique")
    public String ajouterNivPratique(Model model, HttpSession session){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("sports", sportService.getAll());
        model.addAttribute("niveaux", NiveauPratique.values());
        return "setSportNivPratique";
    }
    @PostMapping("/nivPratique")
    public String ajouterNiveauratique(Model model, HttpSession session, @RequestParam Long sport,@RequestParam NiveauPratique niveau){
        Utilisateur sessionUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/user/login";
        }
        Utilisateur user = utilisateurService.getUtilisateurAvecSports(sessionUser.getId());
        if (user == null){ return "redirect:/user/login";}
        Sport sports = sportService.getById(sport);
        Optional<SportNiveauPratique> existing = sportNiveauPratiqueService.findByUtilisateurIdAndSportId(user.getId(),sport);
        if(existing.isPresent()){
            SportNiveauPratique sn = existing.get();
            sn.setNiveau(niveau);
            sportNiveauPratiqueService.save(sn);
        }
        else{
            SportNiveauPratique sn = new SportNiveauPratique();
            sn.setSport(sports);
            sn.setNiveau(niveau);
            sn.setUtilisateur(user);
            user.getListSportNivPratique().add(sn);
            utilisateurService.save(user);
        }
        return "redirect:/user/profile/"+ user.getId();
    }
    @GetMapping("/deleteSportNiveau/{idSn}")
    public String deleteSportNiveau(@PathVariable Long idSn, HttpSession session){

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        sportNiveauPratiqueService.deleteById(idSn);
        return "redirect:/user/profile/" + loggedInUser.getId();
    }


}
