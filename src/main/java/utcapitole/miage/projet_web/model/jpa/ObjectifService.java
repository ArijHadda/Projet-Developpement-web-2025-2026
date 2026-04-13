package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.dto.ObjectifProgressDTO;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ObjectifService {

    private final ObjectifRepository objectifRepository;
    private final ActiviteRepository activiteRepository;

    public ObjectifService(ObjectifRepository objectifRepository, ActiviteRepository activiteRepository) {
        this.objectifRepository = objectifRepository;
        this.activiteRepository = activiteRepository;
    }

    public List<ObjectifProgressDTO> getObjectifsAvecProgression(Utilisateur user) {
        List<Objectif> objectifs = objectifRepository.findByUtilisateur(user);
        List<ObjectifProgressDTO> resultList = new ArrayList<>();

        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        for (Objectif obj : objectifs) {
            double distActuelle = 0.0;
            double pctDist = 0.0;
            double durActuelle = 0.0;
            double pctDur = 0.0;

            if (obj.getDistance() > 0) {
                distActuelle = activiteRepository.calculerDistanceTotale(
                        user.getId(), obj.getSport().getId(), debutMois, finMois);
                pctDist = (distActuelle / obj.getDistance()) * 100;
                if (pctDist > 100) pctDist = 100;
            }

            if (obj.getDuree() > 0) {
                Long dureeLong = activiteRepository.calculerDureeTotale(
                        user.getId(), obj.getSport().getId(), debutMois, finMois);
                durActuelle = dureeLong != null ? dureeLong.doubleValue() : 0.0;
                pctDur = (durActuelle / obj.getDuree()) * 100;
                if (pctDur > 100) pctDur = 100;
            }

            resultList.add(new ObjectifProgressDTO(obj, distActuelle, pctDist, durActuelle, pctDur));
        }

        return resultList;
    }

    public List<Objectif> getObjectifsByUtilisateur(Utilisateur utilisateur) {
        return objectifRepository.findByUtilisateur(utilisateur);
    }

    public Optional<Objectif> getObjectifById(Long id) {
        return objectifRepository.findById(id);
    }

    public Objectif enregistrerObjectif(Objectif objectif) {
        return objectifRepository.save(objectif);
    }

    public void supprimerObjectif(Long id) {
        objectifRepository.deleteById(id);
    }
}