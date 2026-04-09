package utcapitole.miage.projet_web;

import org.junit.jupiter.api.Test;
import utcapitole.miage.projet_web.model.Sport;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SportTest {

    @Test
    void testNoArgsConstructor() {
        Sport sport = new Sport();

        assertAll(
                () -> assertNull(sport.getId()),
                () -> assertNull(sport.getNom()),
                () -> assertNull(sport.getType())
        );
    }

    @Test
    void testConstructorWithArgs() {
        Sport sport = new Sport("Course", "Endurance");

        assertAll(
                () -> assertEquals("Course", sport.getNom()),
                () -> assertEquals("Endurance", sport.getType())
        );
    }

    @Test
    void testSettersAndGettersShouldWork() {
        Sport sport = new Sport();

        sport.setNom("Natation");
        sport.setType("Cardio");

        assertAll(
                () -> assertEquals("Natation", sport.getNom()),
                () -> assertEquals("Cardio", sport.getType())
        );
    }

    @Test
    void testToStringContainsFields() {
        Sport sport = new Sport("Cyclisme", "Route");

        String representation = sport.toString();

        assertAll(
                () -> assertTrue(representation.contains("Sport{")),
                () -> assertTrue(representation.contains("nom='Cyclisme'")),
                () -> assertTrue(representation.contains("type='Route'"))
        );
    }
}
