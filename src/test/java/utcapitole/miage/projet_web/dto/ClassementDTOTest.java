package utcapitole.miage.projet_web.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassementDTOTest {

    private static final String EXPECTED_NOM = "Jean Dupont";
    private static final Integer EXPECTED_CALORIES = 500;

    @Test
    void testConstructorAndGetters() {
        ClassementDTO dto = new ClassementDTO(EXPECTED_NOM, EXPECTED_CALORIES);
        assertEquals(EXPECTED_NOM, dto.getNomComplet());
        assertEquals(EXPECTED_CALORIES, dto.getTotalCalories());
    }
}
