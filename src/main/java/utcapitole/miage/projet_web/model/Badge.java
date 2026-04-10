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

    public Badge() {}

    public Badge(Long id, String entitule) {
        this.id = id;
        this.entitule = entitule;
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

    @Override
    public String toString() {
        return "Badge [id=" + id + ", entitule=" + entitule + "]";
    }
    
}
