package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import utcapitole.miage.projet_web.model.Commentaire;

/**
 * Interface d'accès aux données pour la gestion des entités Commentaires.
 */
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
}
