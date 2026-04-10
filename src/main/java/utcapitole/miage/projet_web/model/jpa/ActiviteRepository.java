package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;


@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    List<Activite> findByNom(String nom);

    List<Activite> findByUtilisateurIdOrderByDateDesc(Long id);

    List<Activite> findByUtilisateur(Utilisateur user);

    boolean existsByUtilisateurIdAndDistanceGreaterThanEqual(Long utilisateurId, double distance);

    List<Activite> findByUtilisateurId(Long utilisateurId);

}

