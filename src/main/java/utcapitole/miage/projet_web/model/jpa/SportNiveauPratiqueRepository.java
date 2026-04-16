package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import utcapitole.miage.projet_web.model.SportNiveauPratique;

import java.util.Optional;

/**
 * Interface d'accès aux données pour la relation entre un utilisateur, un sport et son niveau.
 */
public interface SportNiveauPratiqueRepository extends JpaRepository<SportNiveauPratique, Long> {

    void deleteById(Long id);

    /**
     * Recherche la configuration de niveau sportif d'un utilisateur pour un sport donné.
     *
     * @param idu L'identifiant de l'utilisateur.
     * @param idSport L'identifiant du sport.
     * @return Un Optional contenant l'entité SportNiveauPratique si elle existe.
     */
    Optional<SportNiveauPratique> findByUtilisateurIdAndSportId(Long idu, Long idSport);
}