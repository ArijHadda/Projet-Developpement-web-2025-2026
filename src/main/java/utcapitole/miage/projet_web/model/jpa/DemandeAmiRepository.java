package utcapitole.miage.projet_web.model.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;
import java.util.List;
import java.util.Optional;

/**
 * Interface de gestion des accès aux données pour les demandes d'amitié.
 */
public interface DemandeAmiRepository extends JpaRepository<DemandeAmi, Long> {

    /**
     * Récupère les demandes reçues par un utilisateur ayant un statut spécifique (ex: "en attente").
     *
     * @param dest L'utilisateur destinataire.
     * @param statut Le statut de la demande.
     * @return La liste des demandes correspondantes.
     */
    List<DemandeAmi> findByDestinataireAndStatut(Utilisateur dest, String statut);

    /**
     * Récupère les demandes envoyées par un utilisateur ayant un statut spécifique.
     *
     * @param exp L'utilisateur expéditeur.
     * @param statut Le statut de la demande.
     * @return La liste des demandes correspondantes.
     */
    List<DemandeAmi> findByExpediteurAndStatut(Utilisateur exp, String statut);

    /**
     * Vérifie l'existence d'une demande avec un statut donné entre deux utilisateurs.
     * Permet d'éviter les spams ou les demandes en double.
     *
     * @param exp L'expéditeur.
     * @param dest Le destinataire.
     * @param statut Le statut ciblé.
     * @return true si une telle demande existe, false sinon.
     */
    boolean existsByExpediteurAndDestinataireAndStatut(Utilisateur exp, Utilisateur dest, String statut);

    /**
     * Cherche une demande spécifique entre deux utilisateurs. Utilisé pour
     * gérer les acceptations croisées ou mutuelles d'amitié.
     *
     * @param exp L'expéditeur initial.
     * @param dest Le destinataire initial.
     * @param statut Le statut ciblé.
     * @return Un Optional contenant la demande si elle existe.
     */
    Optional<DemandeAmi> findByExpediteurAndDestinataireAndStatut(Utilisateur exp, Utilisateur dest, String statut);
}