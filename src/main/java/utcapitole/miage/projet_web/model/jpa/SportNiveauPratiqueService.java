package utcapitole.miage.projet_web.model.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.SportNiveauPratique;

import java.util.Optional;

@Service
public class SportNiveauPratiqueService {

    @Autowired
    private SportNiveauPratiqueRepository sportNiveauPratiqueRepository;

    public void deleteById(Long id) {
        sportNiveauPratiqueRepository.deleteById(id);
    }

    public Optional<SportNiveauPratique> findByUtilisateurIdAndSportId(Long id, Long sportId) {
        return sportNiveauPratiqueRepository.findByUtilisateurIdAndSportId(id,sportId);
    }

    public void save(SportNiveauPratique sn) {
        sportNiveauPratiqueRepository.save(sn);
    }
}
