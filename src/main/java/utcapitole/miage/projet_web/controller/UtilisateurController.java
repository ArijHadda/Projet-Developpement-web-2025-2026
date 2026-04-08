package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UtilisateurController {
    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        Optional<Utilisateur> userOpt = utilisateurService.findByMailU(email);

        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getMdpU())) {
            System.out.println("coucou");
            session.setAttribute("loggedInUser", userOpt.get());

            return "redirect:/user/profile/" + userOpt.get().getIdU();
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
        if (utilisateurService.findByMailU(utilisateur.getMailU()).isPresent()) {
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

        Optional<Utilisateur> userOpt = utilisateurService.findByIdU(IdU);
        if (userOpt.isPresent()) {
            model.addAttribute("userProfile", userOpt.get());
            return "profile";
        } else {
            return "redirect:/user/login";
        }
    }

    @PostMapping("/profile/update/{IdU}")
    public String modifierProfile(@PathVariable Long IdU,@RequestParam String mailU,
                                  @RequestParam String sexeU,
                                  @RequestParam int ageU, @RequestParam float tailleU,
                                  @RequestParam float poidsU, HttpSession session){

        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";
        utilisateurService.modifierProfile(IdU,mailU,sexeU,ageU,tailleU,poidsU);
        return "redirect:/user/profile/" + currentUser.getIdU();
    }

    @GetMapping("/profile/update/{IdU}")
    public String updateProfile(@PathVariable Long IdU, HttpSession session, Model model){
        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        Optional<Utilisateur> userOpt = utilisateurService.findByIdU(IdU);
        if (userOpt.isPresent()) {
            model.addAttribute("userUpdate", userOpt.get());
            return "update";
        } else {
            return "redirect:/user/login";
        }

    }

    // changer mot de passe
    @GetMapping("/profile/update-password/{IdU}")
    public String showUpdatePasswordForm(@PathVariable Long IdU, HttpSession session, Model model) {

        Utilisateur loggedInUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getIdU().equals(IdU)) {
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
        if (loggedInUser == null || !loggedInUser.getIdU().equals(IdU)) {
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
}
