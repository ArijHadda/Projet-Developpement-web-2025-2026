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

@Entity
@AllArgsConstructor
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idAct")
    private Long id;

    @NotBlank(message = "Le nom de l'activité est obligatoire")
    @Column(name = "nomAct")
    private String nom;

    @NotNull(message = "La date est obligatoire")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    @Column(name = "dateAct")
    private LocalDate date;

    @Column(name = "conditionsMeteo")
    private String conditionsMeteo;

    @Min(value = 1, message = "La durée est obligatoire et doit être au moins de 1")
    @Column(name = "dureeAct")
    private int duree;

    @PositiveOrZero(message = "La distance ne peut pas être négative")
    @Column(name = "distanceAct")
    private double distance;

    @Min(value = 1, message = "La note doit être au moins de 1")
    @Max(value = 10, message = "La note ne peut pas dépasser 10")
    @Column(name = "noteAct")
    private int note;

    @Min(value = 1, message = "Le niveau d'intensité doit être au moins de 1")
    @Max(value = 5, message = "Le niveau d'intensité ne peut pas dépasser 5")
    @Column(name = "niveauIntensite")
    private int niveauIntensite;

    @Column(name = "caloriesConsommeesAct")
    private int caloriesConsommees;

    @ManyToOne
    @JoinColumn(name = "IdU")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "idSport")
    private Sport sport;

    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> commentaires = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "activite_kudos",
            joinColumns = @JoinColumn(name = "id_activite"),
            inverseJoinColumns = @JoinColumn(name = "id_utilisateur")
    )
    private List<Utilisateur> likers = new ArrayList<>();

    public Activite() {
    }

    public Activite(Long id, String nom, LocalDate date, String conditionsMeteo, int duree, double distance, int note,
            int niveauIntensite, int caloriesConsommees) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.conditionsMeteo = conditionsMeteo;
        this.duree = duree;
        this.distance = distance;
        this.note = note;
        this.niveauIntensite = niveauIntensite;
        this.caloriesConsommees = caloriesConsommees;
    }

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

    public void setSport(Sport sport) {
        this.sport = sport;
    }

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

    public List<Commentaire> getCommentaires() {
        return commentaires;
    }

    @Override
    public String toString() {
        return "Activite [id=" + id + ", nom=" + nom + ", date=" + date + ", conditionsMeteo=" + conditionsMeteo
                + ", duree=" + duree + ", distance=" + distance + ", note=" + note + ", niveauIntensite=" + niveauIntensite
                + ", caloriesConsommees=" + caloriesConsommees + "]";
    }

    
}
