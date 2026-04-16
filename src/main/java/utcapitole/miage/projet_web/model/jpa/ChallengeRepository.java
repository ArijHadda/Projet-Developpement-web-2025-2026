package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utcapitole.miage.projet_web.model.Challenge;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface d'accès aux données pour l'entité {@link Challenge}.
 */
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByCreateurId(Long createurId);

    /**
     * Récupère les challenges dont la période d'activité chevauche une période donnée.
     *
     * @param date1 La limite basse de la période (ex: Date du jour).
     * @param date2 La limite haute de la période (ex: Date du jour).
     * @return La liste des challenges actifs.
     */
    List<Challenge> findByDateDebutLessThanEqualAndDateFinGreaterThanEqual(LocalDate date1, LocalDate date2);

}