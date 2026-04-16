package utcapitole.miage.projet_web.dto;

import utcapitole.miage.projet_web.model.Objectif;

/**
 * DTO représentant la progression d'un utilisateur vers un objectif spécifique.
 */
public class ObjectifProgressDTO {
    /**
     * L'objectif concerné.
     */
    private Objectif objectif;

    /**
     * La distance parcourue actuellement pour cet objectif.
     */
    private double distanceActuelle;

    /**
     * Le pourcentage d'accomplissement de l'objectif en termes de distance.
     */
    private double pourcentageDistance;

    /**
     * La durée cumulée actuellement pour cet objectif.
     */
    private double dureeActuelle;

    /**
     * Le pourcentage d'accomplissement de l'objectif en termes de durée.
     */
    private double pourcentageDuree;

    /**
     * Constructeur de ObjectifProgressDTO.
     * @param objectif L'objectif concerné
     * @param distanceActuelle La distance actuelle parcourue
     * @param pourcentageDistance Le pourcentage de progression en distance
     * @param dureeActuelle La durée actuelle cumulée
     * @param pourcentageDuree Le pourcentage de progression en durée
     */
    public ObjectifProgressDTO(Objectif objectif, double distanceActuelle, double pourcentageDistance, double dureeActuelle, double pourcentageDuree) {
        this.objectif = objectif;
        this.distanceActuelle = distanceActuelle;
        this.pourcentageDistance = pourcentageDistance;
        this.dureeActuelle = dureeActuelle;
        this.pourcentageDuree = pourcentageDuree;
    }

    /**
     * Retourne l'objectif.
     * @return l'objectif
     */
    public Objectif getObjectif() { return objectif; }

    /**
     * Retourne la distance actuelle.
     * @return la distance parcourue
     */
    public double getDistanceActuelle() { return distanceActuelle; }

    /**
     * Retourne le pourcentage de distance.
     * @return le pourcentage de progression (distance)
     */
    public double getPourcentageDistance() { return pourcentageDistance; }

    /**
     * Retourne la durée actuelle.
     * @return la durée cumulée
     */
    public double getDureeActuelle() { return dureeActuelle; }

    /**
     * Retourne le pourcentage de durée.
     * @return le pourcentage de progression (durée)
     */
    public double getPourcentageDuree() { return pourcentageDuree; }
}