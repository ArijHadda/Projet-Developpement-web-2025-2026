package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;
import java.util.List;
import java.util.Optional;

public interface DemandeAmiRepository extends JpaRepository<DemandeAmi, Long> {

    // pour les gens qui recoivent les demandes -> afficher les demandes
    List<DemandeAmi> findByDestinataireAndStatut(Utilisateur dest, String statut);

    // pour les gens qui envoient les demandes -> afficher la statut "en anttent"
    List<DemandeAmi> findByExpediteurAndStatut(Utilisateur exp, String statut);

    // pour verifier la repetition (comme A peut just envoyer une demande a B une seule fois avant la demande etre examinee)
    boolean existsByExpediteurAndDestinataireAndStatut(Utilisateur exp, Utilisateur dest, String statut);

    // vrifier s'il y a deja une demande de A a B quand B veut ajouter A (si oui, A et B va devenir amis directement apres B click "Ajouter ami" de A)
    Optional<DemandeAmi> findByExpediteurAndDestinataireAndStatut(Utilisateur exp, Utilisateur dest, String statut);
}
