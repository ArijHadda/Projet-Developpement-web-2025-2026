package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sport_niveau_pratique")
public class SportNiveauPratique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "IdU", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "id_sport", nullable = false)
    private Sport sport;

    @Enumerated(EnumType.STRING)
    private NiveauPratique niveau;

    public SportNiveauPratique(Sport s, NiveauPratique niveau) {
        this.sport = s;
        this.niveau = niveau;
    }

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
