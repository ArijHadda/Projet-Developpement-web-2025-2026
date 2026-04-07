package utcapitole.miage.projet_web.model.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {
    @Autowired
    private final UtilisateurRepository utilisateurRepository;
    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, BCryptPasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Utilisateur registerUser(Utilisateur utilisateur) {
        String encodedPassword = passwordEncoder.encode(utilisateur.getMdpU());
        utilisateur.setMdpU(encodedPassword);
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur modifierProfile(Long IdU, String mailU,String sexeU,int ageU,
                                       float tailleU, float poidsU){
        Optional<Utilisateur> userOpt= utilisateurRepository.findById(IdU);

        Utilisateur user = userOpt.get();
        user.setMailU(mailU);
        user.setSexeU(sexeU);
        user.setAgeU(ageU);
        user.setTailleU(tailleU);
        user.setPoidsU(poidsU);
        return utilisateurRepository.save(user);

    }

    public Optional<Utilisateur> findByIdU(Long IdU){
        return utilisateurRepository.findById(IdU);
    }

    public Optional<Utilisateur> findByMailU(String mailU){
        return utilisateurRepository.findByMailU(mailU);
    }

    public Optional<Utilisateur> findByNomU(String nomU){
        return utilisateurRepository.findByNomU(nomU);
    }

    public List<Utilisateur> findAll(){
        return utilisateurRepository.findAll();
    }


}
