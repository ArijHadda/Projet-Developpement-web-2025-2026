package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;

@Entity
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBadge")
    private Long id;

    @Column(name = "entituleBadge", nullable = false, unique = true)
    private String entitule;

    @Column(name = "imageName", nullable = false)
    private String imageName;

    public Badge() {}

    public Badge(Long id, String entitule, String imageName) {
        this.id = id;
        this.entitule = entitule;
        this.imageName = imageName;
    }

    public Badge(String entitule, String imageName) {
        this.entitule = entitule;
        this.imageName = imageName;
    }

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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String toString() {
        return "Badge [id=" + id + ", entitule=" + entitule + ", imageName=" + imageName + "]";
    }
    
}
