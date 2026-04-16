package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Entité représentant un objectif sportif fixé par un utilisateur.
 * Un objectif peut être basé sur la durée ou la distance pour un sport donné.
 */
@Entity
@Table(name = "Objectif")
public class Objectif {

    /** Identifiant unique de l'objectif */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idObj")
    private Long id;

    /** Titre de l'objectif */
    @NotBlank(message = "Le titre de l'objectif est obligatoire")
    @Column(name = "titreObj")
    private String titre;

    /** Fréquence de l'objectif (ex: QUOTIDIEN, HEBDOMADAIRE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequenceObj")
    private Frequence frequence = Frequence.MENSUEL;

    /** Durée cible de l'objectif en minutes (0 si non définie) */
    @PositiveOrZero(message = "La durée ne peut pas être négative")
    @Column(name = "dureeObj")
    private int duree;

    /** Distance cible de l'objectif en km (0 si non définie) */
    @PositiveOrZero(message = "La distance ne peut pas être négative")
    @Column(name = "distanceObj")
    private double distance;

    /** Utilisateur à qui appartient l'objectif */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idU", nullable = false)
    private Utilisateur utilisateur;

    /** Sport concerné par l'objectif */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sport", nullable = false)
    private Sport sport;

    /** Constructeur par défaut requis par JPA */
    public Objectif() {
    }

    /**
     * Constructeur avec tous les paramètres principaux.
     * @param titre Titre de l'objectif
     * @param frequence Fréquence de répétition
     * @param duree Durée cible
     * @param distance Distance cible
     * @param utilisateur Propriétaire de l'objectif
     * @param sport Sport visé
     */
    public Objectif(String titre, Frequence frequence, int duree, double distance, Utilisateur utilisateur, Sport sport) {
        this.titre = titre;
        this.frequence = frequence;
        this.duree = duree;
        this.distance = distance;
        this.utilisateur = utilisateur;
        this.sport = sport;
    }

    /** @return L'identifiant de l'objectif */
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

    public Frequence getFrequence() { return frequence; }

    public void setFrequence(Frequence frequence) { this.frequence = frequence; }
}