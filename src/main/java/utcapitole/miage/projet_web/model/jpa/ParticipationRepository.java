package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Participation;
import utcapitole.miage.projet_web.model.Utilisateur;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByUtilisateurAndChallenge(Utilisateur utilisateur, Challenge challenge);
}
