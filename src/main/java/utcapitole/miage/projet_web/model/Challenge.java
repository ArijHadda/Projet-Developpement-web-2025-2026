package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Challenge")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idCh")
    private Long id;

    @Column(name = "titreCh", nullable = false)
    private String titre;

    @Column(name = "dateDebutCh", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "dateFinCh", nullable = false)
    private LocalDate dateFin;

    @Column(name = "sportCible")
    private String sportCible;

    // 关联关系 1: 创建者 (créer)
    @ManyToOne
    @JoinColumn(name = "idCreateur")
    private Utilisateur createur;

    // 关联关系 2: 参与者 (rejoindre) -> 指向中间表
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<Participation> participations = new ArrayList<>();
}