package utcapitole.miage.projet_web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import utcapitole.miage.projet_web.model.Sport;
import utcapitole.miage.projet_web.model.jpa.SportRepository;

import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDatabase(SportRepository repository) {
        final String sportTypeDurance = "Endurance";
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                    // 1. Course: Basé sur la vitesse (Approximation: MET = vitesse)
                    // Base=0, Coeff=1.0 (ex: 10km/h -> 10 MET)
                    new Sport("Course", sportTypeDurance, 0.0, 1.0, true),

                    // 2. Cyclisme: Basé sur la vitesse (Approximation: MET = 2 + 0.4*v)
                    // Base=2, Coeff=0.4 (ex: 20km/h -> 10 MET)
                    new Sport("Cyclisme", sportTypeDurance, 2.0, 0.4, true),

                    // 3. Musculation/Yoga: Basé sur l'intensité (1-5)
                    // Base=2, Coeff=1.0 (ex: Niveau 3 -> 5 MET)
                    new Sport("Musculation", "Force", 2.0, 1.0, false),

                    // 4. Marche: Basé sur la vitesse (Approximation: MET = 2 + 0.5*v)
                    // Base=2, Coeff=0.5 (ex: 5km/h -> 4.5 MET)
                    new Sport("Marche", sportTypeDurance, 2.0, 0.5, true)
                ));
                logger.info("Initialisation des données de sport terminée.");
            }
        };
    }
}
