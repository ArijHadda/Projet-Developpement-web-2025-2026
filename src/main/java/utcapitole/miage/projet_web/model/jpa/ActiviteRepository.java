package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    List<Activite> findByNom(String nom);

    List<Activite> findByUtilisateurIdOrderByDateDesc(Long id);

    List<Activite> findByUtilisateur(Utilisateur user);

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
    // Trouve les activités d'une liste des amis triées par date la plus récente
    List<Activite> findByUtilisateurInOrderByDateDesc(List<Utilisateur> amis);

    // calculerDistanceTotale pour objectif
    @Query("SELECT COALESCE(SUM(a.distance), 0.0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND a.sport.id = :sportId " +
            "AND a.date >= :debut " +
            "AND a.date <= :fin")
    Double calculerDistanceTotale(@Param("userId") Long userId,
                                  @Param("sportId") Long sportId,
                                  @Param("debut") LocalDate debut,
                                  @Param("fin") LocalDate fin);

    // calculerDureeTotale pour l'objectif
    @Query("SELECT COALESCE(SUM(a.duree), 0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND a.sport.id = :sportId " +
            "AND a.date >= :debut " +
            "AND a.date <= :fin")
    Long calculerDureeTotale(@Param("userId") Long userId,
                             @Param("sportId") Long sportId,
                             @Param("debut") LocalDate debut,
                             @Param("fin") LocalDate fin);

    // Calculer la distance totale de tous les sports pour un utilisateur
    @Query("SELECT COALESCE(SUM(a.distance), 0.0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId")
    Double calculerDistanceTotaleUtilisateur(@Param("userId") Long userId);

    // Calculer la durée totale de musculation pour un utilisateur (toutes dates)
    @Query("SELECT COALESCE(SUM(a.duree), 0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND LOWER(a.sport.nom) = 'musculation'")
    Long calculerDureeMusculation(@Param("userId") Long userId);

}
