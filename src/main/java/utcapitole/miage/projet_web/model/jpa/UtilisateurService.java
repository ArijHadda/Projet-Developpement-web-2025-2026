package utcapitole.miage.projet_web.model.jpa;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;
import java.util.Optional;

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

    public Utilisateur registerUser(Utilisateur utilisateur) {
        String encodedPassword = passwordEncoder.encode(utilisateur.getMdp());
        utilisateur.setMdp(encodedPassword);
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur modifierProfile(Long idU, String mailU, String sexeU, int ageU,
                                       float tailleU, float poidsU) {

        // Remplacement de .get() par .orElseThrow() (SonarQube java:S3655)
        Utilisateur user = utilisateurRepository.findById(idU)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        user.setMail(mailU);
        user.setSexe(sexeU);
        user.setAge(ageU);
        user.setTaille(tailleU);
        user.setPoids(poidsU);
        return utilisateurRepository.save(user);
    }

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

    public void envoyerDemande(Long expId, Long destId) {
        Utilisateur exp = utilisateurRepository.findById(expId).orElseThrow();
        Utilisateur dest = utilisateurRepository.findById(destId).orElseThrow();

        if (exp.getAmis().contains(dest)) {
            throw new IllegalArgumentException("Vous êtes déjà amis !");
        }

        // Utilisation de la constante STATUT_PENDING
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

    public List<Utilisateur> rechercherParNomOuPrenom(String motCle) {
        return utilisateurRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(motCle, motCle);
    }

    @Transactional
    public Utilisateur getUtilisateurAvecSports(Long id) {
        Utilisateur u = utilisateurRepository.findById(id).orElseThrow();

        // Correction pour forcer le Lazy Loading sans alerter SonarQube (java:S2201)
        // forEach ne retourne rien, donc on n'ignore aucune valeur de retour
        u.getListSportNivPratique().forEach(s -> {});

        return u;
    }

    public void save(Utilisateur user) {
        utilisateurRepository.save(user);
    }
}