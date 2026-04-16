package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;

/**
 * Interface d'accès aux données pour la gestion des entités Objectifs.
 */
@Repository
public interface ObjectifRepository extends JpaRepository<Objectif, Long> {

    List<Objectif> findByUtilisateur(Utilisateur utilisateur);

}