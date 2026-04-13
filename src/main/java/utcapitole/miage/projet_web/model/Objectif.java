package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "Objectif")
public class Objectif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idObj")
    private Long id;

    @NotBlank(message = "Le titre de l'objectif est obligatoire")
    @Column(name = "titreObj")
    private String titre;

    @Column(name = "frequenceObj")
    private String frequence = "Mensuel";

    // La durée objective = 0 signifie qu'il n'y a pas d'objectif ou non prevue
    @PositiveOrZero(message = "La durée ne peut pas être négative")
    @Column(name = "dureeObj")
    private int duree;

    // pareille que durée, peut = 0
    @PositiveOrZero(message = "La distance ne peut pas être négative")
    @Column(name = "distanceObj")
    private double distance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idU", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sport", nullable = false)
    private Sport sport;

    public Objectif() {
    }

    public Objectif(String titre, String frequence, int duree, double distance, Utilisateur utilisateur, Sport sport) {
        this.titre = titre;
        this.frequence = frequence;
        this.duree = duree;
        this.distance = distance;
        this.utilisateur = utilisateur;
        this.sport = sport;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getFrequence() {
        return frequence;
    }

    public void setFrequence(String frequence) {
        this.frequence = frequence;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }
}