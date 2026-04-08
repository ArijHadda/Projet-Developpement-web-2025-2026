package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utcapitole.miage.projet_web.model.Activite;
import java.util.List;


@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    List<Activite> findByNom(String nom);

    List<Activite> findByUtilisateurIdOrderByDateDesc(Long id);

}
