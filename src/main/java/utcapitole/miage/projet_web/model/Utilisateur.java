package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Utilisateur")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long IdU;
    private String nomU;
    private String prenomU;
    private String mailU;
    private String mdpU;
    private String sexeU;
    private int ageU;
    private float tailleU;
    private float poidsU;


}
