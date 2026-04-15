package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.Sport;

import java.util.List;

@Service
public class SportService {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    public List<Sport> getAll() {
        return sportRepository.findAll();
    }

    public Sport getById(Long id) {
        return sportRepository.findById(id).orElse(null);
    }
}
