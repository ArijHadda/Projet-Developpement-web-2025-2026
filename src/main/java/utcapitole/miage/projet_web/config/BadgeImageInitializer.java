package utcapitole.miage.projet_web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import utcapitole.miage.projet_web.model.jpa.BadgeRepository;
import utcapitole.miage.projet_web.util.BadgeImageMapper;

/**
 * Initialise les noms d'images pour les badges existants dans la base de données
 */
@Component
public class BadgeImageInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    public BadgeImageInitializer(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Mettre à jour tous les badges qui n'ont pas de imageName
        badgeRepository.findAll().forEach(badge -> {
            if (badge.getImageName() == null || badge.getImageName().isEmpty()) {
                String imageName = BadgeImageMapper.mapBadgeToImageName(badge.getEntitule());
                badge.setImageName(imageName);
                badgeRepository.save(badge);
            }
        });
    }
}
