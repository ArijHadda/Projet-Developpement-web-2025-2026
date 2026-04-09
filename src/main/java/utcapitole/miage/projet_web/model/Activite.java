package utcapitole.miage.projet_web.model;

<<<<<<< HEAD
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
=======
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a

@Entity
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idAct")
    private Long id;

<<<<<<< HEAD
    @Column(name = "nomAct")
    private String nom;

    @Column(name = "dateAct")
    private String date;
=======
    @NotBlank(message = "Le nom de l'activité est obligatoire")
    @Column(name = "nomAct")
    private String nom;

    @NotNull(message = "La date est obligatoire")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    @Column(name = "dateAct")
    private LocalDate date;
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a

    @Column(name = "conditionsMeteo")
    private String conditionsMeteo;

<<<<<<< HEAD
    @Column(name = "dureeAct")
    private int duree;

=======
    @Min(value = 1, message = "La durée est obligatoire et doit être au moins de 1")
    @Column(name = "dureeAct")
    private int duree;

    @PositiveOrZero(message = "La distance ne peut pas être négative")
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a
    @Column(name = "distanceAct")
    private double distance;

    @Column(name = "noteAct")
    private int note;

    @Column(name = "caloriesConsommeesAct")
    private int caloriesConsommees;

<<<<<<< HEAD
    @ManyToOne(fetch = FetchType.LAZY)
=======
    @ManyToOne
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a
    @JoinColumn(name = "IdU")
    private Utilisateur utilisateur;

    public Activite() {
    }

<<<<<<< HEAD
    public Activite(Long id, String nom, String date, int duree, double distance) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.duree = duree;
        this.distance = distance;
=======
    public Activite(Long id, String nom, LocalDate date, String conditionsMeteo, int duree, double distance, int note,
            int caloriesConsommees) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.conditionsMeteo = conditionsMeteo;
        this.duree = duree;
        this.distance = distance;
        this.note = note;
        this.caloriesConsommees = caloriesConsommees;
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a
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

<<<<<<< HEAD
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
=======
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a
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

    @Override
    public String toString() {
        return "Activite [id=" + id + ", nom=" + nom + ", date=" + date + ", conditionsMeteo=" + conditionsMeteo
<<<<<<< HEAD
            + ", duree=" + duree + ", distance=" + distance + ", note=" + note + ", caloriesConsommees="
            + caloriesConsommees + "]";
=======
                + ", duree=" + duree + ", distance=" + distance + ", note=" + note + ", caloriesConsommees="
                + caloriesConsommees + "]";
>>>>>>> 6f98eb3f43cf805934f3cf3f749d9972ae6ef79a
    }

    
}
