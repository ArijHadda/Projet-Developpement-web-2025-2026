package utcapitole.miage.projet_web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import utcapitole.miage.projet_web.model.Badge;
import utcapitole.miage.projet_web.model.jpa.BadgeRepository;
import utcapitole.miage.projet_web.util.BadgeImageMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Initialise tous les badges du système dans la base de données au démarrage
 */
@Component
public class BadgeImageInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    public BadgeImageInitializer(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // 1. Liste complète des 10 badges définis dans le système
        List<String> tousLesBadges = Arrays.asList(
                "10km", "25km", "50km", "100km",
                "10h musculation", "25h musculation", "50h musculation", "100h musculation",
                "Premier Objectif Complété", "Première Victoire de Challenge"
        );

        // 2. Créer les badges dans la base de données s'ils n'existent pas encore
        for (String entitule : tousLesBadges) {
            if (badgeRepository.findByEntitule(entitule).isEmpty()) {
                String imageName = BadgeImageMapper.mapBadgeToImageName(entitule);
                Badge nouveauBadge = new Badge(entitule, imageName);
                badgeRepository.save(nouveauBadge);
            }
        }

        // 3. Mettre à jour les anciens badges qui n'auraient pas d'imageName (Sécurité)
        badgeRepository.findAll().forEach(badge -> {
            if (badge.getImageName() == null || badge.getImageName().isEmpty()) {
                String imageName = BadgeImageMapper.mapBadgeToImageName(badge.getEntitule());
                badge.setImageName(imageName);
                badgeRepository.save(badge);
            }
        });
    }
}