package utcapitole.miage.projet_web.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SportNiveauPratiqueTest {


    @Test
    void testConstructeurVide() {
        SportNiveauPratique snp = new SportNiveauPratique();

        assertNull(snp.getId());
        assertNull(snp.getUtilisateur());
        assertNull(snp.getSport());
        assertNull(snp.getNiveau());
    }

    @Test
    void testConstructeurAvecParametres() {
        Sport sport = new Sport();
        NiveauPratique niveau = NiveauPratique.DEBUTANT;

        SportNiveauPratique snp = new SportNiveauPratique(sport, niveau);

        assertEquals(sport, snp.getSport());
        assertEquals(niveau, snp.getNiveau());
        assertNull(snp.getUtilisateur());
        assertNull(snp.getId());
    }


    @Test
    void testSetId() {
        SportNiveauPratique snp = new SportNiveauPratique();
        snp.setId(10L);
        assertEquals(10L, snp.getId());
    }

    @Test
    void testSetUtilisateur() {
        SportNiveauPratique snp = new SportNiveauPratique();
        Utilisateur user = new Utilisateur();

        snp.setUtilisateur(user);
        assertEquals(user, snp.getUtilisateur());
    }

    @Test
    void testSetSport() {
        SportNiveauPratique snp = new SportNiveauPratique();
        Sport sport = new Sport();

        snp.setSport(sport);
        assertEquals(sport, snp.getSport());
    }
    @Test
    void testSetNiveau() {
        SportNiveauPratique snp = new SportNiveauPratique();
        NiveauPratique niveau = NiveauPratique.EXPERT;

        snp.setNiveau(niveau);
        assertEquals(niveau, snp.getNiveau());
    }
}