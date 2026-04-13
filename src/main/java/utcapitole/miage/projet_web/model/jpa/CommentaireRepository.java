package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import utcapitole.miage.projet_web.model.Commentaire;

public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
}
