package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ObjectifService;
import utcapitole.miage.projet_web.model.jpa.SportService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObjectifControllerTest {

    @Mock
    private ObjectifService objectifService;

    @Mock
    private SportService sportService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ObjectifController objectifController;

    private Utilisateur mockUser;
    private Objectif mockObj;

    @BeforeEach
    void setUp() {
        mockUser = new Utilisateur();
        mockUser.setId(1L);

        mockObj = new Objectif();
        mockObj.setId(10L);
        mockObj.setUtilisateur(mockUser);
    }

    @Test
    void testListerObjectifs_RedirectIfNoSession() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        String view = objectifController.listerObjectifs(null, null, model, session);
        assertEquals("redirect:/user/login", view);
    }

    @Test
    void testListerObjectifs_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(objectifService.getObjectifsAvecProgression(mockUser)).thenReturn(Collections.emptyList());

        String view = objectifController.listerObjectifs(null, null, model, session);

        assertEquals("objectif-list", view);
        verify(model).addAttribute(eq("objectifsProgress"), anyList());
    }

    @Test
    void testShowCreateForm_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(sportService.getAll()).thenReturn(Collections.emptyList());

        String view = objectifController.showCreateForm(model, session);

        assertEquals("objectif-form", view);
        verify(model).addAttribute(eq("objectif"), any(Objectif.class));
        verify(model).addAttribute(eq("sports"), anyList());
    }

    @Test
    void testSaveObjectif() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        String view = objectifController.saveObjectif(mockObj, session);

        assertEquals("redirect:/objectif/list", view);
        assertEquals(mockUser, mockObj.getUtilisateur());
        verify(objectifService).enregistrerObjectif(mockObj);
    }

    @Test
    void testShowEditForm_BloqueAccesAUnAutreUtilisateur() {
        // L'utilisateur connecté est le N° 2
        Utilisateur hacker = new Utilisateur();
        hacker.setId(2L);
        when(session.getAttribute("loggedInUser")).thenReturn(hacker);

        // L'objectif appartient à l'utilisateur N° 1
        when(objectifService.getObjectifById(10L)).thenReturn(Optional.of(mockObj));

        String view = objectifController.showEditForm(10L, model, session);

        // Le hacker est redirigé vers sa propre liste
        assertEquals("redirect:/objectif/list", view);
        verify(model, never()).addAttribute(eq("objectif"), any());
    }

    @Test
    void testShowEditForm_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(objectifService.getObjectifById(10L)).thenReturn(Optional.of(mockObj));

        String view = objectifController.showEditForm(10L, model, session);

        assertEquals("objectif-form", view);
        verify(model).addAttribute("objectif", mockObj);
    }

    @Test
    void testDeleteObjectif_SupprimeSeulementSiProprietaire() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);
        when(objectifService.getObjectifById(10L)).thenReturn(Optional.of(mockObj));

        String view = objectifController.deleteObjectif(10L, session);

        assertEquals("redirect:/objectif/list", view);
        verify(objectifService).supprimerObjectif(10L);
    }

    @Test
    void testDeleteObjectif_HackerBloque() {
        Utilisateur hacker = new Utilisateur();
        hacker.setId(2L);
        when(session.getAttribute("loggedInUser")).thenReturn(hacker);
        when(objectifService.getObjectifById(10L)).thenReturn(Optional.of(mockObj));

        String view = objectifController.deleteObjectif(10L, session);

        assertEquals("redirect:/objectif/list", view);
        // On vérifie que la méthode de suppression n'a JAMAIS été appelée
        verify(objectifService, never()).supprimerObjectif(any());
    }

    @Test
    void testShowEditForm_ObjectifIntrouvable_RedirectsToList() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        when(objectifService.getObjectifById(10L)).thenReturn(Optional.empty());

        String returnValue = objectifController.showEditForm(10L,model,session);
        assertEquals("redirect:/objectif/list",returnValue);
        verify(model, never()).addAttribute(eq("objectif"), any());
        verify(model, never()).addAttribute(eq("sports"), any());
    }

    @Test
    void testListerObjectifs_AvecFiltres() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        // Création d'un faux DTO pour tester le passage dans les filtres (sportId et frequence)
        utcapitole.miage.projet_web.dto.ObjectifProgressDTO mockDto = mock(utcapitole.miage.projet_web.dto.ObjectifProgressDTO.class);
        Objectif mockObjectifFiltre = mock(Objectif.class);
        utcapitole.miage.projet_web.model.Sport mockSport = new utcapitole.miage.projet_web.model.Sport();
        mockSport.setId(99L);

        when(mockDto.getObjectif()).thenReturn(mockObjectifFiltre);
        when(mockObjectifFiltre.getSport()).thenReturn(mockSport);

        // FIX: Return the actual Enum value, not a String
        doReturn(utcapitole.miage.projet_web.model.Frequence.HEBDOMADAIRE).when(mockObjectifFiltre).getFrequence();

        when(objectifService.getObjectifsAvecProgression(mockUser)).thenReturn(java.util.List.of(mockDto));

        // Appel avec les paramètres de filtre renseignés
        String view = objectifController.listerObjectifs(99L, "HEBDOMADAIRE", model, session);

        assertEquals("objectif-list", view);
        verify(model).addAttribute(eq("objectifsProgress"), anyList());
    }

    @Test
    void testShowCreateForm_RedirectIfNoSession() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        String view = objectifController.showCreateForm(model, session);
        assertEquals("redirect:/user/login", view);
    }

    @Test
    void testSaveObjectif_RedirectIfNoSession() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        String view = objectifController.saveObjectif(mockObj, session);
        assertEquals("redirect:/user/login", view);
    }

    @Test
    void testShowEditForm_RedirectIfNoSession() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        String view = objectifController.showEditForm(10L, model, session);
        assertEquals("redirect:/user/login", view);
    }

    @Test
    void testDeleteObjectif_RedirectIfNoSession() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        String view = objectifController.deleteObjectif(10L, session);
        assertEquals("redirect:/user/login", view);
    }

    @Test
    void testDeleteObjectif_ObjectifIntrouvable() {
        when(session.getAttribute("loggedInUser")).thenReturn(mockUser);

        // Simuler le cas où l'objectif n'existe pas en base
        when(objectifService.getObjectifById(10L)).thenReturn(Optional.empty());

        String view = objectifController.deleteObjectif(10L, session);

        // Il doit juste rediriger gentiment sans rien supprimer
        assertEquals("redirect:/objectif/list", view);
        verify(objectifService, never()).supprimerObjectif(any());
    }
}