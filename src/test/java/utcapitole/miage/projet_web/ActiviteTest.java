package utcapitole.miage.projet_web;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import utcapitole.miage.projet_web.model.Activite;

public class ActiviteTest {
   
    private final Long id = 1L;
    private final String nom = "Course";
    private final String date = "2022-01-01";
    private final String conditionsMeteo = "Soleil";
    private final int duree = 60;
    private final float distance = 10;
    private final int note = 5;
    private final int caloriesConsommeestest = 500;

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
        activite.setCaloriesConsommees(caloriesConsommeestest);
        assertAll(
            () -> assertEquals(1L, activite.getId()),
            () -> assertEquals("Course", activite.getNom()),
            () -> assertEquals("2022-01-01", activite.getDate()),
            () -> assertEquals("Soleil", activite.getConditionsMeteo()),
            () -> assertEquals(60, activite.getDuree()),
            () -> assertEquals(10, activite.getDistance()),
            () -> assertEquals(5, activite.getNote()),
            () -> assertEquals(500, activite.getCaloriesConsommees())
        );
    }

}
