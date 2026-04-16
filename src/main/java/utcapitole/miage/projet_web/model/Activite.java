package utcapitole.miage.projet_web.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

/**
 * Entité représentant une activité sportive effectuée par un utilisateur.
 * Contient les détails de l'activité tels que le nom, la date, la durée, la distance, etc.
 */
@Entity
@AllArgsConstructor
public class Activite {

    /** Identifiant unique de l'activité */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idAct")
    private Long id;

    /** Nom de l'activité */
    @NotBlank(message = "Le nom de l'activité est obligatoire")
    @Column(name = "nomAct")
    private String nom;

    /** Date à laquelle l'activité a eu lieu */
    @NotNull(message = "La date est obligatoire")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    @Column(name = "dateAct")
    private LocalDate date;

    /** Conditions météorologiques lors de l'activité */
    @Column(name = "conditionsMeteo")
    private String conditionsMeteo;

    /** Durée de l'activité en minutes */
    @Min(value = 1, message = "La durée est obligatoire et doit être au moins de 1")
    @Column(name = "dureeAct")
    private int duree;

    /** Distance parcourue lors de l'activité (en km) */
    @PositiveOrZero(message = "La distance ne peut pas être négative")
    @Column(name = "distanceAct")
    private double distance;

    /** Note attribuée par l'utilisateur à l'activité (1-10) */
    @Min(value = 1, message = "La note doit être au moins de 1")
    @Max(value = 10, message = "La note ne peut pas dépasser 10")
    @Column(name = "noteAct")
    private int note;

    /** Niveau d'intensité de l'activité (1-5) */
    @Min(value = 1, message = "Le niveau d'intensité doit être au moins de 1")
    @Max(value = 5, message = "Le niveau d'intensité ne peut pas dépasser 5")
    @Column(name = "niveauIntensite")
    private int niveauIntensite;

    /** Calories consommées lors de l'activité */
    @Column(name = "caloriesConsommeesAct")
    private int caloriesConsommees;

    /** Utilisateur/Administrateur ayant effectué l'activité */
    @ManyToOne
    @JoinColumn(name = "IdU")
    private Utilisateur utilisateur;

    /** Type de sport associé à l'activité */
    @ManyToOne
    @JoinColumn(name = "idSport")
    private Sport sport;

    /** Liste des commentaires associés à l'activité */
    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> commentaires = new ArrayList<>();

    /** Liste des utilisateurs ayant donné des kudos à cette activité */
    @ManyToMany
    @JoinTable(
            name = "activite_kudos",
            joinColumns = @JoinColumn(name = "id_activite"),
            inverseJoinColumns = @JoinColumn(name = "id_utilisateur")
    )
    private List<Utilisateur> likers = new ArrayList<>();

    /** Constructeur par défaut sans argument (requis par JPA) */
    public Activite() { // no args constructor
    }

    /** @return L'identifiant de l'activité */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getConditionsMeteo() {
        return conditionsMeteo;
    }

    public void setConditionsMeteo(String conditionsMeteo) {
        this.conditionsMeteo = conditionsMeteo;
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

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public int getNiveauIntensite() {
        return niveauIntensite;
    }

    public void setNiveauIntensite(int niveauIntensite) {
        this.niveauIntensite = niveauIntensite;
    }

    public int getCaloriesConsommees() {
        return caloriesConsommees;
    }

    public void setCaloriesConsommees(int caloriesConsommees) {
        this.caloriesConsommees = caloriesConsommees;
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

    /** @param sport Le sport associé à l'activité */
    public void setSport(Sport sport) {
        this.sport = sport;
    }

    /**
     * Récupère le nombre de kudos.
     * @return Le nombre total d'utilisateurs ayant aimé l'activité.
     */
    public int getNbKudos() {
        return this.likers != null ? this.likers.size() : 0;
    }

    public List<Utilisateur> getLikers() {
        return likers;
    }

    public void setLikers(List<Utilisateur> likers) {
        this.likers = likers;
    }

    public void setCommentaires(List<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    /** @return La liste des commentaires */
    public List<Commentaire> getCommentaires() {
        return commentaires;
    }

    /** @return La représentation textuelle de l'activité */
    @Override
    public String toString() {
        return "Activite [id=" + id + ", nom=" + nom + ", date=" + date + ", conditionsMeteo=" + conditionsMeteo
                + ", duree=" + duree + ", distance=" + distance + ", note=" + note + ", niveauIntensite=" + niveauIntensite
                + ", caloriesConsommees=" + caloriesConsommees + "]";
    }

    
}
