package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un défi (challenge) lancé aux utilisateurs.
 * Un défi a une durée déterminée et concerne un sport spécifique.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Challenge")
public class Challenge {

    /** Identifiant unique du défi */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idCh")
    private Long id;

    /** Titre ou nom du défi */
    @Column(name = "titreCh", nullable = false)
    private String titre;

    /** Date de début du défi */
    @Column(name = "dateDebutCh", nullable = false)
    private LocalDate dateDebut;

    /** Date de fin du défi */
    @Column(name = "dateFinCh", nullable = false)
    private LocalDate dateFin;

    /** Type de sport concerné par le défi (ex: "Running", "Cyclisme") */
    @Column(name = "sportCible")
    private String sportCible;

    /** Utilisateur ayant créé le défi */
    @ManyToOne
    @JoinColumn(name = "idCreateur")
    private Utilisateur createur;

    /** Liste des participations des utilisateurs à ce défi */
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<Participation> participations = new ArrayList<>();
}