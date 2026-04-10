package utcapitole.miage.projet_web.model.jpa;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.DemandeAmi;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {
    @Autowired
    private final UtilisateurRepository utilisateurRepository;
    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;
    @Autowired
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

    public Utilisateur modifierProfile(Long IdU, String mailU,String sexeU,int ageU,
                                       float tailleU, float poidsU, String niveauPratique){
        Optional<Utilisateur> userOpt= utilisateurRepository.findById(IdU);

        Utilisateur user = userOpt.get();
        user.setMail(mailU);
        user.setSexe(sexeU);
        user.setAge(ageU);
        user.setTaille(tailleU);
        user.setPoids(poidsU);
        return utilisateurRepository.save(user);

    }

    public void changerMotDePasse(Long idU, String ancienMdp, String nouveauMdp, String confirmMdp) {
        //findById retoune optinal, .orElseThrow signifie si user est null, il va creer RuntimeException et retourne message.
        Utilisateur user = utilisateurRepository.findById(idU)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));
        //comparer ancienmdp et input
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

        if (demandeAmiRepository.existsByExpediteurAndDestinataireAndStatut(exp, dest, "PENDING")) {
            throw new IllegalArgumentException("Une demande est déjà en cours.");
        }

        //vrifier s'il y a deja une demande de A a B quand B veut ajouter A
        // si oui, A et B va devenir amis directement apres B click "Ajouter ami" de A
        Optional<DemandeAmi> demandeInverse = demandeAmiRepository
                .findByExpediteurAndDestinataireAndStatut(dest, exp, "PENDING");

        if (demandeInverse.isPresent()) {
            this.accepterDemande(demandeInverse.get().getId());
            return;
        }

        DemandeAmi demande = new DemandeAmi();
        demande.setExpediteur(exp);
        demande.setDestinataire(dest);
        demande.setStatut("PENDING");
        demandeAmiRepository.save(demande);
    }

    // @jakarta.transaction.Transactional soit toutes les actions de BD succes, soit annuler tout
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

    public Optional<Utilisateur> findById(Long IdU){
        return utilisateurRepository.findById(IdU);
    }

    public Optional<Utilisateur> findByMail(String mailU){
        return utilisateurRepository.findByMail(mailU);
    }

    public Optional<Utilisateur> findByNom(String nomU){
        return utilisateurRepository.findByNom(nomU);
    }

    public List<Utilisateur> findAll(){
        return utilisateurRepository.findAll();
    }

    public List<Utilisateur> rechercherParNomOuPrenom(String motCle) {
        return utilisateurRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(motCle, motCle);
    }

    @Transactional
    public Utilisateur getUtilisateurAvecSports(Long id) {
        Utilisateur u = utilisateurRepository.findById(id).orElseThrow();
        u.getListSportNivPratique().size(); // force le chargement
        return u;
    }

    public void save(Utilisateur user) {
        utilisateurRepository.save(user);
    }
}
