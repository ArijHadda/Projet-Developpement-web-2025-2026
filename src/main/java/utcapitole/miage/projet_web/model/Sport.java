package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "Sport")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSport")
    private Long id;

    @NotBlank(message = "Le nom du sport est obligatoire")
    @Column(name = "nomSport", nullable = false)
    private String nom;

    @NotBlank(message = "Le type de sport est obligatoire")
    @Column(name = "typeSport")
    private String type;

    @PositiveOrZero(message = "L'intensité de base ne peut pas être négative")
    @Column(name = "intensiteBase")
    private double intensiteBase;

    @PositiveOrZero(message = "Le coefficient d'intensité ne peut pas être négatif")
    @Column(name = "coeffIntensite")
    private double coeffIntensite;

    @Column(name = "estBaseSurVitesse")
    private boolean estBaseSurVitesse;

    // Constructeurs
    public Sport() {}

    public Sport(String nom, String type, double intensiteBase, double coeffIntensite, boolean estBaseSurVitesse) {
        this.nom = nom;
        this.type = type;
        this.intensiteBase = intensiteBase;
        this.coeffIntensite = coeffIntensite;
        this.estBaseSurVitesse = estBaseSurVitesse;
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

    public double getIntensiteBase() {
        return intensiteBase;
    }

    public void setIntensiteBase(double intensiteBase) {
        this.intensiteBase = intensiteBase;
    }

    public double getCoeffIntensite() {
        return coeffIntensite;
    }

    public void setCoeffIntensite(double coeffIntensite) {
        this.coeffIntensite = coeffIntensite;
    }

    public boolean isEstBaseSurVitesse() {
        return estBaseSurVitesse;
    }

    public void setEstBaseSurVitesse(boolean estBaseSurVitesse) {
        this.estBaseSurVitesse = estBaseSurVitesse;
    }

    // toString (utile pour debug)
    @Override
    public String toString() {
        return "Sport{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", intensiteBase=" + intensiteBase +
                ", coeffIntensite=" + coeffIntensite +
                ", estBaseSurVitesse=" + estBaseSurVitesse +
                '}';
    }
}

