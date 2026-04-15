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

    @Autowired
    private BadgeAttributionService badgeAttributionService;

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

        // Attribuer le badge au top 1 si le challenge est termin�
        verifierEtAttribuerBadgeChallengeGagne(challenge, classement);

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
                .orElseThrow(() -> new RuntimeException("Challenge introuvable"));

        if (participationRepository.existsByUtilisateurAndChallenge(utilisateur, challenge)) {
            throw new RuntimeException("Vous participez d\u00e9j\u00e0 \u00e0 ce challenge !");
        }

        Participation participation = new Participation();
        participation.setChallenge(challenge);
        participation.setUtilisateur(utilisateur);
        participation.setDateInscription(LocalDate.now());

        participationRepository.save(participation);
    }

    public void supprimerChallenge(Long challengeId, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge introuvable"));

        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challengeRepository.delete(challenge);
        } else {
            throw new RuntimeException("Non autoris\u00e9 \u00e0 supprimer ce challenge.");
        }
    }

    public void modifierTitreChallenge(Long challengeId, String nouveauTitre, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge introuvable"));

        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challenge.setTitre(nouveauTitre);
            challengeRepository.save(challenge);
        } else {
            throw new RuntimeException("Non autoris\u00e9 \u00e0 modifier ce challenge.");
        }
    }

    private void verifierEtAttribuerBadgeChallengeGagne(Challenge challenge, List<ClassementDTO> classement) {
        LocalDate today = LocalDate.now();

        if (challenge.getDateFin() != null && today.isAfter(challenge.getDateFin()) && !classement.isEmpty()) {
            ClassementDTO gagnant = classement.get(0);

            for (Participation p : challenge.getParticipations()) {
                String nomComplet = p.getUtilisateur().getPrenom() + " " + p.getUtilisateur().getNom();
                if (badgeAttributionService != null && nomComplet.equals(gagnant.getNomComplet())) {
                    badgeAttributionService.attribuerBadgeChallengeGagne(p.getUtilisateur());
                    break;
                }
            }
        }
    }
}
