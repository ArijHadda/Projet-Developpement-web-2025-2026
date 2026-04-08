package utcapitole.miage.projet_web.model.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Mockito : creer des donnees mock, ne pas polluer BD, sinon chaque fois on lance TEST, il va creer une ligne dans BD
// @ExtendWith(MockitoExtension.class): activer Mockito
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    private static final Long CORRECT_ID = 1L;
    private static final String CORRECT_MAIL = "test@miage.fr";
    private static final String CORRECT_NOM = "Dupont";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    // @Mock : creer un objet mock
    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    // @InjectMocks : ajouter les donnees mock a service
    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur mockUser;

    //prepare un user mock avant lancer TEST, @BeforeEach garantie toutes les donnees mock sont pretes
    @BeforeEach
    void setUp() {
        mockUser = new Utilisateur();
        mockUser.setId(CORRECT_ID);
        mockUser.setMail(CORRECT_MAIL);
        mockUser.setNom(CORRECT_NOM);
        mockUser.setMdp(ENCODED_PASSWORD);
    }

    @Test
    void testRegisterUser() {
        Utilisateur newUser = new Utilisateur();
        newUser.setMdp(RAW_PASSWORD);

        // imiter l'action encoder, quand on veut faire encoder, il va retourner ENCODED_PASSWORD directement.
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        // imiter l'action save, any(Utilisateur.class) garantie n'importe quel objet utilisateur peut passer
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(newUser);

        Utilisateur savedUser = assertDoesNotThrow(() -> utilisateurService.registerUser(newUser));

        assertEquals(ENCODED_PASSWORD, savedUser.getMdp());
        //methode de Mockito, verifier la methode save() d'utilisateurRepository est bien appelee.
        verify(utilisateurRepository).save(newUser);
    }

    @Test
    void testModifierProfile() {
        when(utilisateurRepository.findById(CORRECT_ID)).thenReturn(Optional.of(mockUser));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(mockUser);

        Utilisateur updatedUser = assertDoesNotThrow(() ->
                utilisateurService.modifierProfile(CORRECT_ID, "new@miage.fr", "M", 25, 1.80f, 75.0f)
        );

        assertEquals("new@miage.fr", updatedUser.getMail());
        assertEquals("M", updatedUser.getSexe());
        assertEquals(25, updatedUser.getAge());
        assertEquals(1.80f, updatedUser.getTaille());
        assertEquals(75.0f, updatedUser.getPoids());

        verify(utilisateurRepository).save(mockUser);
    }

    @Test
    void testChangerMotDePasseSuccess() {
        String newPassword = "newPassword123";
        String encodedNewPassword = "encodedNewPassword123";

        when(utilisateurRepository.findById(CORRECT_ID)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, ENCODED_PASSWORD)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        assertDoesNotThrow(() ->
                utilisateurService.changerMotDePasse(CORRECT_ID, RAW_PASSWORD, newPassword, newPassword)
        );

        assertEquals(encodedNewPassword, mockUser.getMdp());
        verify(utilisateurRepository).save(mockUser);
    }

    @Test
    void testChangerMotDePasseUserNotFoundShouldThrow() {
        when(utilisateurRepository.findById(CORRECT_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                utilisateurService.changerMotDePasse(CORRECT_ID, RAW_PASSWORD, "newMdp", "newMdp")
        );
        assertEquals("Utilisateur non trouvé.", exception.getMessage());
    }

    @Test
    void testChangerMotDePasseOldPasswordIncorrectShouldThrow() {
        when(utilisateurRepository.findById(CORRECT_ID)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                utilisateurService.changerMotDePasse(CORRECT_ID, "wrongPassword", "newMdp", "newMdp")
        );
        assertEquals("L'ancien mot de passe est incorrect.", exception.getMessage());
    }

    @Test
    void testChangerMotDePasseNewPasswordsNotMatchShouldThrow() {
        when(utilisateurRepository.findById(CORRECT_ID)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                utilisateurService.changerMotDePasse(CORRECT_ID, RAW_PASSWORD, "newMdp", "differentMdp")
        );
        assertEquals("Les nouveaux mots de passe ne correspondent pas.", exception.getMessage());
    }

    @Test
    void testChangerMotDePasseNewSameAsOldShouldThrow() {
        when(utilisateurRepository.findById(CORRECT_ID)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        // imiter l'action ancienmdp=nouveaumdp
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                utilisateurService.changerMotDePasse(CORRECT_ID, RAW_PASSWORD, RAW_PASSWORD, RAW_PASSWORD)
        );
        assertEquals("Le nouveau mot de passe doit être différent de l'ancien.", exception.getMessage());
    }

    @Test
    void testFindByIdU() {
        when(utilisateurRepository.findById(CORRECT_ID)).thenReturn(Optional.of(mockUser));

        Optional<Utilisateur> result = utilisateurService.findByIdU(CORRECT_ID);

        assertTrue(result.isPresent());
        assertEquals(CORRECT_ID, result.get().getId());
    }

    @Test
    void testFindByMailU() {
        when(utilisateurRepository.findByMail(CORRECT_MAIL)).thenReturn(Optional.of(mockUser));

        Optional<Utilisateur> result = utilisateurService.findByMailU(CORRECT_MAIL);

        assertTrue(result.isPresent());
        assertEquals(CORRECT_MAIL, result.get().getMail());
    }

    @Test
    void testFindByNomU() {
        when(utilisateurRepository.findByNom(CORRECT_NOM)).thenReturn(Optional.of(mockUser));

        Optional<Utilisateur> result = utilisateurService.findByNomU(CORRECT_NOM);

        assertTrue(result.isPresent());
        assertEquals(CORRECT_NOM, result.get().getNom());
    }

    @Test
    void testFindAll() {
        List<Utilisateur> users = Arrays.asList(mockUser, new Utilisateur());
        when(utilisateurRepository.findAll()).thenReturn(users);

        List<Utilisateur> result = utilisateurService.findAll();

        assertEquals(2, result.size());
        verify(utilisateurRepository).findAll();
    }
}
