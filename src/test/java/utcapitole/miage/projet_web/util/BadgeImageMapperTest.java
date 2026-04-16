package utcapitole.miage.projet_web.util;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class BadgeImageMapperTest {

    @Test
    void testMapBadgeToImageName_NullOuVide() {
        assertEquals("BADGE", BadgeImageMapper.mapBadgeToImageName(null));
        assertEquals("BADGE", BadgeImageMapper.mapBadgeToImageName(""));
        assertEquals("BADGE", BadgeImageMapper.mapBadgeToImageName("   "));
    }

    @Test
    void testMapBadgeToImageName_Distance() {
        assertEquals("10KM", BadgeImageMapper.mapBadgeToImageName("10km"));
        assertEquals("25KM", BadgeImageMapper.mapBadgeToImageName(" 25KM ")); // Test trim et case
        assertEquals("50KM", BadgeImageMapper.mapBadgeToImageName("50km"));
        assertEquals("100KM", BadgeImageMapper.mapBadgeToImageName("100Km"));
    }

    @Test
    void testMapBadgeToImageName_Musculation() {
        assertEquals("10H", BadgeImageMapper.mapBadgeToImageName("10h musculation"));
        assertEquals("25H", BadgeImageMapper.mapBadgeToImageName("25H"));
        assertEquals("50H", BadgeImageMapper.mapBadgeToImageName("50h musculation intense"));
        assertEquals("100H", BadgeImageMapper.mapBadgeToImageName("100h"));
    }

    @Test
    void testMapBadgeToImageName_Accomplissement() {
        assertEquals("OBJECTIF", BadgeImageMapper.mapBadgeToImageName("Premier Objectif"));
        assertEquals("OBJECTIF", BadgeImageMapper.mapBadgeToImageName("objectif atteint"));
        assertEquals("CHALLENGE", BadgeImageMapper.mapBadgeToImageName("Première Victoire de Challenge"));
        assertEquals("CHALLENGE", BadgeImageMapper.mapBadgeToImageName("CHALLENGE GAGNÉ"));
    }

    @Test
    void testMapBadgeToImageName_ParDefaut() {
        // Test le fallback (majuscule par défaut)
        assertEquals("INCONNU", BadgeImageMapper.mapBadgeToImageName("inconnu"));
        assertEquals("NOUVEAU BADGE", BadgeImageMapper.mapBadgeToImageName("Nouveau Badge"));
    }

    @Test
    void testConstructeurPrive() throws Exception {
        // Couverture du constructeur privé des classes utilitaires pour SonarQube
        Constructor<BadgeImageMapper> constructor = BadgeImageMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        assertNotNull(constructor.newInstance());
    }
}