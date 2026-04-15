package utcapitole.miage.projet_web.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilisateurTest {

    @Test
    void testNoArgsConstructorInitializesCollections() {
        Utilisateur utilisateur = new Utilisateur();

        assertAll(
                () -> assertNotNull(utilisateur.getAmis()),
                () -> assertNotNull(utilisateur.getListSportNivPratique()),
                () -> assertNotNull(utilisateur.getActivites()),
                () -> assertNotNull(utilisateur.getBadges()),
                () -> assertNotNull(utilisateur.getChallengesCrees()),
                () -> assertNotNull(utilisateur.getParticipations()),
                () -> assertNotNull(utilisateur.getObjectifs()),

                () -> assertTrue(utilisateur.getAmis().isEmpty()),
                () -> assertTrue(utilisateur.getListSportNivPratique().isEmpty()),
                () -> assertTrue(utilisateur.getActivites().isEmpty()),
                () -> assertTrue(utilisateur.getBadges().isEmpty()),
                () -> assertTrue(utilisateur.getChallengesCrees().isEmpty()),
                () -> assertTrue(utilisateur.getParticipations().isEmpty()),
                () -> assertTrue(utilisateur.getBadges().isEmpty()),
                () -> assertTrue(utilisateur.getObjectifs().isEmpty())
        );
    }

    @Test
    void testSettersAndGettersShouldWork() {
        Utilisateur utilisateur = new Utilisateur();

        List<Utilisateur> amis = new ArrayList<>();
        amis.add(new Utilisateur());

        Sport sport = new Sport("Course", "Endurance", 0.0, 1.0, true);

        List<SportNiveauPratique> niveaux = new ArrayList<>();
        niveaux.add(new SportNiveauPratique(sport, NiveauPratique.DEBUTANT));

        Activite activite = new Activite();
        activite.setNom("Course footing");
        activite.setDate(LocalDate.now());
        List<Activite> activites = new ArrayList<>();
        activites.add(activite);

        Badge badge = new Badge(1L, "1er 10km", "10KM");
        List<Badge> badges = new ArrayList<>();
        badges.add(badge);

        utilisateur.setId(5L);
        utilisateur.setNom("Dupont");
        utilisateur.setPrenom("Alice");
        utilisateur.setMail("alice@miage.fr");
        utilisateur.setMdp("secret");
        utilisateur.setSexe("F");
        utilisateur.setAge(22);
        utilisateur.setTaille(1.68f);
        utilisateur.setPoids(58.5f);
        utilisateur.setAmis(amis);
        utilisateur.setListSportNivPratique(niveaux);
        utilisateur.setActivites(activites);
        utilisateur.setBadges(badges);

        assertAll(
                () -> assertEquals(5L, utilisateur.getId()),
                () -> assertEquals("Dupont", utilisateur.getNom()),
                () -> assertEquals("Alice", utilisateur.getPrenom()),
                () -> assertEquals("alice@miage.fr", utilisateur.getMail()),
                () -> assertEquals("secret", utilisateur.getMdp()),
                () -> assertEquals("F", utilisateur.getSexe()),
                () -> assertEquals(22, utilisateur.getAge()),
                () -> assertEquals(1.68f, utilisateur.getTaille()),
                () -> assertEquals(58.5f, utilisateur.getPoids()),
                () -> assertEquals(1, utilisateur.getAmis().size()),
                () -> assertEquals(1, utilisateur.getListSportNivPratique().size()),
                () -> assertEquals(1, utilisateur.getActivites().size()),
                () -> assertEquals(1, utilisateur.getBadges().size())
        );
    }

    @Test
    void testAllArgsConstructorShouldPopulateFields() {
        Utilisateur ami = new Utilisateur();
        List<Utilisateur> amis = List.of(ami);

        Sport sport = new Sport("Natation", "Cardio", 0.0, 1.0, true);

        List<SportNiveauPratique> niveaux = new ArrayList<>();
        niveaux.add(new SportNiveauPratique(sport, NiveauPratique.INTERMEDIAIRE));

        List<Activite> activites = List.of(new Activite());
        List<Badge> badges = List.of(new Badge(2L, "Finisher", "FINISHER"));

        List<Challenge> challenges = new ArrayList<>();
        List<Participation> participations = new ArrayList<>();

        List<Objectif> objectifs = new ArrayList<>();

        Utilisateur utilisateur = new Utilisateur(
                10L,
                "Martin",
                "Leo",
                "leo@miage.fr",
                "mdp",
                "M",
                24,
                1.80f,
                72.0f,
                amis,
                niveaux,
                activites,
                badges,
                challenges,
                participations,
                objectifs
        );

        assertAll(
                () -> assertEquals(10L, utilisateur.getId()),
                () -> assertEquals("Martin", utilisateur.getNom()),
                () -> assertEquals("Leo", utilisateur.getPrenom()),
                () -> assertEquals("leo@miage.fr", utilisateur.getMail()),
                () -> assertEquals("mdp", utilisateur.getMdp()),
                () -> assertEquals("M", utilisateur.getSexe()),
                () -> assertEquals(24, utilisateur.getAge()),
                () -> assertEquals(1.80f, utilisateur.getTaille()),
                () -> assertEquals(72.0f, utilisateur.getPoids()),
                () -> assertEquals(amis, utilisateur.getAmis()),
                () -> assertEquals(niveaux, utilisateur.getListSportNivPratique()),
                () -> assertEquals(activites, utilisateur.getActivites()),
                () -> assertEquals(badges, utilisateur.getBadges()),
                () -> assertEquals(challenges, utilisateur.getChallengesCrees()),
                () -> assertEquals(participations, utilisateur.getParticipations()),
                () -> assertEquals(objectifs, utilisateur.getObjectifs())
        );
    }

    @Test
    void testToStringContainsMainFields() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(3L);
        utilisateur.setNom("Bernard");
        utilisateur.setPrenom("Nina");
        utilisateur.setMail("nina@miage.fr");

        String representation = utilisateur.toString();

        assertAll(
                () -> assertTrue(representation.contains("Utilisateur")),
                () -> assertTrue(representation.contains("id=3")),
                () -> assertTrue(representation.contains("nom=Bernard")),
                () -> assertTrue(representation.contains("prenom=Nina")),
                () -> assertTrue(representation.contains("mail=nina@miage.fr"))
        );
    }
}