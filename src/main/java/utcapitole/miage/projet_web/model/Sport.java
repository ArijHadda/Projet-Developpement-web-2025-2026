package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Sport")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sport")
    private Long id;

    @Column(name = "nom_sport")
    private String nom;

    @Column(name = "type_sport")
    private String type;
    // Constructeurs
    public Sport() {}

    public Sport(String nom, String type) {
        this.nom = nom;
        this.type = type;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // toString (utile pour debug)
    @Override
    public String toString() {
        return "Sport{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

