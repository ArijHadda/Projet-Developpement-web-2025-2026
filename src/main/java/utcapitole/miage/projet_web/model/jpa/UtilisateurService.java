package utcapitole.miage.projet_web.model.jpa;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;
import java.util.Optional;

/**
 * Service métier central pour la gestion des utilisateurs.
 * Gère l'inscription, la modification des profils, la sécurité (hashage des mots de passe)
 * et la logique sociale (demandes d'amitié).
 */
@Service
public class UtilisateurService {
    private static final String STATUT_PENDING = "PENDING";

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final DemandeAmiRepository demandeAmiRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, BCryptPasswordEncoder passwordEncoder, DemandeAmiRepository demandeAmiRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.demandeAmiRepository = demandeAmiRepository;
    }

    /**
     * Enregistre un nouvel utilisateur dans le système après avoir sécurisé son mot de passe.
     *
     * @param utilisateur L'utilisateur à inscrire.
     * @return L'utilisateur persisté en base de données.
     */
    public Utilisateur registerUser(Utilisateur utilisateur) {
        String encodedPassword = passwordEncoder.encode(utilisateur.getMdp());
        utilisateur.setMdp(encodedPassword);
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Met à jour les informations personnelles et biométriques d'un utilisateur existant.
     *
     * @param idU L'identifiant de l'utilisateur.
     * @param mailU La nouvelle adresse e-mail.
     * @param sexeU Le sexe.
     * @param ageU L'âge.
     * @param tailleU La taille (en cm).
     * @param poidsU Le poids (en kg).
     * @return L'utilisateur mis à jour.
     */
    public Utilisateur modifierProfile(Long idU, String mailU, String sexeU, int ageU,
                                       float tailleU, float poidsU) {

        Utilisateur user = utilisateurRepository.findById(idU)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        if (!user.getMail().equals(mailU)) {
            Optional<Utilisateur> existingUser = utilisateurRepository.findByMail(mailU);
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre compte.");
            }
        }

        user.setMail(mailU);
        user.setSexe(sexeU);
        user.setAge(ageU);
        user.setTaille(tailleU);
        user.setPoids(poidsU);
        return utilisateurRepository.save(user);
    }

    /**
     * Change le mot de passe d'un utilisateur en vérifiant l'ancien mot de passe
     * et la correspondance de la confirmation.
     *
     * @param idU L'identifiant de l'utilisateur.
     * @param ancienMdp Le mot de passe actuel (en clair).
     * @param nouveauMdp Le nouveau mot de passe souhaité.
     * @param confirmMdp La confirmation du nouveau mot de passe.
     */
    public void changerMotDePasse(Long idU, String ancienMdp, String nouveauMdp, String confirmMdp) {
        Utilisateur user = utilisateurRepository.findById(idU)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));

        if (!passwordEncoder.matches(ancienMdp, user.getMdp())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect.");
        }

        if (!nouveauMdp.equals(confirmMdp)) {
            throw new IllegalArgumentException("Les nouveaux mots de passe ne correspondent pas.");
        }

        if (passwordEncoder.matches(nouveauMdp, user.getMdp())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        user.setMdp(passwordEncoder.encode(nouveauMdp));
        utilisateurRepository.save(user);
    }

    /**
     * Envoie une demande d'amitié à un autre utilisateur.
     * Si une demande inverse est déjà en attente, les deux utilisateurs deviennent automatiquement amis.
     *
     * @param expId L'identifiant de l'expéditeur de la demande.
     * @param destId L'identifiant du destinataire de la demande.
     */
    public void envoyerDemande(Long expId, Long destId) {
        Utilisateur exp = utilisateurRepository.findById(expId).orElseThrow();
        Utilisateur dest = utilisateurRepository.findById(destId).orElseThrow();

        if (exp.getAmis().contains(dest)) {
            throw new IllegalArgumentException("Vous êtes déjà amis !");
        }

        if (demandeAmiRepository.existsByExpediteurAndDestinataireAndStatut(exp, dest, STATUT_PENDING)) {
            throw new IllegalArgumentException("Une demande est déjà en cours.");
        }

        Optional<DemandeAmi> demandeInverse = demandeAmiRepository
                .findByExpediteurAndDestinataireAndStatut(dest, exp, STATUT_PENDING);

        if (demandeInverse.isPresent()) {
            this.accepterDemande(demandeInverse.get().getId());
            return;
        }

        DemandeAmi demande = new DemandeAmi();
        demande.setExpediteur(exp);
        demande.setDestinataire(dest);
        demande.setStatut(STATUT_PENDING);
        demandeAmiRepository.save(demande);
    }

    /**
     * Accepte une demande d'amitié en attente.
     * Lie les deux utilisateurs et supprime la demande de la file d'attente.
     *
     * @param demandeId L'identifiant de la demande d'amitié à accepter.
     */
    @jakarta.transaction.Transactional
    public void accepterDemande(Long demandeId) {
        DemandeAmi demande = demandeAmiRepository.findById(demandeId).orElseThrow();
        Utilisateur exp = demande.getExpediteur();
        Utilisateur dest = demande.getDestinataire();

        exp.addAmi(dest);

        utilisateurRepository.save(exp);
        utilisateurRepository.save(dest);

        demandeAmiRepository.delete(demande);
    }

    /**
     * Refuse (supprime) une demande d'amitié.
     *
     * @param demandeId L'identifiant de la demande à supprimer.
     */
    public void refuserDemande(Long demandeId) {
        demandeAmiRepository.deleteById(demandeId);
    }

    public Optional<Utilisateur> findById(Long idU) {
        return utilisateurRepository.findById(idU);
    }

    public Optional<Utilisateur> findByMail(String mailU) {
        return utilisateurRepository.findByMail(mailU);
    }

    public Optional<Utilisateur> findByNom(String nomU) {
        return utilisateurRepository.findByNom(nomU);
    }

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    /**
     * Recherche des utilisateurs dont le nom ou le prénom correspond (totalement ou partiellement) au mot-clé.
     *
     * @param motCle La chaîne de caractères à rechercher.
     * @return Une liste d'utilisateurs correspondants.
     */
    public List<Utilisateur> rechercherParNomOuPrenom(String motCle) {
        return utilisateurRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(motCle, motCle);
    }

    /**
     * Récupère un utilisateur en forçant l'initialisation de sa collection de niveaux de pratique sportive.
     * Permet d'éviter les exceptions "LazyInitializationException" dans la vue.
     *
     * @param id L'identifiant de l'utilisateur.
     * @return L'utilisateur avec sa liste de sports chargée en mémoire.
     */
    @Transactional
    public Utilisateur getUtilisateurAvecSports(Long id) {
        Utilisateur u = utilisateurRepository.findById(id).orElseThrow();

        // Correction pour forcer le Lazy Loading sans alerter SonarQube (java:S2201)
        u.getListSportNivPratique().forEach(s -> {});

        return u;
    }

    public void save(Utilisateur user) {
        utilisateurRepository.save(user);
    }
}