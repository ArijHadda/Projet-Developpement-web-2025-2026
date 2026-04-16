package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entité représentant la participation d'un utilisateur à un défi (Challenge).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Participation")
public class Participation {

    /** Identifiant unique de la participation */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idParticipation")
    private Long id;

    /** Utilisateur participant au défi */
    @ManyToOne
    @JoinColumn(name = "idU")
    private Utilisateur utilisateur;

    /** Défi (Challenge) auquel l'utilisateur participe */
    @ManyToOne
    @JoinColumn(name = "idCh")
    private Challenge challenge;

    /** Date à laquelle l'utilisateur s'est inscrit au défi */
    @Column(name = "dateInscription")
    private LocalDate dateInscription;
}