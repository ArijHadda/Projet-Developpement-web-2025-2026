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
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private UtilisateurService utilisateurService;

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
        mockActivite.setDate(LocalDate.now());
        mockActivite.setNote(5);
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
        when(utilisateurService.getUtilisateurAvecSports(mockUser.getId())).thenReturn(mockUser);

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
        when(utilisateurService.getUtilisateurAvecSports(mockUser.getId())).thenReturn(mockUser);

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

    @Test
    void addActivite_futureDate_resteSurLeFormulaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(utilisateurService.getUtilisateurAvecSports(mockUser.getId())).thenReturn(mockUser);

        Activite futureActivite = new Activite();
        futureActivite.setNom("Running");
        futureActivite.setDate(LocalDate.of(2027, 1, 1));
        futureActivite.setNote(5);

        String viewName = activiteController.addActivite(futureActivite, model, session);

        assertEquals("add-activite", viewName);
        verify(activiteService, never()).enregistrerActivite(any());
        verify(model).addAttribute(eq("error"), any());
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
    void listActivites_loggedIn_addsActivitesAndStatsToModel() {
        // Given
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        
        Activite a1 = new Activite();
        a1.setNom("Running");
        a1.setDate(java.time.LocalDate.now());
        a1.setDuree(45);
        
        List<Activite> activites = Arrays.asList(a1);
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(activites);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("count", 1);
        when(activiteService.getStatsActivites(activites)).thenReturn(mockStats);

        // When
        String viewName = activiteController.listActivites(model, session);

        // Then
        assertEquals("activiteList", viewName);
        verify(model).addAttribute("activites", activites);
        verify(model).addAttribute("stats", mockStats);
        
        // Vérification explicite pour Issue #62 (données présentes dans l'objet)
        assertEquals("Running", activites.get(0).getNom());
        assertEquals(45, activites.get(0).getDuree());
    }

    @Test
    void listActivites_loggedIn_listeVideEstGeree() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("activites"), any());
        verify(model).addAttribute(eq("stats"), any());
    }

    // addActivite note validation

    @Test
    void addActivite_noteInferieureA1_resteSurLeFormulaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(utilisateurService.getUtilisateurAvecSports(mockUser.getId())).thenReturn(mockUser);

        Activite badNoteActivite = new Activite();
        badNoteActivite.setNom("Running");
        badNoteActivite.setDate(LocalDate.now());
        badNoteActivite.setNote(0);

        String viewName = activiteController.addActivite(badNoteActivite, model, session);

        assertEquals("add-activite", viewName);
        verify(activiteService, never()).enregistrerActivite(any());
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void addActivite_noteSuperieureA10_resteSurLeFormulaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(utilisateurService.getUtilisateurAvecSports(mockUser.getId())).thenReturn(mockUser);

        Activite badNoteActivite = new Activite();
        badNoteActivite.setNom("Running");
        badNoteActivite.setDate(LocalDate.now());
        badNoteActivite.setNote(11);

        String viewName = activiteController.addActivite(badNoteActivite, model, session);

        assertEquals("add-activite", viewName);
        verify(activiteService, never()).enregistrerActivite(any());
        verify(model).addAttribute(eq("error"), any());
    }

    // supprimerActivite

    @Test
    void supprimerActivite_notLoggedIn_redirectsToLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = activiteController.supprimerActivite(1L, session, model);

        assertEquals("redirect:/user/login", viewName);
        verify(activiteService, never()).supprimer(any());
    }

    @Test
    void supprimerActivite_loggedIn_supprimeEtRedirige() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        String viewName = activiteController.supprimerActivite(1L, session, model);

        assertEquals("redirect:/activite/list", viewName);
        verify(activiteService).supprimer(1L);
    }

    // ShowModifierActivite (GET)

    @Test
    void showModifierActivite_notLoggedIn_redirectsToLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = activiteController.ShowModifierActivite(1L, session, model);

        assertEquals("redirect:/user/login", viewName);
    }

    @Test
    void showModifierActivite_loggedIn_afficheLeFormulaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(1L)).thenReturn(Optional.of(mockActivite));

        String viewName = activiteController.ShowModifierActivite(1L, session, model);

        assertEquals("modifier-activite", viewName);
        verify(model).addAttribute("activite", mockActivite);
        verify(model).addAttribute(eq("today"), any(LocalDate.class));
    }

    @Test
    void showModifierActivite_activiteIntrouvable_throwsException() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            activiteController.ShowModifierActivite(999L, session, model);
        });
    }

    // modifierActivite (POST)

    @Test
    void modifierActivite_notLoggedIn_redirectsToLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = activiteController.modifierActivite(
                1L, session, model, LocalDate.now(), 30, null, null, 5);

        assertEquals("redirect:/user/login", viewName);
        verify(activiteService, never()).enregistrerActivite(any());
    }

    @Test
    void modifierActivite_futureDate_resteSurLeFormulaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(1L)).thenReturn(Optional.of(mockActivite));

        String viewName = activiteController.modifierActivite(
                1L, session, model, LocalDate.of(2027, 1, 1), 30, null, null, 5);

        assertEquals("modifier-activite", viewName);
        verify(activiteService, never()).enregistrerActivite(any());
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void modifierActivite_noteInferieureA1_resteSurLeFormulaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(1L)).thenReturn(Optional.of(mockActivite));

        String viewName = activiteController.modifierActivite(
                1L, session, model, LocalDate.now(), 30, null, null, 0);

        assertEquals("modifier-activite", viewName);
        verify(activiteService, never()).enregistrerActivite(any());
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void modifierActivite_noteSuperieureA10_resteSurLeFormulaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(1L)).thenReturn(Optional.of(mockActivite));

        String viewName = activiteController.modifierActivite(
                1L, session, model, LocalDate.now(), 30, null, null, 11);

        assertEquals("modifier-activite", viewName);
        verify(activiteService, never()).enregistrerActivite(any());
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void modifierActivite_valide_enregistreEtRedirige() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(1L)).thenReturn(Optional.of(mockActivite));

        String viewName = activiteController.modifierActivite(
                1L, session, model, LocalDate.now(), 45, null, null, 7);

        assertEquals("redirect:/activite/list", viewName);
        verify(activiteService).enregistrerActivite(mockActivite);
        assertEquals(45, mockActivite.getDuree());
        assertEquals(7, mockActivite.getNote());
    }

    @Test
    void modifierActivite_avecDistanceEtIntensite() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(1L)).thenReturn(Optional.of(mockActivite));

        String viewName = activiteController.modifierActivite(
                1L, session, model, LocalDate.now(), 60, 5.5, 3, 8);

        assertEquals("redirect:/activite/list", viewName);
        verify(activiteService).enregistrerActivite(mockActivite);
        assertEquals(5.5, mockActivite.getDistance(), 0.01);
        assertEquals(3, mockActivite.getNiveauIntensite());
    }

    @Test
    void modifierActivite_activiteIntrouvable_throwsException() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            activiteController.modifierActivite(
                    999L, session, model, LocalDate.now(), 30, null, null, 5);
        });
    }
}
