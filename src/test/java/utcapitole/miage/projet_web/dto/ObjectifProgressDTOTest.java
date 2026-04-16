package utcapitole.miage.projet_web.dto;

import org.junit.jupiter.api.Test;
import utcapitole.miage.projet_web.model.Objectif;
import static org.junit.jupiter.api.Assertions.*;

class ObjectifProgressDTOTest {

    private static final String TEST_TITRE = "Test Objectif";
    private static final double DISTANCE = 50.0;
    private static final double POURCENTAGE = 50.0;
    private static final double DUREE = 300.0;

    @Test
    void testGetObjectif() {
        Objectif expectedObjectif = new Objectif();
        expectedObjectif.setId(10L);
        expectedObjectif.setTitre(TEST_TITRE);
        ObjectifProgressDTO dto = new ObjectifProgressDTO(expectedObjectif, DISTANCE, POURCENTAGE, DUREE, POURCENTAGE);
        assertEquals(expectedObjectif, dto.getObjectif(), "L'objectif retourné doit être celui passé au constructeur.");
        assertEquals(TEST_TITRE, dto.getObjectif().getTitre());
    }

    @Test
    void testAllGetters() {
        Objectif obj = new Objectif();
        ObjectifProgressDTO dto = new ObjectifProgressDTO(obj, 10.5, 25.0, 100.0, 75.0);

        assertEquals(obj, dto.getObjectif());
        assertEquals(10.5, dto.getDistanceActuelle());
        assertEquals(25.0, dto.getPourcentageDistance());
        assertEquals(100.0, dto.getDureeActuelle());
        assertEquals(75.0, dto.getPourcentageDuree());
    }
}
