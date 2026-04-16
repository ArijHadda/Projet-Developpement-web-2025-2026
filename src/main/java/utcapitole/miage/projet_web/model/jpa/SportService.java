package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.Sport;

import java.util.List;

/**
 * Service fournissant un accès en lecture seule au catalogue des sports disponibles dans l'application.
 */
@Service
public class SportService {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    /**
     * Récupère l'intégralité du catalogue des sports.
     * Utilisé principalement pour alimenter les listes déroulantes (select) dans les formulaires.
     *
     * @return La liste complète des sports.
     */
    public List<Sport> getAll() {
        return sportRepository.findAll();
    }

    /**
     * Récupère un sport en fonction de son identifiant technique.
     *
     * @param id L'identifiant du sport.
     * @return Le sport ciblé, ou null s'il n'est pas trouvé.
     */
    public Sport getById(Long id) {
        return sportRepository.findById(id).orElse(null);
    }
}