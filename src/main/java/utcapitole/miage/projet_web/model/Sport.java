package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "Sport")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sport")
    private Long id;
  
    @NotBlank(message = "Le nom du sport est obligatoire")
    //@Column(name = "nomSport", nullable = false)
    @Column(name = "nom_sport", nullable = false)
    private String nom;

    @NotBlank(message = "Le type de sport est obligatoire")
    //@Column(name = "typeSport")
    @Column(name = "type_sport")
    private String type;

    @PositiveOrZero(message = "L'intensité de base ne peut pas être négative")
    @Column(name = "intensiteBase")
    private Double intensiteBase;

    @PositiveOrZero(message = "Le coefficient d'intensité ne peut pas être négatif")
    @Column(name = "coeffIntensite")
    private Double coeffIntensite;

    @Column(name = "estBaseSurVitesse")
    private Boolean estBaseSurVitesse;

    // Constructeurs
    public Sport() {
        this.intensiteBase = 0.0;
        this.coeffIntensite = 0.0;
        this.estBaseSurVitesse = false;
    }

    public Sport(String nom, String type, Double intensiteBase, Double coeffIntensite, Boolean estBaseSurVitesse) {
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

    public Double getIntensiteBase() {
        return intensiteBase;
    }

    public void setIntensiteBase(Double intensiteBase) {
        this.intensiteBase = intensiteBase;
    }

    public Double getCoeffIntensite() {
        return coeffIntensite;
    }

    public void setCoeffIntensite(Double coeffIntensite) {
        this.coeffIntensite = coeffIntensite;
    }

   /* public Boolean isEstBaseSurVitesse() {
        return estBaseSurVitesse;
    }*/

    public void setEstBaseSurVitesse(Boolean estBaseSurVitesse) {
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

    public Boolean getEstBaseSurVitesse() {
        return estBaseSurVitesse;
    }
}

