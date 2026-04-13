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
                () -> assertEquals(Frequence.MENSUEL, obj.getFrequence()), // 修改点
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
}