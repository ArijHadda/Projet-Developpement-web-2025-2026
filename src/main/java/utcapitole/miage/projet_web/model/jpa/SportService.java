package utcapitole.miage.projet_web.model.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.Sport;

import java.util.List;

@Service
public class SportService {
    @Autowired
    private SportRepository sportRepository;
    public List<Sport> getAll() {
        return sportRepository.findAll();
    }

    public Sport getById(Long id) {
        return sportRepository.findById(id).orElse(null);
    }
}
