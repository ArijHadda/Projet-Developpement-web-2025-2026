package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;
import java.util.Optional;

/**
 * Interface d'accès aux données pour l'entité Utilisateur.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /**
     * Recherche un utilisateur par son adresse e-mail exacte.
     * Utile pour le système d'authentification.
     *
     * @param mailU L'adresse e-mail à chercher.
     * @return Un Optional contenant l'utilisateur s'il est trouvé.
     */
    Optional<Utilisateur> findByMail(String mailU);

    Optional<Utilisateur> findByNom(String nomU);

    /**
     * Effectue une recherche globale sur les noms et prénoms.
     *
     * @param nom La chaîne à chercher dans les noms.
     * @param prenom La chaîne à chercher dans les prénoms.
     * @return La liste des utilisateurs correspondant aux critères (insensible à la casse).
     */
    List<Utilisateur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);
}