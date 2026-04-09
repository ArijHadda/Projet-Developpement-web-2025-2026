package utcapitole.miage.projet_web.model.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SportNiveauPratiqueService {

    @Autowired
    private SportNiveauPratiqueRepository sportNiveauPratiqueRepository;

    public void deleteById(Long id) {
        sportNiveauPratiqueRepository.deleteById(id);
    }
}
