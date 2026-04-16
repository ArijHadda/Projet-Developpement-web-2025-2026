package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import utcapitole.miage.projet_web.dto.ObjectifProgressDTO;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service métier gérant le cycle de vie et le suivi des objectifs personnels.
 * Calcule dynamiquement la progression des utilisateurs en fonction de la fréquence
 * (quotidienne, hebdomadaire, etc.) et déclenche l'attribution des badges d'accomplissement.
 */
@Service
public class ObjectifService {

    private final ObjectifRepository objectifRepository;
    private final ActiviteRepository activiteRepository;
    private final BadgeAttributionService badgeAttributionService;

    public ObjectifService(ObjectifRepository objectifRepository, ActiviteRepository activiteRepository) {
        this(objectifRepository, activiteRepository, null);
    }

    @Autowired
    public ObjectifService(ObjectifRepository objectifRepository, ActiviteRepository activiteRepository, BadgeAttributionService badgeAttributionService) {
        this.objectifRepository = objectifRepository;
        this.activiteRepository = activiteRepository;
        this.badgeAttributionService = badgeAttributionService;
    }

    /**
     * Calcule la progression actuelle (distance et durée) pour tous les objectifs d'un utilisateur.
     * La période de calcul s'ajuste dynamiquement selon la fréquence de l'objectif (ex: de Lundi à Dimanche pour HEBDOMADAIRE).
     *
     * @param user L'utilisateur dont on veut consulter les objectifs.
     * @return Une liste de DTO contenant les objectifs enrichis de leurs pourcentages de complétion.
     */
    public List<ObjectifProgressDTO> getObjectifsAvecProgression(Utilisateur user) {
        List<Objectif> objectifs = objectifRepository.findByUtilisateur(user);
        List<ObjectifProgressDTO> resultList = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (Objectif obj : objectifs) {
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

            ObjectifProgressDTO dto = new ObjectifProgressDTO(obj, distActuelle, pctDist, durActuelle, pctDur);
            resultList.add(dto);

            if (badgeAttributionService != null && estObjectifComplet(obj, pctDist, pctDur)) {
                badgeAttributionService.attribuerBadgeObjectifComplet(user);
            }
        }

        return resultList;
    }

    /**
     * Récupère la liste brute des objectifs d'un utilisateur (sans calcul de progression).
     *
     * @param utilisateur L'utilisateur ciblé.
     * @return La liste des entités Objectif.
     */
    public List<Objectif> getObjectifsByUtilisateur(Utilisateur utilisateur) {
        return objectifRepository.findByUtilisateur(utilisateur);
    }

    /**
     * Récupère les détails d'un objectif spécifique.
     *
     * @param id L'identifiant de l'objectif.
     * @return Un Optional contenant l'objectif s'il existe.
     */
    public Optional<Objectif> getObjectifById(Long id) {
        return objectifRepository.findById(id);
    }

    /**
     * Sauvegarde un nouvel objectif ou met à jour un objectif existant.
     *
     * @param objectif L'entité Objectif à persister.
     * @return L'objectif sauvegardé.
     */
    public Objectif enregistrerObjectif(Objectif objectif) {
        return objectifRepository.save(objectif);
    }

    /**
     * Supprime un objectif de la base de données.
     *
     * @param id L'identifiant de l'objectif à supprimer.
     */
    public void supprimerObjectif(Long id) {
        objectifRepository.deleteById(id);
    }

    private boolean estObjectifComplet(Objectif obj, double pctDist, double pctDur) {
        if (obj.getDistance() > 0 && obj.getDuree() > 0) {
            return pctDist >= 100 && pctDur >= 100;
        } else if (obj.getDistance() > 0) {
            return pctDist >= 100;
        } else if (obj.getDuree() > 0) {
            return pctDur >= 100;
        }
        return false;
    }
}