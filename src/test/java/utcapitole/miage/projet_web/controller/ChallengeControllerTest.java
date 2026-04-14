package utcapitole.miage.projet_web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ChallengeService;
import utcapitole.miage.projet_web.model.jpa.SportRepository;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChallengeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChallengeService challengeService;

    @Mock
    private SportRepository sportRepository;

    @InjectMocks
    private ChallengeController challengeController;

    private MockHttpSession session;

    private Utilisateur mockUser;
    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new Utilisateur();
        mockUser.setId(1L);
        mockUser.setMail("test@test.com");

        mockMvc = MockMvcBuilders.standaloneSetup(challengeController).build();

        mockUser = new Utilisateur();
        mockUser.setId(1L);
        session = new MockHttpSession();
        session.setAttribute("loggedInUser", mockUser);
    }

    @Test
    void testListerChallenges() throws Exception {
        when(challengeService.getAllChallenges()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/challenge/list").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("challenge-list"))
                .andExpect(model().attributeExists("challenges"));
    }

    @Test
    void testShowClassement() throws Exception {
        when(challengeService.getClassement(10L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/challenge/10/classement").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("challenge-classement"))
                .andExpect(model().attributeExists("classementList"));

        verify(challengeService).getClassement(10L);
    }

    @Test
    void testShowCreateForm() throws Exception {
        when(sportRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/challenge/create").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("challenge-create"))
                .andExpect(model().attributeExists("challenge"))
                .andExpect(model().attributeExists("sports"));
    }

    @Test
    void testCreateChallengePost() throws Exception {
        mockMvc.perform(post("/challenge/create")
                        .session(session)
                        .param("titre", "Running Challenge"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/list"));

        verify(challengeService).creerChallenge(any(Challenge.class), eq(mockUser));
    }

    @Test
    void testRejoindreChallenge() throws Exception {
        mockMvc.perform(post("/challenge/10/join").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/10/classement"));

        verify(challengeService).rejoindreChallenge(10L, mockUser);
    }


    @Test
    void testSupprimerChallenge() throws Exception {
        mockMvc.perform(post("/challenge/10/delete").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/list"));

        verify(challengeService).supprimerChallenge(10L, mockUser);
    }

    @Test
    void testShowEditFormSuccess() throws Exception {
        Challenge mockChallenge = new Challenge();
        mockChallenge.setId(10L);
        mockChallenge.setCreateur(mockUser);

        when(challengeService.getAllChallenges()).thenReturn(Collections.singletonList(mockChallenge));

        mockMvc.perform(get("/challenge/10/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("challenge-edit"))
                .andExpect(model().attributeExists("challenge"));
    }

    @Test
    void testShowEditFormNotCreatorRedirects() throws Exception {
        Utilisateur otherUser = new Utilisateur();
        otherUser.setId(99L);

        Challenge mockChallenge = new Challenge();
        mockChallenge.setId(10L);
        mockChallenge.setCreateur(otherUser);

        when(challengeService.getAllChallenges()).thenReturn(Collections.singletonList(mockChallenge));

        mockMvc.perform(get("/challenge/10/edit").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/list"));
    }


    @Test
    void testModifierChallengePost() throws Exception {
        mockMvc.perform(post("/challenge/10/edit")
                        .session(session)
                        .param("titre", "Nouveau Titre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/list"));

        verify(challengeService).modifierTitreChallenge(10L, "Nouveau Titre", mockUser);
    }

    @Test
    void testUserNotLogginReternRedirectLogin() throws Exception {
        mockMvc.perform(get("/challenge/1/classement"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login"));
    }

    @Test
    void testRejoindreChallenge_RejoindreFail_CatchShouldThrowException() throws Exception {
        session.setAttribute("loggedInUser", mockUser);

        doThrow(new RuntimeException("Erreur interne"))
                .when(challengeService).rejoindreChallenge(10L, mockUser);

        mockMvc.perform(post("/challenge/10/join").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/10/classement"));

        verify(challengeService).rejoindreChallenge(10L, mockUser);

    }
    @Test
    void testSupprimerChallenge_SuppressionFail_CatchShouldThrowException() throws Exception {
        session.setAttribute("loggedInUser", mockUser);

        doThrow(new RuntimeException("Erreur interne"))
                .when(challengeService).supprimerChallenge(10L, mockUser);

        mockMvc.perform(post("/challenge/10/delete").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/list"));

        verify(challengeService).supprimerChallenge(10L, mockUser);

    }
    @Test
    void testModifierChallenge_ModifyFail_CatchShouldThrowException() throws Exception {
        session.setAttribute("loggedInUser", mockUser);

        doThrow(new RuntimeException("Erreur interne"))
                .when(challengeService).modifierTitreChallenge(10L, "Nouveau Titre",mockUser);

        mockMvc.perform(post("/challenge/10/edit").session(session).param("titre","Nouveau Titre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenge/list"));

        verify(challengeService).modifierTitreChallenge(10L,"Nouveau Titre", mockUser);

    }
}