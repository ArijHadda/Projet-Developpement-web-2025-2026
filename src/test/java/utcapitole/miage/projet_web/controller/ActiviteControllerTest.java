package utcapitole.miage.projet_web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ActiviteService;
import utcapitole.miage.projet_web.model.jpa.SportRepository;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActiviteControllerTest {

    @Mock
    private ActiviteService activiteService;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ActiviteController activiteController;

    private Utilisateur mockUser;
    private Activite mockActivite;

    @BeforeEach
    void setUp() {
        mockUser = new Utilisateur();
        mockUser.setId(1L);
        mockUser.setMail("test@test.com");

        mockActivite = new Activite();
        mockActivite.setId(100L);
        mockActivite.setNom("Running");
    }

    // showAddActiviteForm

    @Test
    void showAddActiviteForm_notLoggedIn_redirectsToLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = activiteController.showAddActiviteForm(model, session);

        assertEquals("redirect:/user/login", viewName);
        verifyNoInteractions(model);
    }

    @Test
    void showAddActiviteForm_loggedIn_returnsAddActiviteView() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());

        String viewName = activiteController.showAddActiviteForm(model, session);

        assertEquals("add-activite", viewName);
        verify(model).addAttribute(eq("activite"), any(Activite.class));
        verify(model).addAttribute(eq("sports"), any());
        verify(model).addAttribute("user", mockUser);
    }

    @Test
    void showAddActiviteForm_loggedIn_ajouteSportsAuModel() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        List<?> sports = Arrays.asList("Football", "Tennis");
        when(sportRepository.findAll()).thenReturn((List) sports);

        activiteController.showAddActiviteForm(model, session);

        verify(model).addAttribute("sports", sports);
    }

    // addActivite 

    @Test
    void addActivite_notLoggedIn_redirectsToLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = activiteController.addActivite(mockActivite, model, session);

        assertEquals("redirect:/user/login", viewName);
        verifyNoInteractions(activiteService);
    }

    @Test
    void addActivite_loggedIn_associeUtilisateurEtEnregistre() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        String viewName = activiteController.addActivite(mockActivite, model, session);

        // L'utilisateur doit être associé à l'activité avant la sauvegarde
        assertEquals(mockUser, mockActivite.getUtilisateur(),
                "L'activité doit être associée à l'utilisateur connecté");
        verify(activiteService).enregistrerActivite(mockActivite);
        assertEquals("redirect:/user/profile/1", viewName);
    }

    @Test
    void addActivite_loggedIn_redirectsToUserProfile() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        String viewName = activiteController.addActivite(mockActivite, model, session);

        assertEquals("redirect:/user/profile/" + mockUser.getId(), viewName);
    }

    // listActivites

    @Test
    void listActivites_notLoggedIn_redirectsToLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = activiteController.listActivites(model, session);

        assertEquals("redirect:/user/login", viewName);
        verifyNoInteractions(activiteService);
        verifyNoInteractions(model);
    }

    @Test
    void listActivites_loggedIn_addsActivitesToModelAndReturnsView() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        List<Activite> activites = Arrays.asList(mockActivite, new Activite());
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(activites);

        String viewName = activiteController.listActivites(model, session);

        assertEquals("activiteList", viewName);
        verify(activiteService).getActivitesByUtilisateur(mockUser);
        verify(model).addAttribute("activites", activites);
    }

    @Test
    void listActivites_loggedIn_listeVideEstGeree() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Collections.emptyList());

        String viewName = activiteController.listActivites(model, session);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute("activites", Collections.emptyList());
    }
}
