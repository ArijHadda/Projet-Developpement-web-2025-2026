package utcapitole.miage.projet_web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.DemandeAmiRepository;
import utcapitole.miage.projet_web.model.jpa.UtilisateurService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@ExtendWith(MockitoExtension.class)
class AmiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private DemandeAmiRepository demandeAmiRepository;

    @InjectMocks
    private AmiController amiController;

    private MockHttpSession session;
    private Utilisateur mockCurrentUser;
    private Utilisateur mockUserDb;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(amiController)
                .setViewResolvers(viewResolver)
                .build();

        mockCurrentUser = new Utilisateur();
        mockCurrentUser.setId(1L);
        session = new MockHttpSession();
        session.setAttribute("loggedInUser", mockCurrentUser);

        mockUserDb = new Utilisateur();
        mockUserDb.setId(1L);
        mockUserDb.setAmis(new ArrayList<>());
    }

    @Test
    void testChercherAmisWithoutSessionRedirects() throws Exception {
        mockMvc.perform(get("/user/ami/chercher"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login"));
    }

    @Test
    void testChercherAmisWithoutMotCle() throws Exception {
        when(utilisateurService.findById(1L)).thenReturn(Optional.of(mockUserDb));
        when(utilisateurService.findAll()).thenReturn(new ArrayList<>());
        when(demandeAmiRepository.findByExpediteurAndStatut(mockUserDb, "PENDING")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/user/ami/chercher").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("usersList"))
                .andExpect(model().attributeExists("utiliste"))
                .andExpect(model().attributeExists("mesAmis"))
                .andExpect(model().attributeExists("waitingIds"));

        verify(utilisateurService).findAll();
        verify(utilisateurService, never()).rechercherParNomOuPrenom(anyString());
    }

    @Test
    void testChercherAmisWithMotCle() throws Exception {
        when(utilisateurService.findById(1L)).thenReturn(Optional.of(mockUserDb));
        when(utilisateurService.rechercherParNomOuPrenom("Jean")).thenReturn(new ArrayList<>());
        when(demandeAmiRepository.findByExpediteurAndStatut(mockUserDb, "PENDING")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/user/ami/chercher")
                        .param("motCle", "Jean")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("motCle", "Jean"));

        verify(utilisateurService).rechercherParNomOuPrenom("Jean");
    }

    @Test
    void testChercherAmisMapsWaitingIdsCorrectly() throws Exception {
        Utilisateur destinataire = new Utilisateur();
        destinataire.setId(2L);

        DemandeAmi demande = new DemandeAmi();
        demande.setDestinataire(destinataire);

        when(utilisateurService.findById(1L)).thenReturn(Optional.of(mockUserDb));
        when(demandeAmiRepository.findByExpediteurAndStatut(mockUserDb, "PENDING"))
                .thenReturn(Collections.singletonList(demande));

        mockMvc.perform(get("/user/ami/chercher").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("waitingIds", Collections.singletonList(2L)));
    }

    @Test
    void testDemanderAmiSuccess() throws Exception {
        mockMvc.perform(post("/user/ami/demander/2").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/ami/chercher"))
                .andExpect(flash().attribute("success", "Demande envoyée !"));

        verify(utilisateurService).envoyerDemande(1L, 2L);
    }

    @Test
    void testDemanderAmiExceptionHandled() throws Exception {
        doThrow(new IllegalArgumentException("Vous êtes déjà amis !"))
                .when(utilisateurService).envoyerDemande(1L, 2L);

        mockMvc.perform(post("/user/ami/demander/2").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/ami/chercher"))
                .andExpect(flash().attribute("error", "Vous êtes déjà amis !"));
    }

    @Test
    void testVoirInvitations() throws Exception {
        when(utilisateurService.findById(1L)).thenReturn(Optional.of(mockUserDb));
        when(demandeAmiRepository.findByDestinataireAndStatut(mockUserDb, "PENDING")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/user/ami/invitations").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("invitations"))
                .andExpect(model().attributeExists("invitations"));
    }

    @Test
    void testAccepterDemandeSuccess() throws Exception {
        mockMvc.perform(post("/user/ami/accepter/10").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/ami/invitations"))
                .andExpect(flash().attributeExists("accepter"));

        verify(utilisateurService).accepterDemande(10L);
    }

    @Test
    void testAccepterDemandeExceptionHandled() throws Exception {
        doThrow(new RuntimeException("Erreur database")).when(utilisateurService).accepterDemande(10L);

        mockMvc.perform(post("/user/ami/accepter/10").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/ami/invitations"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testRefuserDemandeSuccess() throws Exception {
        mockMvc.perform(post("/user/ami/refuser/10").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/ami/invitations"))
                .andExpect(flash().attributeExists("refuser"));

        verify(utilisateurService).refuserDemande(10L);
    }

    @Test
    void testRefuserDemandeShouldThrowExceptionError(){
        doThrow(new RuntimeException("Erreur interne"))
                .when(utilisateurService).refuserDemande(123L);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        String returnValue = amiController.refuser(100L,session,redirectAttributes);
        assertEquals("redirect:/user/ami/invitations",returnValue);
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("refuser"), any());
    }
}