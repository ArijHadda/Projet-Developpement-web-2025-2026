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

    // 计算某用户、某运动、在特定时间段内的总距离 (返回 Double)
    @Query("SELECT COALESCE(SUM(a.distance), 0.0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND a.sport.id = :sportId " +
            "AND a.date >= :debut " +
            "AND a.date <= :fin")
    Double calculerDistanceTotale(@Param("userId") Long userId,
                                  @Param("sportId") Long sportId,
                                  @Param("debut") LocalDate debut,
                                  @Param("fin") LocalDate fin);

    // 计算某用户、某运动、在特定时间段内的总时长 (注意：SUM 默认返回 Long)
    @Query("SELECT COALESCE(SUM(a.duree), 0) FROM Activite a " +
            "WHERE a.utilisateur.id = :userId " +
            "AND a.sport.id = :sportId " +
            "AND a.date >= :debut " +
            "AND a.date <= :fin")
    Long calculerDureeTotale(@Param("userId") Long userId,
                             @Param("sportId") Long sportId,
                             @Param("debut") LocalDate debut,
                             @Param("fin") LocalDate fin);

}
