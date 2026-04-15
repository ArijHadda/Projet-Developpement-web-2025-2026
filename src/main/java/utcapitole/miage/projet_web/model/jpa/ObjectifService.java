package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.dto.ObjectifProgressDTO;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
        List<Objectif> objectifs = objectifRepository.findByUtilisateur(user); // [cite: 3, 4]
        List<ObjectifProgressDTO> resultList = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (Objectif obj : objectifs) {
            // --- Logique centrale : calculer les dates de début et de fin du cycle actuel selon la fréquence ---
            LocalDate dateDebut;
            LocalDate dateFin;

            switch (obj.getFrequence()) {
                case QUOTIDIEN:
                    dateDebut = today;
                    dateFin = today;
                    break;
                case HEBDOMADAIRE:
                    dateDebut = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    dateFin = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    break;
                case ANNUEL:
                    dateDebut = today.with(TemporalAdjusters.firstDayOfYear());
                    dateFin = today.with(TemporalAdjusters.lastDayOfYear());
                    break;
                case MENSUEL:
                default:
                    dateDebut = today.withDayOfMonth(1);
                    dateFin = today.withDayOfMonth(today.lengthOfMonth());
                    break;
            }

            double distActuelle = 0.0;
            double pctDist = 0.0;
            double durActuelle = 0.0;
            double pctDur = 0.0;

            if (obj.getDistance() > 0) {
                distActuelle = activiteRepository.calculerDistanceTotale(
                        user.getId(), obj.getSport().getId(), dateDebut, dateFin);
                pctDist = Math.min((distActuelle / obj.getDistance()) * 100, 100);
            }

            if (obj.getDuree() > 0) {
                Long dureeLong = activiteRepository.calculerDureeTotale(
                        user.getId(), obj.getSport().getId(), dateDebut, dateFin);
                durActuelle = dureeLong != null ? dureeLong.doubleValue() : 0.0;
                pctDur = Math.min((durActuelle / obj.getDuree()) * 100, 100);
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