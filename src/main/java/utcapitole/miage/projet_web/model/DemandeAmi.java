package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="DemandeAmi")
public class DemandeAmi {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_demande")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_expediteur")
    private Utilisateur expediteur; // qui envoyer le demande

    @ManyToOne
    @JoinColumn(name = "id_destinataire")
    private Utilisateur destinataire; // qui recu le demande

    private String statut; // "PENDING" (anttende), "ACCEPTED" (accepter)
}
