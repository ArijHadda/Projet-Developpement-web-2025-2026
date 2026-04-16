package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant une demande d'ami entre deux utilisateurs.
 * Gère l'expéditeur, le destinataire et le statut de la demande.
 */
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="DemandeAmi")
public class DemandeAmi {
    /** Identifiant unique de la demande d'ami */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_demande")
    private Long id;

    /** Utilisateur qui envoie la demande d'ami */
    @ManyToOne
    @JoinColumn(name = "id_expediteur")
    private Utilisateur expediteur;

    /** Utilisateur qui reçoit la demande d'ami */
    @ManyToOne
    @JoinColumn(name = "id_destinataire")
    private Utilisateur destinataire;

    /** Statut de la demande (ex: "PENDING", "ACCEPTED") */
    private String statut;
}
