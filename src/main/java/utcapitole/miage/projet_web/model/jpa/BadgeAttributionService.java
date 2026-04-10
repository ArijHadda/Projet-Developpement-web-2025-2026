package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Badge;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.ArrayList;
import java.util.List;

@Service
public class BadgeAttributionService {

    public static final String BADGE_PREMIER_10KM = "1er 10km";
    private static final double PALIER_PREMIER_10KM = 10.0;

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
        if (aAtteintLePalierPremier10Km(utilisateurId) && !possedeBadge(utilisateur, BADGE_PREMIER_10KM)) {
            Badge badgePremier10Km = badgeRepository.findByEntitule(BADGE_PREMIER_10KM)
                    .orElseGet(() -> badgeRepository.save(new Badge(null, BADGE_PREMIER_10KM)));

            utilisateur.getBadges().add(badgePremier10Km);
            utilisateurRepository.save(utilisateur);
            badgesAttribues.add(BADGE_PREMIER_10KM);
        }

        return badgesAttribues;
    }

    private boolean aAtteintLePalierPremier10Km(Long utilisateurId) {
        return activiteRepository.existsByUtilisateurIdAndDistanceGreaterThanEqual(utilisateurId, PALIER_PREMIER_10KM);
    }

    private boolean possedeBadge(Utilisateur utilisateur, String entituleBadge) {
        return utilisateur.getBadges() != null && utilisateur.getBadges().stream()
                .anyMatch(badge -> entituleBadge.equalsIgnoreCase(badge.getEntitule()));
    }
}