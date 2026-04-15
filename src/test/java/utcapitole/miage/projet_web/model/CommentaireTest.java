package utcapitole.miage.projet_web.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentaireTest {

    @Test
    void testNoArgsConstructor() {
        Commentaire commentaire = new Commentaire();

        assertAll(
                () -> assertNull(commentaire.getId()),
                () -> assertNull(commentaire.getContenu()),
                () -> assertNull(commentaire.getDateCreation()),
                () -> assertNull(commentaire.getAuteur()),
                () -> assertNull(commentaire.getActivite())
        );
    }

    @Test
    void testAllArgsConstructor() {
        Utilisateur auteur = new Utilisateur();
        auteur.setId(1L);
        auteur.setPrenom("Alice");

        Activite activite = new Activite();
        activite.setId(10L);

        LocalDateTime date = LocalDateTime.of(2026, 4, 12, 14, 30);

        Commentaire commentaire = new Commentaire(
                5L,
                "Super performance !",
                date,
                auteur,
                activite
        );

        assertAll(
                () -> assertEquals(5L, commentaire.getId()),
                () -> assertEquals("Super performance !", commentaire.getContenu()),
                () -> assertEquals(date, commentaire.getDateCreation()),
                () -> assertEquals(1L, commentaire.getAuteur().getId()),
                () -> assertEquals("Alice", commentaire.getAuteur().getPrenom()),
                () -> assertEquals(10L, commentaire.getActivite().getId())
        );
    }

    @Test
    void testGettersAndSetters() {
        Commentaire commentaire = new Commentaire();

        Utilisateur auteur = new Utilisateur();
        auteur.setId(2L);

        Activite activite = new Activite();
        activite.setId(20L);

        LocalDateTime now = LocalDateTime.now();

        commentaire.setId(7L);
        commentaire.setContenu("Bravo pour ce record !");
        commentaire.setDateCreation(now);
        commentaire.setAuteur(auteur);
        commentaire.setActivite(activite);

        assertAll(
                () -> assertEquals(7L, commentaire.getId()),
                () -> assertEquals("Bravo pour ce record !", commentaire.getContenu()),
                () -> assertEquals(now, commentaire.getDateCreation()),
                () -> assertEquals(auteur, commentaire.getAuteur()),
                () -> assertEquals(activite, commentaire.getActivite())
        );
    }

    @Test
    void testToStringAndEqualsProvidedByLombok() {
        Utilisateur auteur = new Utilisateur();
        auteur.setId(1L);

        Commentaire com1 = new Commentaire(1L, "Test", null, auteur, null);
        Commentaire com2 = new Commentaire(1L, "Test", null, auteur, null);

        assertEquals(com1, com2);

        assertTrue(com1.toString().contains("Commentaire"));
        assertTrue(com1.toString().contains("Test"));
    }
}
