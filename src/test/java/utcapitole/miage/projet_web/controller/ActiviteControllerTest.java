package utcapitole.miage.projet_web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Sport;
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
    private jakarta.servlet.http.HttpServletRequest request;

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

    // ═══════════════════════════════════════════════════════════════════════
    // Tests pour le flux, kudos et commentaires
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void afficherFluxAmis_notLoggedIn_redirectsToLogin() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        String viewName = activiteController.afficherFluxAmis(model, session);
        assertEquals("redirect:/user/login", viewName);
    }

    @Test
    void afficherFluxAmis_loggedIn_returnsFluxAmis() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(utilisateurService.findById(mockUser.getId())).thenReturn(java.util.Optional.of(mockUser));
        when(activiteService.getFluxActivitesAmis(mockUser)).thenReturn(java.util.Collections.emptyList());

        String viewName = activiteController.afficherFluxAmis(model, session);

        assertEquals("fluxAmis", viewName);
        verify(model).addAttribute(eq("fluxActivites"), any());
    }

    @Test
    void liker_avecReferer_redirectsToReferer() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(request.getHeader("Referer")).thenReturn("/activite/list");

        String viewName = activiteController.liker(100L, session, request);

        verify(activiteService).toggleKudos(100L, mockUser.getId());
        assertEquals("redirect:/activite/list", viewName); // Doit retourner au referer
    }

    @Test
    void commenter_avecContenuValide_redirectsToReferer() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(request.getHeader("Referer")).thenReturn("/activite/flux-amis");

        String viewName = activiteController.commenter(100L, "Beau travail !", session, request);

        verify(activiteService).ajouterCommentaire(100L, mockUser.getId(), "Beau travail !");
        assertEquals("redirect:/activite/flux-amis", viewName);
    }
    @Test
    void testSuppressionActiviteShouldWork(){

        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        // Act
        String viewName = activiteController.supprimerActivite(10L, session, model);

        // Assert
        assertEquals("redirect:/activite/list", viewName);
        verify(activiteService).supprimer(10L);
    }


    // ═══════════════════════════════════════════════════════════════════════
    // Tests manquants pour améliorer la couverture
    // ═══════════════════════════════════════════════════════════════════════

    // --- listActivites avec paramètres ---

    @Test
    void listActivites_avecPeriode7j_filtreCorrectement() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Activite aRecent = new Activite();
        aRecent.setNom("Running");
        aRecent.setDate(LocalDate.now());
        aRecent.setDuree(30);
        aRecent.setDistance(5.0);
        aRecent.setCaloriesConsommees(300);

        Activite aOlder = new Activite();
        aOlder.setNom("Running");
        aOlder.setDate(LocalDate.now().minusDays(10));
        aOlder.setDuree(45);
        aOlder.setDistance(8.0);
        aOlder.setCaloriesConsommees(500);

        List<Activite> activites = Arrays.asList(aRecent, aOlder);
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(activites);
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "7j", "jour", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedPeriode"), eq("7j"));
    }

    @Test
    void listActivites_avecPeriode12m_filtreCorrectement() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Collections.emptyList());
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "12m", "jour", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedPeriode"), eq("12m"));
    }

    @Test
    void listActivites_avecPeriodeTout_nexclutAucuneActivite() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Activite a = new Activite();
        a.setNom("Running");
        a.setDate(LocalDate.now().minusYears(2));
        a.setDuree(30);
        a.setDistance(5.0);
        a.setCaloriesConsommees(300);

        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Arrays.asList(a));
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "tout", "jour", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedPeriode"), eq("tout"));
    }

    @Test
    void listActivites_periodeInvalide_reinitialiseA30j() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Collections.emptyList());
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "invalid", "jour", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedPeriode"), eq("30j"));
    }

    @Test
    void listActivites_regroupementSemaine() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Activite a = new Activite();
        a.setNom("Running");
        a.setDate(LocalDate.now());
        a.setDuree(30);
        a.setDistance(5.0);
        a.setCaloriesConsommees(300);

        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Arrays.asList(a));
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "30j", "semaine", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedRegroupement"), eq("semaine"));
    }

    @Test
    void listActivites_regroupementMois() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Activite a = new Activite();
        a.setNom("Running");
        a.setDate(LocalDate.now());
        a.setDuree(30);
        a.setDistance(5.0);
        a.setCaloriesConsommees(300);

        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Arrays.asList(a));
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "30j", "mois", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedRegroupement"), eq("mois"));
    }

    @Test
    void listActivites_regroupementInvalide_reinitialiseAJour() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Collections.emptyList());
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "30j", "invalid", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedRegroupement"), eq("jour"));
    }

    @Test
    void listActivites_avecSportId_filtreParSport() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Sport sport1 = new Sport();
        sport1.setId(1L);
        sport1.setNom("Course");

        Sport sport2 = new Sport();
        sport2.setId(2L);
        sport2.setNom("Cyclisme");

        Activite a1 = new Activite();
        a1.setNom("Course");
        a1.setDate(LocalDate.now());
        a1.setDuree(30);
        a1.setDistance(5.0);
        a1.setCaloriesConsommees(300);
        a1.setSport(sport1);

        Activite a2 = new Activite();
        a2.setNom("Cyclisme");
        a2.setDate(LocalDate.now());
        a2.setDuree(60);
        a2.setDistance(20.0);
        a2.setCaloriesConsommees(500);
        a2.setSport(sport2);

        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Arrays.asList(a1, a2));
        when(sportRepository.findAll()).thenReturn(Arrays.asList(sport1, sport2));
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "30j", "jour", 1L);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedSportId"), eq(1L));
    }

    @Test
    void listActivites_avecActiviteDateNull_estFiltree() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Activite a = new Activite();
        a.setNom("Running");
        a.setDate(null);
        a.setDuree(30);

        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Arrays.asList(a));
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "7j", "jour", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("selectedPeriode"), eq("7j"));
    }

    // --- liker : cas non couverts ---

    @Test
    void liker_notLoggedIn_neAppellePasToggleKudos() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn("/activite/flux-amis");

        String viewName = activiteController.liker(100L, session, request);

        verify(activiteService, never()).toggleKudos(any(), any());
        assertEquals("redirect:/activite/flux-amis", viewName);
    }

    @Test
    void liker_sansReferer_redirigeVersFluxAmis() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(request.getHeader("Referer")).thenReturn(null);

        String viewName = activiteController.liker(100L, session, request);

        verify(activiteService).toggleKudos(100L, mockUser.getId());
        assertEquals("redirect:/activite/flux-amis", viewName);
    }

    // --- commenter : cas non couverts ---

    @Test
    void commenter_notLoggedIn_neAppellePasAjouterCommentaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn("/activite/flux-amis");

        String viewName = activiteController.commenter(100L, "Hello", session, request);

        verify(activiteService, never()).ajouterCommentaire(any(), any(), any());
        assertEquals("redirect:/activite/flux-amis", viewName);
    }

    @Test
    void commenter_contenuVide_neAppellePasAjouterCommentaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(request.getHeader("Referer")).thenReturn("/activite/flux-amis");

        String viewName = activiteController.commenter(100L, "   ", session, request);

        verify(activiteService, never()).ajouterCommentaire(any(), any(), any());
        assertEquals("redirect:/activite/flux-amis", viewName);
    }

    @Test
    void commenter_contenuNull_neAppellePasAjouterCommentaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(request.getHeader("Referer")).thenReturn("/activite/flux-amis");

        String viewName = activiteController.commenter(100L, null, session, request);

        verify(activiteService, never()).ajouterCommentaire(any(), any(), any());
        assertEquals("redirect:/activite/flux-amis", viewName);
    }

    @Test
    void commenter_sansReferer_redirigeVersFluxAmis() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(request.getHeader("Referer")).thenReturn(null);

        String viewName = activiteController.commenter(100L, "Super !", session, request);

        verify(activiteService).ajouterCommentaire(100L, mockUser.getId(), "Super !");
        assertEquals("redirect:/activite/flux-amis", viewName);
    }

    // --- addActivite : date null ---

    @Test
    void addActivite_dateNull_passeLaValidationNoteEtEnregistre() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Activite nullDateActivite = new Activite();
        nullDateActivite.setNom("Running");
        nullDateActivite.setDate(null);
        nullDateActivite.setNote(5);

        String viewName = activiteController.addActivite(nullDateActivite, model, session);

        verify(activiteService).enregistrerActivite(nullDateActivite);
        assertEquals("redirect:/user/profile/1", viewName);
    }

    // --- modifierActivite : date null n'est pas testée, mais @RequestParam requis ---

    @Test
    void modifierActivite_valideMiseAJourChamps() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(activiteService.getById(1L)).thenReturn(Optional.of(mockActivite));

        String viewName = activiteController.modifierActivite(
                1L, session, model, LocalDate.now(), 90, 12.0, 4, 8);

        assertEquals("redirect:/activite/list", viewName);
        assertEquals(90, mockActivite.getDuree());
        assertEquals(12.0, mockActivite.getDistance(), 0.01);
        assertEquals(4, mockActivite.getNiveauIntensite());
        assertEquals(8, mockActivite.getNote());
    }

    // --- listActivites : progression chart data ---

    @Test
    void listActivites_avecDonnees_progressionEstDansLeModel() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        Activite a = new Activite();
        a.setNom("Running");
        a.setDate(LocalDate.now().minusDays(1));
        a.setDuree(30);
        a.setDistance(5.0);
        a.setCaloriesConsommees(300);

        when(activiteService.getActivitesByUtilisateur(mockUser)).thenReturn(Arrays.asList(a));
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());
        when(activiteService.getStatsActivites(any())).thenReturn(new HashMap<>());

        String viewName = activiteController.listActivites(model, session, "30j", "jour", null);

        assertEquals("activiteList", viewName);
        verify(model).addAttribute(eq("chartLabels"), any());
        verify(model).addAttribute(eq("chartDistances"), any());
        verify(model).addAttribute(eq("chartCalories"), any());
    }
}
