package utcapitole.miage.projet_web.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void testAddAmi_CreationListeSiNull() {
        Utilisateur u1 = new Utilisateur();
        Utilisateur u2 = new Utilisateur();

        u1.setAmis(null); // simulate null list

        u1.addAmi(u2);

        assertNotNull(u1.getAmis());
        assertTrue(u1.getAmis().contains(u2));
    }

    @Test
    void testGetAmisWhenNull() {
        Utilisateur user = new Utilisateur();
        user.setAmis(null);

        assertNull(user.getAmis());
    }

    @Test
    void testGetAmisWhenNotNull() {
        Utilisateur user = new Utilisateur();

        Utilisateur a1 = new Utilisateur();
        Utilisateur a2 = new Utilisateur();

        List<Utilisateur> list = new ArrayList<>();
        list.add(a1);
        list.add(a2);

        user.setAmis(list);

        assertEquals(2, user.getAmis().size());
        assertSame(list, user.getAmis());
    }
    @Test
    void testAddAmi_AjoutAmi() {
        Utilisateur u1 = new Utilisateur();
        Utilisateur u2 = new Utilisateur();

        u1.setAmis(new ArrayList<>());
        u2.setAmis(new ArrayList<>());

        u1.addAmi(u2);

        assertEquals(1, u1.getAmis().size());
        assertTrue(u1.getAmis().contains(u2));
    }

    @Test
    void testAddAmi_PasDeDoublon() {
        Utilisateur u1 = new Utilisateur();
        Utilisateur u2 = new Utilisateur();

        u1.setAmis(new ArrayList<>());
        u2.setAmis(new ArrayList<>());

        u1.addAmi(u2);
        u1.addAmi(u2); // ne doit pas le reenregistre

        assertEquals(1, u1.getAmis().size());
    }
    @Test
    void testAddAmi_AjoutReciproque() {
        Utilisateur u1 = new Utilisateur();
        Utilisateur u2 = new Utilisateur();

        u1.setAmis(new ArrayList<>());
        u2.setAmis(new ArrayList<>());

        u1.addAmi(u2);

        assertTrue(u1.getAmis().contains(u2));
        assertTrue(u2.getAmis().contains(u1)); // reciprocal
    }
    @Test
    void testGetListActiviteWhenNotNull(){
        Utilisateur user = new Utilisateur();

        Activite act1 = new Activite();
        Activite act2 = new Activite();

        List<Activite> listA = new ArrayList<>();
        listA.add(act1);
        listA.add(act2);

        user.setActivites(listA);

        List<Activite> result = user.getActivites();

        assertEquals(2, result.size());
        assertTrue(result.contains(act1));
        assertTrue(result.contains(act2));
        assertSame(listA, result);
    }
    @Test
    void testGetActivitesWhenNull() {
        Utilisateur user = new Utilisateur();
        user.setActivites(null);

        assertNull(user.getActivites());
    }

    @Test
    void testGetBadgesWhenNull() {
        Utilisateur user = new Utilisateur();
        user.setBadges(null);

        assertNull(user.getBadges());
    }

    @Test
    void testGetBadgesWhenNotNull() {
        Utilisateur user = new Utilisateur();

        Badge b1 = new Badge();
        Badge b2 = new Badge();

        List<Badge> listB = new ArrayList<>();
        listB.add(b1);
        listB.add(b2);

        user.setBadges(listB);

        List<Badge> result = user.getBadges();

        assertEquals(2, result.size());
        assertTrue(result.contains(b1));
        assertTrue(result.contains(b2));
        assertSame(listB, result);
    }

    @Test
    void testGetChallengesCreesWhenNull() {
        Utilisateur user = new Utilisateur();
        user.setChallengesCrees(null);

        assertNull(user.getChallengesCrees());
    }

    @Test
    void testGetChallengesCreesWhenNotNull() {
        Utilisateur user = new Utilisateur();

        Challenge c1 = new Challenge();
        Challenge c2 = new Challenge();

        List<Challenge> list = new ArrayList<>();
        list.add(c1);
        list.add(c2);

        user.setChallengesCrees(list);

        assertEquals(2, user.getChallengesCrees().size());
        assertSame(list, user.getChallengesCrees());
    }

    @Test
    void testGetListSportNivPratiqueWhenNull() {
        Utilisateur user = new Utilisateur();
        user.setListSportNivPratique(null);

        assertNull(user.getListSportNivPratique());
    }

    @Test
    void testGetListSportNivPratiqueWhenNotNull() {
        Utilisateur user = new Utilisateur();

        SportNiveauPratique s1 = new SportNiveauPratique();
        SportNiveauPratique s2 = new SportNiveauPratique();

        List<SportNiveauPratique> list = new ArrayList<>();
        list.add(s1);
        list.add(s2);

        user.setListSportNivPratique(list);

        assertEquals(2, user.getListSportNivPratique().size());
        assertSame(list, user.getListSportNivPratique());
    }
    @Test
    void testGetParticipationsWhenNull() {
        Utilisateur user = new Utilisateur();
        user.setParticipations(null);

        assertNull(user.getParticipations());
    }

    @Test
    void testGetParticipationsWhenNotNull() {
        Utilisateur user = new Utilisateur();

        Participation p1 = new Participation();
        Participation p2 = new Participation();

        List<Participation> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);

        user.setParticipations(list);

        assertEquals(2, user.getParticipations().size());
        assertSame(list, user.getParticipations());
    }
    @Test
    void testGetObjectifsWhenNull() {
        Utilisateur user = new Utilisateur();
        user.setObjectifs(null);

        assertNull(user.getObjectifs());
    }

    @Test
    void testGetObjectifsWhenNotNull() {
        Utilisateur user = new Utilisateur();

        Objectif o1 = new Objectif();
        Objectif o2 = new Objectif();

        List<Objectif> list = new ArrayList<>();
        list.add(o1);
        list.add(o2);

        user.setObjectifs(list);

        assertEquals(2, user.getObjectifs().size());
        assertSame(list, user.getObjectifs());
    }
}