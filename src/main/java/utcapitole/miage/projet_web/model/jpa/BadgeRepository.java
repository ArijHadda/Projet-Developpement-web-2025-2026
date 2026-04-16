package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utcapitole.miage.projet_web.model.Badge;

import java.util.Optional;

/**
 * Interface de gestion des accès aux données pour l'entité {@link Badge}.
 * Permet la persistance et la récupération des trophées du système.
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    /**
     * Recherche un badge spécifique dans la base de données via son intitulé exact.
     *
     * @param entitule Le nom complet du badge (ex: "10km", "Premier Objectif Complété").
     * @return Un {@link Optional} contenant le badge s'il existe, sinon un Optional vide.
     */
    Optional<Badge> findByEntitule(String entitule);
}