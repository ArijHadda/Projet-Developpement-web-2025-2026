package utcapitole.miage.projet_web.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

class SportTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSportConstructors() {
        // No-args constructor
        Sport sport1 = new Sport();
        assertAll(
            () -> assertNull(sport1.getId()),
            () -> assertNull(sport1.getNom()),
            () -> assertNull(sport1.getType()),
            () -> assertEquals(0.0, sport1.getIntensiteBase()),
            () -> assertEquals(0.0, sport1.getCoeffIntensite()),
            () -> assertFalse(sport1.isEstBaseSurVitesse())
        );

        // Parameters constructor
        Sport sport2 = new Sport("Course", "Endurance", 0.0, 1.0, true);
        assertAll(
            () -> assertEquals("Course", sport2.getNom()),
            () -> assertEquals("Endurance", sport2.getType()),
            () -> assertEquals(0.0, sport2.getIntensiteBase()),
            () -> assertEquals(1.0, sport2.getCoeffIntensite()),
            () -> assertTrue(sport2.isEstBaseSurVitesse())
        );
    }

    @Test
    void testSportGettersSetters() {
        Sport sport = new Sport();
        sport.setNom("Natation");
        sport.setType("Eau");
        sport.setIntensiteBase(4.0);
        sport.setCoeffIntensite(1.2);
        sport.setEstBaseSurVitesse(false);

        assertEquals("Natation", sport.getNom());
        assertEquals("Eau", sport.getType());
        assertEquals(4.0, sport.getIntensiteBase());
        assertEquals(1.2, sport.getCoeffIntensite());
        assertFalse(sport.isEstBaseSurVitesse());
    }

    @Test
    void testSportToString() {
        Sport sport = new Sport("Cyclisme", "Endurance", 2.0, 0.4, true);
        String expected = "Sport{id=null, nom='Cyclisme', type='Endurance', intensiteBase=2.0, coeffIntensite=0.4, estBaseSurVitesse=true}";
        assertEquals(expected, sport.toString());
    }

    @Test
    void testSportValidationOnNom() {
        // This test will only pass if we add @NotBlank to Sport.java
        Sport sport = new Sport("", "Endurance", 0.0, 1.0, true);
        Set<ConstraintViolation<Sport>> violations = validator.validate(sport);
        // Note: Currently Sport.java doesn't have validation annotations.
        // We will add them in the next step.
        assertFalse(violations.isEmpty(), "Le nom vide devrait déclencher une violation");
    }
}
