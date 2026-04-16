package utcapitole.miage.projet_web.dto;

/**
 * DTO représentrant une entrée dans le classement des utilisateurs par calories.
 */
public class ClassementDTO {
    /**
     * Nom complet de l'utilisateur.
     */
    private String nomComplet;

    /**
     * Total des calories brûlées par l'utilisateur.
     */
    private Integer totalCalories;

    /**
     * Constructeur de ClassementDTO.
     * @param nomComplet Le nom complet de l'utilisateur
     * @param totalCalories Le total des calories brûlées
     */
    public ClassementDTO(String nomComplet, Integer totalCalories) {
        this.nomComplet = nomComplet;
        this.totalCalories = totalCalories;
    }

    /**
     * Retourne le nom complet de l'utilisateur.
     * @return le nom complet
     */
    public String getNomComplet() { return nomComplet; }

    /**
     * Retourne le total des calories brûlées.
     * @return le total des calories
     */
    public Integer getTotalCalories() { return totalCalories; }
}