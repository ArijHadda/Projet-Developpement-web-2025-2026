package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Entité représentant un type de sport.
 * Contient les paramètres nécessaires au calcul des calories.
 */
@Entity
@Table(name = "Sport")
public class Sport {

    /** Identifiant unique du sport */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sport")
    private Long id;
  
    /** Nom du sport (ex: "Running", "Natation") */
    @NotBlank(message = "Le nom du sport est obligatoire")
    //@Column(name = "nomSport", nullable = false)
    @Column(name = "nom_sport", nullable = false)
    private String nom;

    /** Type ou catégorie du sport */
    @NotBlank(message = "Le type de sport est obligatoire")
    //@Column(name = "typeSport")
    @Column(name = "type_sport")
    private String type;

    /** Intensité de base utilisée pour le calcul des calories */
    @PositiveOrZero(message = "L'intensité de base ne peut pas être négative")
    @Column(name = "intensiteBase")
    private Double intensiteBase;

    /** Coefficient d'intensité appliqué lors du calcul */
    @PositiveOrZero(message = "Le coefficient d'intensité ne peut pas être négatif")
    @Column(name = "coeffIntensite")
    private Double coeffIntensite;

    /** Indique si le calcul de l'effort est basé sur la vitesse */
    @Column(name = "estBaseSurVitesse")
    private Boolean estBaseSurVitesse;

    // Constructeurs
    /** Constructeur par défaut requis par JPA */
    public Sport() {
    }

    /**
     * Constructeur avec tous les paramètres de base.
     * @param nom Nom du sport
     * @param type Catégorie du sport
     * @param intensiteBase Intensité initiale
     * @param coeffIntensite Facteur d'intensité
     * @param estBaseSurVitesse Mode de calcul
     */
    public Sport(String nom, String type, Double intensiteBase, Double coeffIntensite, Boolean estBaseSurVitesse) {
        this.nom = nom;
        this.type = type;
        this.intensiteBase = intensiteBase;
        this.coeffIntensite = coeffIntensite;
        this.estBaseSurVitesse = estBaseSurVitesse;
    }

    // Getters & Setters
    public void setId(Long id) {
        this.id = id;
    }
    /** @return L'identifiant du sport */
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

    public void setEstBaseSurVitesse(Boolean estBaseSurVitesse) {
        this.estBaseSurVitesse = estBaseSurVitesse;
    }

    public Boolean getEstBaseSurVitesse() {
        return estBaseSurVitesse;
    }

    // toString (utile pour debug)
    /** @return La représentation textuelle du sport */
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

