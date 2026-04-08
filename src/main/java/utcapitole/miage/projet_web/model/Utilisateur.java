package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany
    @JoinTable(
            name = "userAmi",
            joinColumns = @JoinColumn(name = "IdU"),
            inverseJoinColumns = @JoinColumn(name = "IdAmis")
    )
    private List<Utilisateur> amis = new ArrayList<>();




    public Long getIdU() {
        return IdU;
    }

    public void setIdU(Long idU) {
        IdU = idU;
    }

    public String getNomU() {
        return nomU;
    }

    public void setNomU(String nomU) {
        this.nomU = nomU;
    }

    public String getPrenomU() {
        return prenomU;
    }

    public void setPrenomU(String prenomU) {
        this.prenomU = prenomU;
    }

    public String getMailU() {
        return mailU;
    }

    public void setMailU(String mailU) {
        this.mailU = mailU;
    }

    public String getMdpU() {
        return mdpU;
    }

    public void setMdpU(String mdpU) {
        this.mdpU = mdpU;
    }

    public String getSexeU() {
        return sexeU;
    }

    public void setSexeU(String sexeU) {
        this.sexeU = sexeU;
    }

    public int getAgeU() {
        return ageU;
    }

    public void setAgeU(int ageU) {
        this.ageU = ageU;
    }

    public float getTailleU() {
        return tailleU;
    }

    public void setTailleU(float tailleU) {
        this.tailleU = tailleU;
    }

    public float getPoidsU() {
        return poidsU;
    }

    public void setPoidsU(float poidsU) {
        this.poidsU = poidsU;
    }


    
}
