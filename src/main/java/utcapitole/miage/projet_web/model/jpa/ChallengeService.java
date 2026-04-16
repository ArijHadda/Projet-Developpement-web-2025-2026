package utcapitole.miage.projet_web.model.jpa;

import org.springframework.stereotype.Service;
import utcapitole.miage.projet_web.dto.ClassementDTO;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Participation;
import utcapitole.miage.projet_web.model.Utilisateur;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service métier gérant le cycle de vie des challenges (défis communautaires).
 * Inclut la création, l'inscription, la génération des classements dynamiques
 * et l'attribution des récompenses aux vainqueurs.
 */
@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ActiviteRepository activiteRepository;
    private final ParticipationRepository participationRepository;
    private final BadgeAttributionService badgeAttributionService;

    private static final String MESSAGE_DE_INTROUVABLE = "Challenge introuvable";
    private static final String MESSAGE_NON_AUTORISATION_MODIFIIER = "Non autorisé à modifier ce challenge.";
    private static final String MESSAGE_NON_AUTORISATION_SUPPRIMER = "Non autorisé à supprimer ce challenge.";
    private static final String MESSAGE_REPETITION = "Vous participez déjà à ce challenge !";

    public ChallengeService(ChallengeRepository challengeRepository, ActiviteRepository activiteRepository, ParticipationRepository participationRepository, BadgeAttributionService badgeAttributionService) {
        this.challengeRepository = challengeRepository;
        this.activiteRepository = activiteRepository;
        this.participationRepository = participationRepository;
        this.badgeAttributionService = badgeAttributionService;
    }

    /**
     * Calcule et génère le classement actuel d'un challenge basé sur les calories brûlées.
     * Si le challenge est terminé, déclenche l'attribution du badge "Première Victoire" au gagnant.
     *
     * @param challengeId L'identifiant du challenge.
     * @return Une liste de ClassementDTO triée par ordre décroissant des scores.
     */
    public List<ClassementDTO> getClassement(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException(MESSAGE_DE_INTROUVABLE));

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

        verifierEtAttribuerBadgeChallengeGagne(challenge, classement);

        return classement;
    }

    /**
     * Récupère l'intégralité des challenges du système.
     *
     * @return La liste des challenges.
     */
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    /**
     * Crée un nouveau challenge après avoir validé la cohérence des dates d'ouverture et de fermeture.
     *
     * @param challenge Le challenge à créer.
     * @param createur L'utilisateur créant le challenge.
     * @return Le challenge persisté.
     */
    public Challenge creerChallenge(Challenge challenge, Utilisateur createur) {
        LocalDate today = LocalDate.now();

        if (challenge.getDateFin() != null && challenge.getDateFin().isBefore(today)) {
            throw new IllegalArgumentException("La date de fin du challenge ne peut pas être dans le passé.");
        }
        if (challenge.getDateDebut() != null && challenge.getDateFin() != null && challenge.getDateDebut().isAfter(challenge.getDateFin())) {
            throw new IllegalArgumentException("La date de début ne peut pas être après la date de fin.");
        }

        challenge.setCreateur(createur);
        return challengeRepository.save(challenge);
    }

    /**
     * Inscrit un utilisateur à un challenge, sous réserve que celui-ci ne soit pas déjà terminé.
     *
     * @param challengeId L'identifiant du challenge.
     * @param utilisateur L'utilisateur souhaitant participer.
     */
    public void rejoindreChallenge(Long challengeId, Utilisateur utilisateur) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException(MESSAGE_DE_INTROUVABLE));

        if (challenge.getDateFin() != null && LocalDate.now().isAfter(challenge.getDateFin())) {
            throw new IllegalStateException("Ce challenge est déjà terminé. Vous ne pouvez plus le rejoindre.");
        }

        if (participationRepository.existsByUtilisateurAndChallenge(utilisateur, challenge)) {
            throw new IllegalStateException(MESSAGE_REPETITION);
        }

        Participation participation = new Participation();
        participation.setChallenge(challenge);
        participation.setUtilisateur(utilisateur);
        participation.setDateInscription(LocalDate.now());

        participationRepository.save(participation);
    }

    /**
     * Supprime un challenge. Seul le créateur du challenge est autorisé à effectuer cette action.
     *
     * @param challengeId L'identifiant du challenge.
     * @param utilisateurActuel L'utilisateur effectuant la requête.
     */
    public void supprimerChallenge(Long challengeId, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException(MESSAGE_DE_INTROUVABLE));

        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challengeRepository.delete(challenge);
        } else {
            throw new IllegalStateException(MESSAGE_NON_AUTORISATION_SUPPRIMER);
        }
    }

    /**
     * Modifie le titre d'un challenge existant (réservé au créateur).
     *
     * @param challengeId L'identifiant du challenge.
     * @param nouveauTitre Le nouveau titre à appliquer.
     * @param utilisateurActuel L'utilisateur effectuant la requête.
     */
    public void modifierTitreChallenge(Long challengeId, String nouveauTitre, Utilisateur utilisateurActuel) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException(MESSAGE_DE_INTROUVABLE));

        if (challenge.getCreateur().getId().equals(utilisateurActuel.getId())) {
            challenge.setTitre(nouveauTitre);
            challengeRepository.save(challenge);
        } else {
            throw new IllegalStateException(MESSAGE_NON_AUTORISATION_MODIFIIER);
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