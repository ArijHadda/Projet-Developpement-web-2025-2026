package utcapitole.miage.projet_web.model.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.dto.ClassementDTO;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Participation;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ActiviteRepository activiteRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    public List<ClassementDTO> getClassement(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge introuvable"));

        List<ClassementDTO> classement = new ArrayList<>();

        for (Participation p : challenge.getParticipations()) {
            Long userId = p.getUtilisateur().getId();

            Integer calories = activiteRepository.calculerCaloriesPourChallenge(
                    userId,
                    challenge.getSportCible(),
                    challenge.getDateDebut(),
                    challenge.getDateFin()
            );

            String nomComplet = p.getUtilisateur().getPrenom() + " " + p.getUtilisateur().getNom();
            classement.add(new ClassementDTO(nomComplet, calories));
        }

        classement.sort((c1, c2) -> c2.getTotalCalories().compareTo(c1.getTotalCalories()));

        return classement;
    }

    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    // ================= 2. 创建挑战 =================
    public Challenge creerChallenge(Challenge challenge, Utilisateur createur) {
        challenge.setCreateur(createur); // 绑定创建者
        return challengeRepository.save(challenge);
    }

    // ================= 3. 加入挑战 =================
    public void rejoindreChallenge(Long challengeId, Utilisateur utilisateur) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge introuvable"));

        // 检查是否已经加入过，防止重复报名
        if (participationRepository.existsByUtilisateurAndChallenge(utilisateur, challenge)) {
            throw new RuntimeException("Vous participez déjà à ce challenge !");
        }

        // 创建新的参与记录
        Participation participation = new Participation();
        participation.setChallenge(challenge);
        participation.setUtilisateur(utilisateur);
        participation.setDateInscription(LocalDate.now()); // 记录报名时间

        participationRepository.save(participation);
    }

    public void supprimerChallenge(Long challengeId, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge introuvable"));

        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challengeRepository.delete(challenge);
        } else {
            throw new RuntimeException("Non autorisé à supprimer ce challenge.");
        }
    }

    public void modifierTitreChallenge(Long challengeId, String nouveauTitre, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge introuvable"));

        // 权限校验：判断当前登录用户是不是这个挑战的创建者
        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challenge.setTitre(nouveauTitre); // 只修改标题
            challengeRepository.save(challenge);
        } else {
            throw new RuntimeException("Non autorisé à modifier ce challenge.");
        }
    }
}