package utcapitole.miage.projet_web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuration de la sécurité pour l'application.
 * Définit les beans nécessaires au chiffrement des mots de passe.
 */
@Configuration
public class SecurityConfig {

    /**
     * Définit l'encodeur de mots de passe par défaut.
     * @return un {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

