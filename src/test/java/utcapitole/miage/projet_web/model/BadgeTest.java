package utcapitole.miage.projet_web.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BadgeTest {

    @Test
    void testNoArgsConstructor() {
        Badge badge = new Badge();

        assertAll(
                () -> assertNull(badge.getId()),
                () -> assertNull(badge.getEntitule()),
                () -> assertNull(badge.getImageName())
        );
    }

    @Test
    void testConstructorWithArgs() {
        Badge badge = new Badge(1L, "1er 10km", "10KM");

        assertAll(
                () -> assertEquals(1L, badge.getId()),
                () -> assertEquals("1er 10km", badge.getEntitule()),
                () -> assertEquals("10KM", badge.getImageName())
        );
    }

    @Test
    void testConstructorWithEntituleAndImageName() {
        Badge badge = new Badge("Marathonien", "MARATHON");

        assertAll(
                () -> assertNull(badge.getId()),
                () -> assertEquals("Marathonien", badge.getEntitule()),
                () -> assertEquals("MARATHON", badge.getImageName())
        );
    }

    @Test
    void testSettersAndGettersShouldWork() {
        Badge badge = new Badge();
        badge.setId(7L);
        badge.setEntitule("Marathonien");
        badge.setImageName("MARATHON");

        assertAll(
                () -> assertEquals(7L, badge.getId()),
                () -> assertEquals("Marathonien", badge.getEntitule()),
                () -> assertEquals("MARATHON", badge.getImageName())
        );
    }

    @Test
    void testToStringShouldMatchExpectedFormat() {
        Badge badge = new Badge(3L, "Finisher", "FINISHER");

        assertEquals("Badge [id=3, entitule=Finisher, imageName=FINISHER]", badge.toString());
    }
}
