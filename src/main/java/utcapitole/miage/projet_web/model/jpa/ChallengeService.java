package utcapitole.miage.projet_web.model.jpa;

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


    private final ChallengeRepository challengeRepository;
    private final ActiviteRepository activiteRepository;
    private final ParticipationRepository participationRepository;

    private static final String MESSAGE_DE_INTROUVABLE = "Challenge introuvable";
    private static final String MESSAGE_NON_AUTORISATION_MODIFIIER = "Non autorisé à modifier ce challenge.";
    private static final String MESSAGE_NON_AUTORISATION_SUPPRIMER = "Non autorisé à supprimer ce challenge.";
    private static final String MESSAGE_REPETITION = "Vous participez déjà à ce challenge !";

    public ChallengeService(ChallengeRepository challengeRepository, ActiviteRepository activiteRepository, ParticipationRepository participationRepository) {
        this.challengeRepository = challengeRepository;
        this.activiteRepository = activiteRepository;
        this.participationRepository = participationRepository;
    }

    public List<ClassementDTO> getClassement(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException(MESSAGE_DE_INTROUVABLE));

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

    public Challenge creerChallenge(Challenge challenge, Utilisateur createur) {
        challenge.setCreateur(createur);
        return challengeRepository.save(challenge);
    }

    public void rejoindreChallenge(Long challengeId, Utilisateur utilisateur) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException(MESSAGE_DE_INTROUVABLE));

        if (participationRepository.existsByUtilisateurAndChallenge(utilisateur, challenge)) {
            throw new RuntimeException(MESSAGE_REPETITION);
        }

        Participation participation = new Participation();
        participation.setChallenge(challenge);
        participation.setUtilisateur(utilisateur);
        participation.setDateInscription(LocalDate.now());

        participationRepository.save(participation);
    }

    public void supprimerChallenge(Long challengeId, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException(MESSAGE_DE_INTROUVABLE));

        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challengeRepository.delete(challenge);
        } else {
            throw new RuntimeException(MESSAGE_NON_AUTORISATION_SUPPRIMER);
        }
    }

    public void modifierTitreChallenge(Long challengeId, String nouveauTitre, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException(MESSAGE_DE_INTROUVABLE));

        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challenge.setTitre(nouveauTitre);
            challengeRepository.save(challenge);
        } else {
            throw new RuntimeException(MESSAGE_NON_AUTORISATION_MODIFIIER);
        }
    }
}