package utcapitole.miage.projet_web.dto;

import utcapitole.miage.projet_web.model.Objectif;

public class ObjectifProgressDTO {
    private Objectif objectif;

    // 距离的进度
    private double distanceActuelle;
    private double pourcentageDistance;

    // 时长的进度
    private double dureeActuelle;
    private double pourcentageDuree;

    public ObjectifProgressDTO(Objectif objectif, double distanceActuelle, double pourcentageDistance, double dureeActuelle, double pourcentageDuree) {
        this.objectif = objectif;
        this.distanceActuelle = distanceActuelle;
        this.pourcentageDistance = pourcentageDistance;
        this.dureeActuelle = dureeActuelle;
        this.pourcentageDuree = pourcentageDuree;
    }

    // --- 生成这 5 个变量的 Getters ---
    public Objectif getObjectif() { return objectif; }
    public double getDistanceActuelle() { return distanceActuelle; }
    public double getPourcentageDistance() { return pourcentageDistance; }
    public double getDureeActuelle() { return dureeActuelle; }
    public double getPourcentageDuree() { return pourcentageDuree; }
}