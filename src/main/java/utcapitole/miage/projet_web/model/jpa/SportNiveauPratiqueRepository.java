package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import utcapitole.miage.projet_web.model.SportNiveauPratique;

public interface SportNiveauPratiqueRepository extends JpaRepository<SportNiveauPratique, Long> {
    void deleteById(Long id);

}
