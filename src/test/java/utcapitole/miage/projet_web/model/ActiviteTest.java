package utcapitole.miage.projet_web.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ActiviteTest {

    private final Long id = 1L;
    private final String nom = "Course";
    private final LocalDate date = LocalDate.of(2022, 1, 1);
    private final String conditionsMeteo = "Soleil";
    private final int duree = 60;
    private final double distance = 10;
    private final int note = 5;
    private final int niveauIntensiteTest = 3;
    private final int caloriesConsommeestest = 500;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Helper method pour créer une Activite valide sans utiliser l'ancien constructeur (parce qu'on a utilise lombok pour resoudre probleme de constructeur avec args>7)
    private Activite createValidActivite() {
        Activite activite = new Activite();
        activite.setId(id);
        activite.setNom(nom);
        activite.setDate(date);
        activite.setConditionsMeteo(conditionsMeteo);
        activite.setDuree(duree);
        activite.setDistance(distance);
        activite.setNote(note);
        activite.setNiveauIntensite(niveauIntensiteTest);
        activite.setCaloriesConsommees(caloriesConsommeestest);
        return activite;
    }

    @Test
    void testActiviteSettersAndGetters() {
        Activite activite = createValidActivite();
        assertAll(
                () -> assertEquals(1L, activite.getId()),
                () -> assertEquals("Course", activite.getNom()),
                () -> assertEquals(LocalDate.of(2022, 1, 1), activite.getDate()),
                () -> assertEquals("Soleil", activite.getConditionsMeteo()),
                () -> assertEquals(60, activite.getDuree()),
                () -> assertEquals(10.0, activite.getDistance()),
                () -> assertEquals(5, activite.getNote()),
                () -> assertEquals(3, activite.getNiveauIntensite()),
                () -> assertEquals(500, activite.getCaloriesConsommees())
        );
    }

    @Test
    void testActiviteToString() {
        Activite activite = createValidActivite();
        assertEquals("Activite [id=" + id + ", nom=" + nom + ", date=" + date + ", conditionsMeteo=" + conditionsMeteo
                + ", duree=" + duree + ", distance=" + distance + ", note=" + note + ", niveauIntensite=" + niveauIntensiteTest + ", caloriesConsommees="
                + caloriesConsommeestest + "]", activite.toString());
    }

    @Test
    void testActiviteNoArgsConstructor() {
        Activite activite = new Activite();
        assertAll(
                () -> assertEquals(null, activite.getId()),
                () -> assertEquals(null, activite.getNom()),
                () -> assertEquals(null, activite.getDate()),
                () -> assertEquals(null, activite.getConditionsMeteo()),
                () -> assertEquals(0, activite.getDuree()),
                () -> assertEquals(0.0, activite.getDistance()),
                () -> assertEquals(0, activite.getNote()),
                () -> assertEquals(0, activite.getNiveauIntensite()),
                () -> assertEquals(0, activite.getCaloriesConsommees())
        );
    }

    @Test
    void testActiviteSetUtilisateur() {
        Activite activite = new Activite();
        Utilisateur utilisateur = new Utilisateur();
        activite.setUtilisateur(utilisateur);
        assertEquals(utilisateur, activite.getUtilisateur());
    }

    @Test
    void testActiviteHashCode() {
        Activite activite = createValidActivite();
        assertEquals(activite.hashCode(), activite.hashCode());
    }

    // Tests pour #44 : Champ obligatoire manquant
    @Test
    void testNomObligatoire() {
        Activite activite = createValidActivite();
        activite.setNom("");
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "Le nom doit comporter une erreur de validation (NotBlank)");

        activite.setNom(null);
        violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "Le nom doit comporter une erreur de validation (NotNull/NotBlank)");
    }

    @Test
    void testDateObligatoire() {
        Activite activite = createValidActivite();
        activite.setDate(null);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La date doit comporter une erreur de validation (NotNull)");
    }

    @Test
    void testDureeObligatoire() {
        Activite activite = createValidActivite();
        activite.setDuree(0);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La durée doit comporter une erreur de validation (Min)");
        activite.setDuree(-15);
        violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La durée négative doit comporter une erreur de validation (Min)");
    }

    @Test
    void testDistancePasNegative() {
        Activite activite = createValidActivite();
        activite.setDistance(-2.5);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La distance ne peut pas être négative (PositiveOrZero)");
    }

    // Tests pour #46 : Validation logique des dates
    @Test
    void testDatePasDansLeFutur() {
        Activite activite = createValidActivite();
        activite.setDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La date ne peut pas être dans le futur (PastOrPresent)");
    }

    @Test
    void testActiviteValide() {
        Activite activite = createValidActivite();
        activite.setDate(LocalDate.now()); // Assure que la date est au moins aujourd'hui
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertTrue(violations.isEmpty(), "L'activité devrait être valide sans erreurs de validation");
    }

    @Test
    void testNiveauIntensiteValidation() {
        Activite activite = createValidActivite();

        // Test min value (1)
        activite.setNiveauIntensite(0);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "Le niveau d'intensité 0 doit comporter une erreur de validation (Min)");

        // Test max value (5)
        activite.setNiveauIntensite(6);
        violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "Le niveau d'intensité 6 doit comporter une erreur de validation (Max)");

        // Test valid value
        activite.setNiveauIntensite(3);
        violations = validator.validate(activite);
        assertTrue(violations.isEmpty(), "Le niveau d'intensité 3 est valide");
    }

    @Test
    void testNoteValidation() {
        Activite activite = createValidActivite();

        activite.setNote(0);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La note 0 doit comporter une erreur de validation (Min)");

        activite.setNote(11);
        violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La note 11 doit comporter une erreur de validation (Max)");

        activite.setNote(7);
        violations = validator.validate(activite);
        assertTrue(violations.isEmpty(), "La note 7 est valide");
    }

    // --- Tests pour Kudos et Commentaires ---

    @Test
    void testKudosAndCommentaires() {
        Activite activite = new Activite();

        assertEquals(0, activite.getNbKudos(), "Une nouvelle activité devrait avoir 0 kudos");

        Utilisateur u1 = new Utilisateur();
        u1.setId(1L);
        java.util.List<Utilisateur> likers = new java.util.ArrayList<>();
        likers.add(u1);
        activite.setLikers(likers);

        assertEquals(1, activite.getNbKudos(), "Le nombre de kudos doit correspondre à la taille de la liste des likers");
        assertTrue(activite.getLikers().contains(u1));

        Commentaire com = new Commentaire();
        com.setContenu("Bravo !");
        java.util.List<Commentaire> commentaires = new java.util.ArrayList<>();
        commentaires.add(com);
        activite.setCommentaires(commentaires);

        assertEquals(1, activite.getCommentaires().size());
        assertEquals("Bravo !", activite.getCommentaires().get(0).getContenu());
    }

    @Test
    void testGetNbKudos_nullLikers_returnsZero() {
        Activite activite = new Activite();
        activite.setLikers(null);
        assertEquals(0, activite.getNbKudos());
    }

    @Test
    void testSetAndGetSport() {
        Activite activite = new Activite();
        Sport sport = new Sport();
        sport.setNom("Tennis");

        activite.setSport(sport);
        assertEquals(sport, activite.getSport());
        assertEquals("Tennis", activite.getSport().getNom());
    }

    @Test
    void testSetAndGetLikers() {
        Activite activite = new Activite();
        Utilisateur u1 = new Utilisateur();
        u1.setId(1L);
        Utilisateur u2 = new Utilisateur();
        u2.setId(2L);

        java.util.List<Utilisateur> likers = new java.util.ArrayList<>();
        likers.add(u1);
        likers.add(u2);

        activite.setLikers(likers);
        assertEquals(2, activite.getLikers().size());
        assertTrue(activite.getLikers().contains(u1));
        assertTrue(activite.getLikers().contains(u2));
    }

    @Test
    void testSetAndGetCommentaires() {
        Activite activite = new Activite();
        Commentaire com1 = new Commentaire();
        com1.setContenu("Commentaire 1");
        Commentaire com2 = new Commentaire();
        com2.setContenu("Commentaire 2");

        java.util.List<Commentaire> commentaires = new java.util.ArrayList<>();
        commentaires.add(com1);
        commentaires.add(com2);

        activite.setCommentaires(commentaires);
        assertEquals(2, activite.getCommentaires().size());
        assertEquals("Commentaire 1", activite.getCommentaires().get(0).getContenu());
        assertEquals("Commentaire 2", activite.getCommentaires().get(1).getContenu());
    }
}