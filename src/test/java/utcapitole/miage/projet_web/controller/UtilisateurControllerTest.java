package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.BadgeAttributionService;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurControllerTest {

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private BadgeAttributionService badgeAttributionService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private UtilisateurController utilisateurController;

    private Utilisateur user(Long id) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setMail("user" + id + "@test.fr");
        utilisateur.setMdp("encoded");
        return utilisateur;
    }

    private static Stream<Arguments> loginFailureScenarios() {
        Utilisateur existing = new Utilisateur();
        existing.setMdp("encoded");
        return Stream.of(
                Arguments.of(Optional.empty(), false),
                Arguments.of(Optional.of(existing), false)
        );
    }

    private static Stream<Arguments> passwordUpdateErrorScenarios() {
        return Stream.of(
                Arguments.of(new IllegalArgumentException("Message test"), "Message test"),
                Arguments.of(new RuntimeException("DB down"), "Une erreur inattendue est survenue.")
        );
    }

    private static Stream<Arguments> activityAwardScenarios() {
        return Stream.of(
                Arguments.of(List.of(), "redirect:/user/profile/10"),
                Arguments.of(List.of("1er 10km"), "redirect:/user/profile/10?badge=attribue")
        );
    }

    @Test
    void pagesStatiquesEtLogout() {
        assertEquals("login", utilisateurController.showLoginForm());
        assertEquals("redirect:/user/login", utilisateurController.logout(session));
        verify(session).invalidate();
    }

    @Test
    void registerFlow() {
        assertEquals("register", utilisateurController.showRegisterForm(model));
        verify(model).addAttribute(eq("utilisateur"), any(Utilisateur.class));

        Utilisateur duplicate = user(1L);
        when(utilisateurService.findByMailU(duplicate.getMail())).thenReturn(Optional.of(new Utilisateur()));
        assertEquals("register", utilisateurController.processRegister(duplicate, model));
        verify(model).addAttribute("error", "Cet email est déjà utilisé !");

        Utilisateur fresh = user(2L);
        when(utilisateurService.findByMailU(fresh.getMail())).thenReturn(Optional.empty());
        assertEquals("redirect:/user/login", utilisateurController.processRegister(fresh, model));
        verify(utilisateurService).registerUser(fresh);
    }

    @Test
    void loginSuccess() {
        Utilisateur found = user(1L);
        when(utilisateurService.findByMailU(found.getMail())).thenReturn(Optional.of(found));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        String view = utilisateurController.processLogin(found.getMail(), "secret", session, model);

        assertEquals("redirect:/user/profile/1", view);
        verify(session).setAttribute("loggedInUser", found);
    }

    @ParameterizedTest
    @MethodSource("loginFailureScenarios")
    void loginFailures(Optional<Utilisateur> foundUser, boolean passwordMatches) {
        when(utilisateurService.findByMailU("mail@test.fr")).thenReturn(foundUser);
        if (foundUser.isPresent()) {
            when(passwordEncoder.matches("secret", foundUser.get().getMdp())).thenReturn(passwordMatches);
        }

        String view = utilisateurController.processLogin("mail@test.fr", "secret", session, model);

        assertEquals("login", view);
        verify(model).addAttribute("error", "L'email ou le mot de passe est incorrect !");
    }

    @Test
    void toutesLesRoutesProtegeesSansSessionRedirigentLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        assertEquals("redirect:/user/login", utilisateurController.afficherProfile(1L, session, model));
        assertEquals("redirect:/user/login", utilisateurController.modifierProfile(1L, "m@test.fr", "F", 20, 1.65f, 55f, "debutant", session));
        assertEquals("redirect:/user/login", utilisateurController.updateProfile(1L, session, model));
        assertEquals("redirect:/user/login", utilisateurController.showUpdatePasswordForm(1L, session, model));
        assertEquals("redirect:/user/login", utilisateurController.processUpdatePassword(1L, "old", "new", "new", session, model));
        assertEquals("redirect:/user/login", utilisateurController.VoirListUtilisateur(model, session));
        assertEquals("redirect:/user/login", utilisateurController.enregistrerActiviteEtAttribuerBadges(10L, "Course", LocalDate.of(2026, 4, 1), 45, 9.0, session));
        assertEquals("redirect:/user/login", utilisateurController.attribuerBadgesAutomatiques(10L, session));

        verify(utilisateurService, never()).getAll();
        verify(badgeAttributionService, never()).enregistrerActiviteEtAttribuerBadges(any(), any());
        verify(badgeAttributionService, never()).attribuerBadgesAutomatiques(any());
    }

    @Test
    void afficherProfileEtUpdateProfileCouvrentBranchesPresentEtAbsent() {
        Utilisateur logged = user(1L);
        Utilisateur target = user(2L);
        when(session.getAttribute("loggedInUser")).thenReturn(logged);

        when(utilisateurService.findByIdU(2L)).thenReturn(Optional.of(target));
        assertEquals("profile", utilisateurController.afficherProfile(2L, session, model));
        verify(model).addAttribute("userProfile", target);

        when(utilisateurService.findByIdU(99L)).thenReturn(Optional.empty());
        assertEquals("redirect:/user/login", utilisateurController.afficherProfile(99L, session, model));

        when(utilisateurService.findByIdU(2L)).thenReturn(Optional.of(target));
        assertEquals("update", utilisateurController.updateProfile(2L, session, model));
        verify(model).addAttribute("userUpdate", target);

        when(utilisateurService.findByIdU(100L)).thenReturn(Optional.empty());
        assertEquals("redirect:/user/login", utilisateurController.updateProfile(100L, session, model));
    }

    @Test
    void modifierProfileEtVoirListeEtBadgesAutoSucces() {
        Utilisateur logged = user(7L);
        when(session.getAttribute("loggedInUser")).thenReturn(logged);

        String updateResult = utilisateurController.modifierProfile(1L, "new@test.fr", "M", 25, 1.8f, 70f, "intermediaire", session);
        assertEquals("redirect:/user/profile/7", updateResult);
        verify(utilisateurService).modifierProfile(1L, "new@test.fr", "M", 25, 1.8f, 70f, "intermediaire");

        List<Utilisateur> users = List.of(logged, user(2L));
        when(utilisateurService.getAll()).thenReturn(users);
        assertEquals("usersList", utilisateurController.VoirListUtilisateur(model, session));
        verify(model).addAttribute("utiliste", users);

        assertEquals("redirect:/user/profile/12", utilisateurController.attribuerBadgesAutomatiques(12L, session));
        verify(badgeAttributionService).attribuerBadgesAutomatiques(12L);
    }

    @Test
    void showUpdatePasswordFormBranches() {
        Utilisateur logged = user(5L);
        when(session.getAttribute("loggedInUser")).thenReturn(logged);

        assertEquals("redirect:/user/login", utilisateurController.showUpdatePasswordForm(1L, session, model));

        logged.setId(1L);
        assertEquals("update-password", utilisateurController.showUpdatePasswordForm(1L, session, model));
        verify(model).addAttribute("userId", 1L);
    }

    @Test
    void processUpdatePasswordSuccess() {
        Utilisateur logged = user(1L);
        when(session.getAttribute("loggedInUser")).thenReturn(logged);

        String view = utilisateurController.processUpdatePassword(1L, "old", "new", "new", session, model);

        assertEquals("redirect:/user/profile/1?success=passwordChanged", view);
        verify(utilisateurService).changerMotDePasse(1L, "old", "new", "new");
    }

    @ParameterizedTest
    @MethodSource("passwordUpdateErrorScenarios")
    void processUpdatePasswordErrorBranches(RuntimeException exception, String expectedMessage) {
        Utilisateur logged = user(1L);
        when(session.getAttribute("loggedInUser")).thenReturn(logged);
        doThrow(exception).when(utilisateurService).changerMotDePasse(1L, "old", "new", "new");

        String view = utilisateurController.processUpdatePassword(1L, "old", "new", "new", session, model);

        assertEquals("update-password", view);
        verify(model).addAttribute("error", expectedMessage);
        verify(model).addAttribute("userId", 1L);
    }

    @ParameterizedTest
    @MethodSource("activityAwardScenarios")
    void enregistrerActiviteEtAttribuerBadgesBranches(List<String> badges, String expectedRedirect) {
        Utilisateur logged = user(1L);
        when(session.getAttribute("loggedInUser")).thenReturn(logged);
        when(badgeAttributionService.enregistrerActiviteEtAttribuerBadges(eq(10L), any(Activite.class))).thenReturn(badges);

        String view = utilisateurController.enregistrerActiviteEtAttribuerBadges(
                10L, "Course", LocalDate.of(2026, 4, 1), 45, 9.0, session
        );

        assertEquals(expectedRedirect, view);

        ArgumentCaptor<Activite> captor = ArgumentCaptor.forClass(Activite.class);
        verify(badgeAttributionService).enregistrerActiviteEtAttribuerBadges(eq(10L), captor.capture());
        Activite activite = captor.getValue();
        assertEquals("Course", activite.getNom());
        assertEquals(LocalDate.of(2026, 4, 1), activite.getDate());
        assertEquals(45, activite.getDuree());
        assertEquals(9.0, activite.getDistance());
    }
}
