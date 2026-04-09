package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "IdU")
    private Long id;

    @Column(name = "nomU")
    private String nom;

    @Column(name = "prenomU")
    private String prenom;

    @Column(name = "mailU")
    private String mail;

    @Column(name = "mdpU")
    private String mdp;

    @Column(name = "sexeU")
    private String sexe;

    @Column(name = "ageU")
    private int age;

    @Column(name = "tailleU")
    private float taille;

    @Column(name = "poidsU")
    private float poids;


    @ManyToMany
    @JoinTable(
            name = "userAmi",
            joinColumns = @JoinColumn(name = "IdU"),
            inverseJoinColumns = @JoinColumn(name = "IdAmis")
    )
    private List<Utilisateur> amis = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "sport_niveau_pratique",
            joinColumns = @JoinColumn(name = "IdU")
    )
    @MapKeyJoinColumn(name = "id_sport")
    @Column(name = "niveau_pratique")
    private Map<Sport, NiveauPratique> listSportNivPratique = new HashMap<>();

    @OneToMany(mappedBy = "createur")
    private List<Challenge> challengesCrees = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    private List<Participation> participations = new ArrayList<>();

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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getTaille() {
        return taille;
    }

    public void setTaille(float taille) {
        this.taille = taille;
    }

    public float getPoids() {
        return poids;
    }

    public void setPoids(float poids) {
        this.poids = poids;
    }


    public List<Utilisateur> getAmis() {
        return amis;
    }

    public void setAmis(List<Utilisateur> amis) {
        this.amis = amis;
    }

    public Map<Sport, NiveauPratique> getListSportNivPratique() {
        return listSportNivPratique;
    }

    public void setListSportNivPratique(Map<Sport, NiveauPratique> listSportNivPratique) {
        this.listSportNivPratique = listSportNivPratique;
    }

    public void addAmi(Utilisateur nouveauAmi) {
        if (this.amis == null) {
            this.amis = new ArrayList<>();
        }
        if (!this.amis.contains(nouveauAmi)) {
            this.amis.add(nouveauAmi);
            nouveauAmi.getAmis().add(this);
        }
    }
}
