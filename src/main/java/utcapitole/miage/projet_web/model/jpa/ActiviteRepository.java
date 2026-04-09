package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
<<<<<<< HEAD
import utcapitole.miage.projet_web.model.Activite;

import java.util.List;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    List<Activite> findByNom(String nom);
    List<Activite> findByUtilisateurIdOrderByDateDesc(Long id);
    boolean existsByUtilisateurIdAndDistanceGreaterThanEqual(Long utilisateurId, double distance);
    List<Activite> findByUtilisateurId(Long utilisateurId);
=======

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;


@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    List<Activite> findByNom(String nom);

    List<Activite> findByUtilisateurIdOrderByDateDesc(Long id);

    List<Activite> findByUtilisateur(Utilisateur user);

>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a
}
