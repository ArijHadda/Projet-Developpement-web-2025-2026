package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utcapitole.miage.projet_web.model.Sport;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    Sport findByNom(String nom);
}
