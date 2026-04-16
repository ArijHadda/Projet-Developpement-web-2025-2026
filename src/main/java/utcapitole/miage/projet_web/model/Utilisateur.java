package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un utilisateur du système.
 * Gère les informations personnelles, les amis, les activités et les accomplissements.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Utilisateur")
public class Utilisateur {

    /** Identifiant unique de l'utilisateur */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "IdU")
    private Long id;

    /** Nom de famille de l'utilisateur */
    @Column(name = "nomU")
    private String nom;

    /** Prénom de l'utilisateur */
    @Column(name = "prenomU")
    private String prenom;

    /** Adresse e-mail (utilisée pour la connexion) */
    @Column(name = "mailU")
    private String mail;

    /** Mot de passe haché */
    @Column(name = "mdpU")
    private String mdp;

    /** Sexe de l'utilisateur */
    @Column(name = "sexeU")
    private String sexe;

    /** Âge de l'utilisateur */
    @Column(name = "ageU")
    private int age;

    /** Taille en cm */
    @Column(name = "tailleU")
    private float taille;

    /** Poids en kg */
    @Column(name = "poidsU")
    private float poids;


    /** Liste des amis de l'utilisateur */
    @ManyToMany
    @JoinTable(
            name = "userAmi",
            joinColumns = @JoinColumn(name = "IdU"),
            inverseJoinColumns = @JoinColumn(name = "IdAmis")
    )
    private List<Utilisateur> amis = new ArrayList<>();

    /** Niveaux de pratique pour différents sports */
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SportNiveauPratique> listSportNivPratique = new ArrayList<>();


    /** Activités sportives enregistrées par l'utilisateur */
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activite> activites = new ArrayList<>();

    /** Badges remportés par l'utilisateur */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "utilisateur_badges",
        joinColumns = @JoinColumn(name = "IdU"),
        inverseJoinColumns = @JoinColumn(name = "idBadge")
    )
    private List<Badge> badges = new ArrayList<>();

    /** Défis (Challenges) créés par cet utilisateur */
    @OneToMany(mappedBy = "createur")
    private List<Challenge> challengesCrees = new ArrayList<>();

    /** Participations de l'utilisateur à divers défis */
    @OneToMany(mappedBy = "utilisateur")
    private List<Participation> participations = new ArrayList<>();

    /** Objectifs fixés par l'utilisateur */
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Objectif> objectifs = new ArrayList<>();

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

    public List<SportNiveauPratique> getListSportNivPratique() {
        return listSportNivPratique;
    }

    public void setListSportNivPratique(List<SportNiveauPratique> listSportNivPratique) {
        this.listSportNivPratique = listSportNivPratique;
    }


    public List<Activite> getActivites() {
        return activites;
    }

    public void setActivites(List<Activite> activites) {
        this.activites = activites;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public void setBadges(List<Badge> badges) {
        this.badges = badges;
    }
    /**
     * Ajoute un nouvel ami à la liste de l'utilisateur.
     * @param nouveauAmi L'utilisateur à ajouter en tant qu'ami.
     */
    public void addAmi(Utilisateur nouveauAmi) {
        if (this.amis == null) {
            this.amis = new ArrayList<>();
        }
        if (!this.amis.contains(nouveauAmi)) {
            this.amis.add(nouveauAmi);
            nouveauAmi.getAmis().add(this);
        }
    }
    public List<Objectif> getObjectifs() {
        return objectifs;
    }

    public void setObjectifs(List<Objectif> objectifs) {
        this.objectifs = objectifs;
    }
}