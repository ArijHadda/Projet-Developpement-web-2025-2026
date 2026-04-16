package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Participation;
import utcapitole.miage.projet_web.model.Utilisateur;

/**
 * Interface d'accès aux données pour la table de jointure des Participations.
 */
@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    /**
     * Vérifie si un utilisateur donné est déjà inscrit à un challenge spécifique.
     *
     * @param utilisateur L'utilisateur à tester.
     * @param challenge Le challenge ciblé.
     * @return true s'il est déjà participant, false sinon.
     */
    boolean existsByUtilisateurAndChallenge(Utilisateur utilisateur, Challenge challenge);
}