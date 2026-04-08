package utcapitole.miage.projet_web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idAct")
    private Long id;

    @Column(name = "nomAct")
    private String nom;

    @Column(name = "dateAct")
    private String date;

    @Column(name = "conditionsMeteo")
    private String conditionsMeteo;

    @Column(name = "dureeAct")
    private int duree;

    @Column(name = "distanceAct")
    private double distance;

    @Column(name = "noteAct")
    private int note;

    @Column(name = "caloriesConsommeesAct")
    private int caloriesConsommees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdU")
    private Utilisateur utilisateur;

    public Activite() {
    }

    public Activite(Long id, String nom, String date, int duree, double distance) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.duree = duree;
        this.distance = distance;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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
            + ", duree=" + duree + ", distance=" + distance + ", note=" + note + ", caloriesConsommees="
            + caloriesConsommees + "]";
    }

    
}
