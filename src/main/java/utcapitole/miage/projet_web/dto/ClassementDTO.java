package utcapitole.miage.projet_web.dto;

public class ClassementDTO {
    private String nomComplet;
    private Integer totalCalories;

    public ClassementDTO(String nomComplet, Integer totalCalories) {
        this.nomComplet = nomComplet;
        this.totalCalories = totalCalories;
    }

    public String getNomComplet() { return nomComplet; }
    public Integer getTotalCalories() { return totalCalories; }
}