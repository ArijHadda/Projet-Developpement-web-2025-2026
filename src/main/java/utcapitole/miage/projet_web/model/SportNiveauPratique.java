package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;

/**
 * Entité représentant l'association entre un utilisateur, un sport et son niveau de pratique.
 */
@Entity
@Table(name = "sport_niveau_pratique")
public class SportNiveauPratique {
    /** Identifiant unique de la relation sport-niveau */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Utilisateur concerné */
    @ManyToOne
    @JoinColumn(name = "IdU", nullable = false)
    private Utilisateur utilisateur;

    /** Sport pratiqué */
    @ManyToOne
    @JoinColumn(name = "id_sport", nullable = false)
    private Sport sport;

    /** Niveau de pratique pour ce sport */
    @Enumerated(EnumType.STRING)
    private NiveauPratique niveau;

    /**
     * Constructeur avec paramètres.
     * @param sport Le sport pratiqué
     * @param niveau Le niveau de pratique
     */
    public SportNiveauPratique(Sport sport, NiveauPratique niveau) {
        this.sport = sport;
        this.niveau = niveau;
    }

    /** Constructeur par défaut requis par JPA */
    public SportNiveauPratique(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public NiveauPratique getNiveau() {
        return niveau;
    }

    public void setNiveau(NiveauPratique niveau) {
        this.niveau = niveau;
    }
}
