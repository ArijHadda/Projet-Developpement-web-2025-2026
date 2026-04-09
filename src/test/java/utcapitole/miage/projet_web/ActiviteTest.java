package utcapitole.miage.projet_web;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActiviteTest {
   
    private final Long id = 1L;
    private final String nom = "Course";
    private final LocalDate date = LocalDate.of(2022, 1, 1);
    private final String conditionsMeteo = "Soleil";
    private final int duree = 60;
    private final float distance = 10;
    private final int note = 5;
    private final int niveauIntensiteTest = 3;
    private final int caloriesConsommeestest = 500;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // test
    @Test
    void testActiviteSettersAndGetters() {
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
        assertAll(
            () -> assertEquals(1L, activite.getId()),
            () -> assertEquals("Course", activite.getNom()),
            () -> assertEquals(LocalDate.of(2022, 1, 1), activite.getDate()),
            () -> assertEquals("Soleil", activite.getConditionsMeteo()),
            () -> assertEquals(60, activite.getDuree()),
            () -> assertEquals(10, activite.getDistance()),
            () -> assertEquals(5, activite.getNote()),
            () -> assertEquals(3, activite.getNiveauIntensite()),
            () -> assertEquals(500, activite.getCaloriesConsommees())
        );
    }
    

    @Test
    void testActiviteToString() {
        Activite activite = new Activite(id, nom, date, conditionsMeteo, duree, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        assertEquals("Activite [id=" + id + ", nom=" + nom + ", date=" + date + ", conditionsMeteo=" + conditionsMeteo
                + ", duree=" + duree + ", distance=" + distance + ", note=" + note + ", niveauIntensite=" + niveauIntensiteTest + ", caloriesConsommees="
                + caloriesConsommeestest + "]", activite.toString());
    }

    @Test
    void testActiviteConstructor() {
        Activite activite = new Activite(id, nom, date, conditionsMeteo, duree, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        assertAll(
            () -> assertEquals(1L, activite.getId()),
            () -> assertEquals("Course", activite.getNom()),
            () -> assertEquals(LocalDate.of(2022, 1, 1), activite.getDate()),
            () -> assertEquals("Soleil", activite.getConditionsMeteo()),
            () -> assertEquals(60, activite.getDuree()),
            () -> assertEquals(10, activite.getDistance()),
            () -> assertEquals(5, activite.getNote()),
            () -> assertEquals(3, activite.getNiveauIntensite()),
            () -> assertEquals(500, activite.getCaloriesConsommees())
        );
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
            () -> assertEquals(0, activite.getDistance()),
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
    void testActiviteGetUtilisateur() {
        Activite activite = new Activite();
        Utilisateur utilisateur = new Utilisateur();
        activite.setUtilisateur(utilisateur);
        assertEquals(utilisateur, activite.getUtilisateur());
    }


    @Test
    void testActiviteHashCode() {
        Activite activite = new Activite(id, nom, date, conditionsMeteo, duree, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        assertEquals(activite.hashCode(), activite.hashCode());
    }

    // Tests pour #44 : Champ obligatoire manquant
    @Test
    void testNomObligatoire() {
        Activite activite = new Activite(id, "", date, conditionsMeteo, duree, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "Le nom doit comporter une erreur de validation (NotBlank)");
        
        activite.setNom(null);
        violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "Le nom doit comporter une erreur de validation (NotNull/NotBlank)");
    }

    @Test
    void testDateObligatoire() {
        Activite activite = new Activite(id, nom, null, conditionsMeteo, duree, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La date doit comporter une erreur de validation (NotNull)");
    }

    @Test
    void testDureeObligatoire() {
        Activite activite = new Activite(id, nom, date, conditionsMeteo, 0, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La durée doit comporter une erreur de validation (Min)");
        activite.setDuree(-15);
        violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La durée négative doit comporter une erreur de validation (Min)");
    }

    @Test
    void testDistancePasNegative() {
        Activite activite = new Activite(id, nom, date, conditionsMeteo, duree, -2.5, note, niveauIntensiteTest, caloriesConsommeestest);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La distance ne peut pas être négative (PositiveOrZero)");
    }

    // Tests pour #46 : Validation logique des dates
    @Test
    void testDatePasDansLeFutur() {
        LocalDate dateFuture = LocalDate.now().plusDays(1);
        Activite activite = new Activite(id, nom, dateFuture, conditionsMeteo, duree, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertFalse(violations.isEmpty(), "La date ne peut pas être dans le futur (PastOrPresent)");
    }

    @Test
    void testActiviteValide() {
        Activite activite = new Activite(id, nom, LocalDate.now(), conditionsMeteo, duree, distance, note, niveauIntensiteTest, caloriesConsommeestest);
        Set<ConstraintViolation<Activite>> violations = validator.validate(activite);
        assertTrue(violations.isEmpty(), "L'activité devrait être valide sans erreurs de validation");
    }

}
