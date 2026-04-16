package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface de gestion des accès aux données pour l'entité {@link Activite}.
 * Fournit les requêtes personnalisées nécessaires pour le calcul des statistiques,
 * l'évaluation des objectifs et les classements des challenges.
 */
@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    List<Activite> findByNom(String nom);

    List<Activite> findByUtilisateurIdOrderByDateDesc(Long id);

    List<Activite> findByUtilisateur(Utilisateur user);

    /**
     * Calcule la somme totale des calories brûlées par un utilisateur pour un sport spécifique,
     * dans un intervalle de temps donné. Utilisé pour générer le classement des challenges.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @param sportCible Le nom du sport ciblé par le challenge.
     * @param dateDebut La date de début du challenge.
     * @param dateFin La date de fin du challenge.
     * @return Le total des calories consommées (retourne 0 si aucune activité n'est trouvée).
     */
    @Query("SELECT COALESCE(SUM(a.caloriesConsommees), 0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND a.nom = :sportCible " +
            "AND a.date >= :dateDebut " +
            "AND a.date <= :dateFin")
    Integer calculerCaloriesPourChallenge(
            @Param("userId") Long userId,
            @Param("sportCible") String sportCible,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    boolean existsByUtilisateurIdAndDistanceGreaterThanEqual(Long utilisateurId, double distance);

    List<Activite> findByUtilisateurId(Long utilisateurId);

    /**
     * Récupère le flux d'activités récentes pour un groupe d'amis.
     *
     * @param amis La liste des utilisateurs amis.
     * @return Une liste d'activités triée par date décroissante (les plus récentes en premier).
     */
    List<Activite> findByUtilisateurInOrderByDateDesc(List<Utilisateur> amis);

    /**
     * Calcule la distance cumulée pour un utilisateur et un sport donnés sur une période.
     * Utilisé pour calculer la progression des objectifs personnels (Distance).
     *
     * @param userId L'identifiant de l'utilisateur.
     * @param sportId L'identifiant du sport.
     * @param debut Date de début de l'objectif.
     * @param fin Date de fin de l'objectif.
     * @return La distance totale en kilomètres (retourne 0.0 si aucune activité n'est trouvée).
     */
    @Query("SELECT COALESCE(SUM(a.distance), 0.0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND a.sport.id = :sportId " +
            "AND a.date >= :debut " +
            "AND a.date <= :fin")
    Double calculerDistanceTotale(@Param("userId") Long userId,
                                  @Param("sportId") Long sportId,
                                  @Param("debut") LocalDate debut,
                                  @Param("fin") LocalDate fin);

    /**
     * Calcule la durée cumulée pour un utilisateur et un sport donnés sur une période.
     * Utilisé pour calculer la progression des objectifs personnels (Durée).
     *
     * @param userId L'identifiant de l'utilisateur.
     * @param sportId L'identifiant du sport.
     * @param debut Date de début de l'objectif.
     * @param fin Date de fin de l'objectif.
     * @return La durée totale en minutes (retourne 0 si aucune activité n'est trouvée).
     */
    @Query("SELECT COALESCE(SUM(a.duree), 0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND a.sport.id = :sportId " +
            "AND a.date >= :debut " +
            "AND a.date <= :fin")
    Long calculerDureeTotale(@Param("userId") Long userId,
                             @Param("sportId") Long sportId,
                             @Param("debut") LocalDate debut,
                             @Param("fin") LocalDate fin);

    /**
     * Calcule la distance totale parcourue par un utilisateur depuis son inscription, tous sports confondus.
     * Utilisé pour l'attribution automatique des badges de distance (10km, 25km, etc.).
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return La distance totale historique en kilomètres.
     */
    @Query("SELECT COALESCE(SUM(a.distance), 0.0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId")
    Double calculerDistanceTotaleUtilisateur(@Param("userId") Long userId);

    /**
     * Calcule la durée totale passée à faire de la musculation par un utilisateur depuis son inscription.
     * Utilisé pour l'attribution automatique des badges de musculation (10h, 25h, etc.).
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return La durée totale en minutes.
     */
    @Query("SELECT COALESCE(SUM(a.duree), 0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND LOWER(a.sport.nom) = 'musculation'")
    Long calculerDureeMusculation(@Param("userId") Long userId);
}