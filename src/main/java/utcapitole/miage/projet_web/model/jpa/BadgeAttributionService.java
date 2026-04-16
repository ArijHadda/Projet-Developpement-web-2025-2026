package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Badge;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.util.BadgeImageMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class BadgeAttributionService {

    // Badges de distance (tous sports)
    public static final String BADGE_PREMIER_10KM = "10km";
    public static final String BADGE_10KM = "10km";
    public static final String BADGE_25KM = "25km";
    public static final String BADGE_50KM = "50km";
    public static final String BADGE_100KM = "100km";

    // Badges de musculation (intensité >= 1)
    public static final String BADGE_10H_MUSCULATION = "10h musculation";
    public static final String BADGE_25H_MUSCULATION = "25h musculation";
    public static final String BADGE_50H_MUSCULATION = "50h musculation";
    public static final String BADGE_100H_MUSCULATION = "100h musculation";

    // Badges d'accomplissement
    public static final String BADGE_OBJECTIF_COMPLETE = "Premier Objectif Complété";
    public static final String BADGE_CHALLENGE_GAGNE = "Première Victoire de Challenge";

    private static final double[] PALIERS_DISTANCE = {10.0, 25.0, 50.0, 100.0};
    private static final String[] BADGES_DISTANCE = {BADGE_10KM, BADGE_25KM, BADGE_50KM, BADGE_100KM};

    private static final int[] PALIERS_MUSCULATION = {10, 25, 50, 100};
    private static final String[] BADGES_MUSCULATION = {BADGE_10H_MUSCULATION, BADGE_25H_MUSCULATION, BADGE_50H_MUSCULATION, BADGE_100H_MUSCULATION};

    private final UtilisateurRepository utilisateurRepository;
    private final ActiviteRepository activiteRepository;
    private final BadgeRepository badgeRepository;

    public BadgeAttributionService(UtilisateurRepository utilisateurRepository,
                                   ActiviteRepository activiteRepository,
                                   BadgeRepository badgeRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.activiteRepository = activiteRepository;
        this.badgeRepository = badgeRepository;
    }

    public List<String> enregistrerActiviteEtAttribuerBadges(Long utilisateurId, Activite activite) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        activite.setUtilisateur(utilisateur);
        activiteRepository.save(activite);

        return attribuerBadgesAutomatiques(utilisateurId);
    }

    public List<String> attribuerBadgesAutomatiques(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        List<String> badgesAttribues = new ArrayList<>();

        // Vérifier les badges de distance (tous sports)
        for (int i = 0; i < PALIERS_DISTANCE.length; i++) {
            if (aAtteintPalierDistance(utilisateurId, PALIERS_DISTANCE[i]) && !possedeBadge(utilisateur, BADGES_DISTANCE[i])) {
                attribuerBadge(utilisateur, BADGES_DISTANCE[i], badgesAttribues);
            }
        }

        // Vérifier les badges de musculation
        for (int i = 0; i < PALIERS_MUSCULATION.length; i++) {
            if (aAtteintPalierMusculation(utilisateurId, PALIERS_MUSCULATION[i]) && !possedeBadge(utilisateur, BADGES_MUSCULATION[i])) {
                attribuerBadge(utilisateur, BADGES_MUSCULATION[i], badgesAttribues);
            }
        }

        return badgesAttribues;
    }

    public List<String> attribuerBadgeObjectifComplet(Utilisateur utilisateur) {
        List<String> badgesAttribues = new ArrayList<>();
        if (!possedeBadge(utilisateur, BADGE_OBJECTIF_COMPLETE)) {
            attribuerBadge(utilisateur, BADGE_OBJECTIF_COMPLETE, badgesAttribues);
        }
        return badgesAttribues;
    }

    public List<String> attribuerBadgeChallengeGagne(Utilisateur utilisateur) {
        List<String> badgesAttribues = new ArrayList<>();
        if (!possedeBadge(utilisateur, BADGE_CHALLENGE_GAGNE)) {
            attribuerBadge(utilisateur, BADGE_CHALLENGE_GAGNE, badgesAttribues);
        }
        return badgesAttribues;
    }

    private void attribuerBadge(Utilisateur utilisateur, String entituleBadge, List<String> badgesAttribues) {
        Badge badge = badgeRepository.findByEntitule(entituleBadge)
                .orElseGet(() -> {
                    String imageName = BadgeImageMapper.mapBadgeToImageName(entituleBadge);
                    return badgeRepository.save(new Badge(entituleBadge, imageName));
                });

        utilisateur.getBadges().add(badge);
        utilisateurRepository.save(utilisateur);
        badgesAttribues.add(entituleBadge);
    }

    private boolean aAtteintPalierDistance(Long utilisateurId, double distance) {
        double totalDistanceKm = calculerDistanceTotaleEnKm(utilisateurId);
        return totalDistanceKm >= distance;
    }

    private boolean aAtteintPalierMusculation(Long utilisateurId, int heures) {
        Long totalDureeMinutes = activiteRepository.calculerDureeMusculation(utilisateurId);
        if (totalDureeMinutes == null) {
            return false;
        }
        return totalDureeMinutes >= heures * 60L;
    }

    private double calculerDistanceTotaleEnKm(Long utilisateurId) {
        List<Activite> activites = activiteRepository.findByUtilisateurId(utilisateurId);
        if (activites == null || activites.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Activite activite : activites) {
            if (activite == null || activite.getDistance() <= 0) {
                continue;
            }
            total += normaliserDistanceEnKm(activite.getDistance());
        }
        return total;
    }

    private double normaliserDistanceEnKm(double distanceSaisie) {
        // La saisie attendue est en km. Si une valeur très grande est stockée (ex: 10000),
        // on considère qu'elle est en mètres et on la convertit en km.
        return distanceSaisie > 500 ? distanceSaisie / 1000.0 : distanceSaisie;
    }

    private boolean possedeBadge(Utilisateur utilisateur, String entituleBadge) {
        return utilisateur.getBadges() != null && utilisateur.getBadges().stream()
                .anyMatch(badge -> entituleBadge.equalsIgnoreCase(badge.getEntitule()));
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }
}