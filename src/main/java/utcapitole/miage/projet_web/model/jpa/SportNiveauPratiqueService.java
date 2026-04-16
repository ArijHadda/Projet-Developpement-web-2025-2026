package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.SportNiveauPratique;

import java.util.Optional;

/**
 * Service métier gérant le paramétrage des niveaux de pratique des utilisateurs (ex: Débutant, Expert)
 * pour les différents sports du catalogue.
 */
@Service
public class SportNiveauPratiqueService {

    private final SportNiveauPratiqueRepository sportNiveauPratiqueRepository;

    public SportNiveauPratiqueService(SportNiveauPratiqueRepository sportNiveauPratiqueRepository) {
        this.sportNiveauPratiqueRepository = sportNiveauPratiqueRepository;
    }

    /**
     * Supprime une configuration de niveau de pratique existante.
     *
     * @param id L'identifiant de l'entité SportNiveauPratique à supprimer.
     */
    public void deleteById(Long id) {
        sportNiveauPratiqueRepository.deleteById(id);
    }

    /**
     * Cherche le niveau qu'a défini un utilisateur pour un sport particulier.
     *
     * @param id L'identifiant de l'utilisateur.
     * @param sportId L'identifiant du sport.
     * @return Un Optional de l'entité SportNiveauPratique.
     */
    public Optional<SportNiveauPratique> findByUtilisateurIdAndSportId(Long id, Long sportId) {
        return sportNiveauPratiqueRepository.findByUtilisateurIdAndSportId(id,sportId);
    }

    /**
     * Enregistre ou met à jour le niveau de pratique d'un utilisateur pour un sport.
     *
     * @param sn L'entité à sauvegarder.
     */
    public void save(SportNiveauPratique sn) {
        sportNiveauPratiqueRepository.save(sn);
    }
}