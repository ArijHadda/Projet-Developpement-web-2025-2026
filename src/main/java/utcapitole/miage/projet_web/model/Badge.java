package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;

/**
 * Entité représentant un badge de récompense.
 * Les badges sont attribués aux utilisateurs pour leurs accomplissements.
 */
@Entity
public class Badge {

    /** Identifiant unique du badge */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBadge")
    private Long id;

    /** Intitulé ou nom du badge */
    @Column(name = "entituleBadge", nullable = false, unique = true)
    private String entitule;

    /** Nom du fichier image associé au badge */
    @Column(name = "imageName", nullable = false)
    private String imageName;

    /** Constructeur par défaut requis par JPA */
    public Badge() {}

    /**
     * Constructeur avec tous les champs.
     * @param id Identifiant du badge
     * @param entitule Nom du badge
     * @param imageName Nom de l'image
     */
    public Badge(Long id, String entitule, String imageName) {
        this.id = id;
        this.entitule = entitule;
        this.imageName = imageName;
    }

    /**
     * Constructeur utile pour la création d'un nouveau badge.
     * @param entitule Nom du badge
     * @param imageName Nom de l'image
     */
    public Badge(String entitule, String imageName) {
        this.entitule = entitule;
        this.imageName = imageName;
    }

    /** @return L'identifiant du badge */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntitule() {
        return entitule;
    }

    public void setEntitule(String entitule) {
        this.entitule = entitule;
    }

    /** @return Le nom du fichier image */
    public String getImageName() {
        return imageName;
    }

    /** @param imageName Le nom du fichier image à définir */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /** @return La représentation textuelle du badge */
    @Override
    public String toString() {
        return "Badge [id=" + id + ", entitule=" + entitule + ", imageName=" + imageName + "]";
    }
    
}
