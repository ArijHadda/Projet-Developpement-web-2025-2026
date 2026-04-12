package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.BadgeAttributionService;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilisateurControllerTest {

    private UtilisateurController controller;
    private FakeUtilisateurService utilisateurService;
    private FakeBadgeAttributionService badgeService;

    @BeforeEach
    void setUp() {
        controller = new UtilisateurController();
        utilisateurService = new FakeUtilisateurService();
        badgeService = new FakeBadgeAttributionService();

        setField(controller, "utilisateurService", utilisateurService);
        setField(controller, "passwordEncoder", new BCryptPasswordEncoder());
        setField(controller, "badgeAttributionService", badgeService);
    }

    @Test
    void loginFlowSuccessAndFailure() {
        String plainPassword = "secret";
        Utilisateur user = user(1L, "a@test.fr", new BCryptPasswordEncoder().encode(plainPassword));
        utilisateurService.byMail.put("a@test.fr", user);

        HttpSession session = new MockHttpSession();
        Model model = new ExtendedModelMap();

        String success = controller.processLogin("a@test.fr", plainPassword, session, model);
        assertEquals("redirect:/user/profile/" + user.getId(), success);

        String failure = controller.processLogin("a@test.fr", "bad", new MockHttpSession(), new ExtendedModelMap());
        assertEquals("login", failure);
    }

    @Test
    void registerFlowDuplicateThenSuccess() {
        Utilisateur duplicate = user(2L, "dup@test.fr", "pwd");
        utilisateurService.byMail.put("dup@test.fr", duplicate);

        Model model = new ExtendedModelMap();
        String duplicateView = controller.processRegister(duplicate, model);
        assertEquals("register", duplicateView);
        assertEquals("Cet email est déjà utilisé !", model.getAttribute("error"));

        Utilisateur fresh = user(3L, "new@test.fr", "pwd");
        String success = controller.processRegister(fresh, new ExtendedModelMap());
        assertEquals("redirect:/user/login", success);
        assertEquals(fresh, utilisateurService.lastRegisteredUser);
    }

    @Test
    void profileAndUpdateRoutesCoverSessionAndNotFoundBranches() {
        Utilisateur logged = user(10L, "u@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        utilisateurService.byId.put(10L, logged);
        String profileOk = controller.afficherProfile(10L, session, new ExtendedModelMap());
        assertEquals("profile", profileOk);

        String profileMissing = controller.afficherProfile(999L, session, new ExtendedModelMap());
        assertEquals("redirect:/user/login", profileMissing);

        String updateOk = controller.updateProfile(10L, session, new ExtendedModelMap());
        assertEquals("update", updateOk);

        String updateMissing = controller.updateProfile(999L, session, new ExtendedModelMap());
        assertEquals("redirect:/user/login", updateMissing);

        String noSession = controller.afficherProfile(10L, new MockHttpSession(), new ExtendedModelMap());
        assertEquals("redirect:/user/login", noSession);
    }

    @Test
    void updatePasswordFlowCoversSuccessAndErrors() {
        Utilisateur logged = user(1L, "p@test.fr", "oldpwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        assertEquals("update-password", controller.showUpdatePasswordForm(1L, session, new ExtendedModelMap()));
        assertEquals("redirect:/user/login", controller.showUpdatePasswordForm(2L, session, new ExtendedModelMap()));

        utilisateurService.passwordException = null;
        String success = controller.processUpdatePassword(1L, "old", "new", "new", session, new ExtendedModelMap());
        assertEquals("redirect:/user/profile/1?success=passwordChanged", success);

        utilisateurService.passwordException = new IllegalArgumentException("Message test");
        Model model = new ExtendedModelMap();
        String illegal = controller.processUpdatePassword(1L, "old", "new", "new", session, model);
        assertEquals("update-password", illegal);
        assertEquals("Message test", model.getAttribute("error"));

        utilisateurService.passwordException = new RuntimeException("boom");
        Model model2 = new ExtendedModelMap();
        String generic = controller.processUpdatePassword(1L, "old", "new", "new", session, model2);
        assertEquals("update-password", generic);
        assertEquals("Une erreur inattendue est survenue.", model2.getAttribute("error"));
    }

   /* @Test
    void listUsersAndProfileUpdateAndLogout() {
        Utilisateur logged = user(5L, "l@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        utilisateurService.allUsers = List.of(logged, user(6L, "x@test.fr", "pwd"));
        String listView = controller.VoirListUtilisateur(new ExtendedModelMap(), session);
        assertEquals("redirect:/user/ami/chercher", listView);

        String update = controller.modifierProfile(5L, "n@test.fr", "F", 20, 1.6f, 50f, "debutant", session);
        assertEquals("redirect:/user/profile/5", update);
        assertEquals("n@test.fr", utilisateurService.lastModifiedMail);

        MockHttpSession logoutSession = new MockHttpSession();
        controller.logout(logoutSession);
        assertTrue(logoutSession.isInvalid());
    }
*/
    @Test
    void badgeEndpointsCoverSessionAndAwardBranches() {
        Utilisateur logged = user(8L, "b@test.fr", "pwd");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", logged);

        badgeService.autoBadgesToReturn = List.of();
        String noBadge = controller.enregistrerActiviteEtAttribuerBadges(
                8L, "Course", LocalDate.of(2026, 4, 1), 40, 9.0, session
        );
        assertEquals("redirect:/user/profile/8", noBadge);
        assertEquals("Course", badgeService.lastActivite.getNom());

        badgeService.autoBadgesToReturn = List.of("1er 10km");
        String withBadge = controller.enregistrerActiviteEtAttribuerBadges(
                8L, "Course", LocalDate.of(2026, 4, 1), 40, 10.0, session
        );
        assertEquals("redirect:/user/profile/8?badge=attribue", withBadge);

        String auto = controller.attribuerBadgesAutomatiques(8L, session);
        assertEquals("redirect:/user/profile/8", auto);

        String blocked = controller.attribuerBadgesAutomatiques(8L, new MockHttpSession());
        assertEquals("redirect:/user/login", blocked);
    }

    private Utilisateur user(Long id, String mail, String mdp) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setMail(mail);
        u.setMdp(mdp);
        return u;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Injection de test impossible pour le champ: " + fieldName, e);
        }
    }

    private static class FakeUtilisateurService extends UtilisateurService {
        Map<String, Utilisateur> byMail = new HashMap<>();
        Map<Long, Utilisateur> byId = new HashMap<>();
        List<Utilisateur> allUsers = new ArrayList<>();

        Utilisateur lastRegisteredUser;
        String lastModifiedMail;
        RuntimeException passwordException;

        FakeUtilisateurService() {
            super(null, new BCryptPasswordEncoder(), null);
        }

        @Override
        public Optional<Utilisateur> findByMail(String mailU) {
            return Optional.ofNullable(byMail.get(mailU));
        }

        @Override
        public Optional<Utilisateur> findById(Long idU) {
            return Optional.ofNullable(byId.get(idU));
        }

        @Override
        public Utilisateur registerUser(Utilisateur utilisateur) {
            this.lastRegisteredUser = utilisateur;
            return utilisateur;
        }

        @Override
        public Utilisateur modifierProfile(Long IdU, String mailU, String sexeU, int ageU, float tailleU, float poidsU, String niveauPratique) {
            this.lastModifiedMail = mailU;
            return byId.getOrDefault(IdU, new Utilisateur());
        }

        @Override
        public void changerMotDePasse(Long idU, String ancienMdp, String nouveauMdp, String confirmMdp) {
            if (passwordException != null) {
                throw passwordException;
            }
        }

        @Override
        public List<Utilisateur> findAll() {
            return allUsers;
        }

        @Override
        public Utilisateur getUtilisateurAvecSports(Long id) {
            // Retourne l'utilisateur s'il existe dans notre map de test, sinon null
            return byId.get(id);
        }
    }

    private static class FakeBadgeAttributionService extends BadgeAttributionService {
        List<String> autoBadgesToReturn = new ArrayList<>();
        Activite lastActivite;

        FakeBadgeAttributionService() {
            super(null, null, null);
        }

        @Override
        public List<String> enregistrerActiviteEtAttribuerBadges(Long utilisateurId, Activite activite) {
            this.lastActivite = activite;
            return autoBadgesToReturn;
        }

        @Override
        public List<String> attribuerBadgesAutomatiques(Long utilisateurId) {
            return autoBadgesToReturn;
        }
    }
}
