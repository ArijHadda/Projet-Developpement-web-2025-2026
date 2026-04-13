package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Participation")
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idParticipation")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idU")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "idCh")
    private Challenge challenge;

    @Column(name = "dateInscription")
    private LocalDate dateInscription;
}