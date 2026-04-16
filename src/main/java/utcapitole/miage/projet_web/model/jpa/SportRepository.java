package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utcapitole.miage.projet_web.model.Sport;

/**
 * Interface d'accès aux données pour le catalogue de référence des sports.
 */
@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {

    /**
     * Trouve un sport dans le référentiel par son nom exact.
     *
     * @param nom Le nom du sport (ex: "Course", "Cyclisme").
     * @return L'entité Sport correspondante.
     */
    Sport findByNom(String nom);
}