package utcapitole.miage.projet_web.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ObjectifTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testConstructorsAndGetters() {
        Utilisateur user = new Utilisateur();
        user.setId(1L);
        Sport sport = new Sport();
        sport.setId(2L);

        Objectif obj = new Objectif("Courir 10km", Frequence.MENSUEL, 120, 10.0, user, sport);
        obj.setId(10L);

        assertAll(
                () -> assertEquals(10L, obj.getId()),
                () -> assertEquals("Courir 10km", obj.getTitre()),
                () -> assertEquals(Frequence.MENSUEL, obj.getFrequence()), // Point de modification
                () -> assertEquals(120, obj.getDuree()),
                () -> assertEquals(10.0, obj.getDistance()),
                () -> assertEquals(user, obj.getUtilisateur()),
                () -> assertEquals(sport, obj.getSport())
        );
    }

    @Test
    void testValidationTitreObligatoire() {
        Objectif obj = new Objectif("", Frequence.MENSUEL, 60, 5.0, new Utilisateur(), new Sport());
        Set<ConstraintViolation<Objectif>> violations = validator.validate(obj);

        assertFalse(violations.isEmpty(), "Le titre vide doit déclencher une erreur");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("obligatoire")));
    }

    @Test
    void testValidationValeursNegatives() {
        Objectif obj = new Objectif("Test", Frequence.MENSUEL, -10, -5.0, new Utilisateur(), new Sport());
        Set<ConstraintViolation<Objectif>> violations = validator.validate(obj);

        assertEquals(2, violations.size(), "Duree et distance négatives doivent déclencher des erreurs");
    }

    @Test
    void testObjectifValide() {
        Objectif obj = new Objectif("Valide", Frequence.MENSUEL, 60, 5.0, new Utilisateur(), new Sport());
        Set<ConstraintViolation<Objectif>> violations = validator.validate(obj);

        assertTrue(violations.isEmpty(), "Un objectif correctement rempli ne doit avoir aucune erreur");
    }

    @Test
    void testConstructeurShouldWork(){
        Objectif obj = new Objectif();

        assertNull(obj.getId());
        assertNull(obj.getTitre());
        assertEquals(Frequence.MENSUEL, obj.getFrequence());
        assertEquals(0, obj.getDuree());
        assertEquals(0.0, obj.getDistance());
        assertNull(obj.getUtilisateur());
        assertNull(obj.getSport());
    }

    @Test
    void testSetTitle(){
        Objectif obj = new Objectif();
        obj.setTitre("hello");
        String title = obj.getTitre();
        assertEquals("hello",title);
    }

    @Test
    void testSetFrequence() {
        Objectif obj = new Objectif();
        obj.setFrequence(Frequence.QUOTIDIEN);
        assertEquals(Frequence.QUOTIDIEN, obj.getFrequence());
    }

    @Test
    void testSetDuree() {
        Objectif obj = new Objectif();
        obj.setDuree(45);
        assertEquals(45, obj.getDuree());
    }

    @Test
    void testSetDistance() {
        Objectif obj = new Objectif();
        obj.setDistance(3.2);
        assertEquals(3.2, obj.getDistance());
    }

    @Test
    void testSetUtilisateur() {
        Objectif obj = new Objectif();
        Utilisateur user = new Utilisateur();

        obj.setUtilisateur(user);
        assertEquals(user, obj.getUtilisateur());
    }
    @Test
    void testSetSport() {
        Objectif obj = new Objectif();
        Sport sport = new Sport();

        obj.setSport(sport);
        assertEquals(sport, obj.getSport());
    }
}