package utcapitole.miage.projet_web;

import org.junit.jupiter.api.Test;
import utcapitole.miage.projet_web.model.Badge;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BadgeTest {

    @Test
    void testNoArgsConstructor() {
        Badge badge = new Badge();

        assertAll(
                () -> assertNull(badge.getId()),
                () -> assertNull(badge.getEntitule())
        );
    }

    @Test
    void testConstructorWithArgs() {
        Badge badge = new Badge(1L, "1er 10km");

        assertAll(
                () -> assertEquals(1L, badge.getId()),
                () -> assertEquals("1er 10km", badge.getEntitule())
        );
    }

    @Test
    void testSettersAndGettersShouldWork() {
        Badge badge = new Badge();
        badge.setId(7L);
        badge.setEntitule("Marathonien");

        assertAll(
                () -> assertEquals(7L, badge.getId()),
                () -> assertEquals("Marathonien", badge.getEntitule())
        );
    }

    @Test
    void testToStringShouldMatchExpectedFormat() {
        Badge badge = new Badge(3L, "Finisher");

        assertEquals("Badge [id=3, entitule=Finisher]", badge.toString());
    }
}
