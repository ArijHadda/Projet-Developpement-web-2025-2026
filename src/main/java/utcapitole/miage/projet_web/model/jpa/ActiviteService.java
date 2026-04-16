package utcapitole.miage.projet_web.model.jpa;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Commentaire;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.Sport;

/**
 * Service métier central gérant le cycle de vie des activités sportives.
 * Responsable de la création, du calcul métabolique (calories), de l'intégration
 * des données météorologiques et des intéractions sociales (Kudos, Commentaires).
 */
@Service
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final SportRepository sportRepository;
    private final CommentaireRepository commentaireRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final BadgeAttributionService badgeAttributionService;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Constructeur alternatif permettant d'instancier ActiviteService sans le système de badges.
     * * @param activiteRepository le repository de l'entité Activite
     * @param sportRepository le repository de l'entité Sport
     * @param commentaireRepository le repository de l'entité Commentaire
     * @param utilisateurRepository le repository de l'entité Utilisateur
     */
    public ActiviteService(ActiviteRepository activiteRepository, SportRepository sportRepository, CommentaireRepository commentaireRepository, UtilisateurRepository utilisateurRepository) {
        this(activiteRepository, sportRepository, commentaireRepository, utilisateurRepository, null);
    }

    @Autowired
    public ActiviteService(ActiviteRepository activiteRepository, SportRepository sportRepository, CommentaireRepository commentaireRepository, UtilisateurRepository utilisateurRepository, BadgeAttributionService badgeAttributionService) {
        this.activiteRepository = activiteRepository;
        this.sportRepository = sportRepository;
        this.commentaireRepository = commentaireRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.badgeAttributionService = badgeAttributionService;
    }

    /**
     * Enregistre une nouvelle activité pour un utilisateur.
     * Ce processus inclut automatiquement le calcul des calories brûlées via le coefficient MET du sport,
     * la récupération des conditions météorologiques du lieu, et déclenche la vérification des badges.
     *
     * @param activite L'objet Activite contenant les détails saisis par l'utilisateur.
     * @return L'activité persistée dans la base de données, avec ses attributs enrichis.
     */
    public Activite enregistrerActivite(Activite activite) {
        // 1. Associer le Sport s'il n'est pas déjà présent
        if (activite.getSport() == null && activite.getNom() != null) {
            activite.setSport(sportRepository.findByNom(activite.getNom()));
        }

        // 2. Calcul automatique des calories
        int calories = calculerCalories(activite);
        activite.setCaloriesConsommees(calories);

        // 3. Récupérer automatiquement les coordonnées puis la météo
        double[] coords = getCoordonnees();
        String meteo = recupererMeteo(coords[0], coords[1]);
        activite.setConditionsMeteo(meteo);

        Activite savedActivite = activiteRepository.save(activite);

        // 4. Déclencher le processus d'attribution des badges
        if (badgeAttributionService != null && activite.getUtilisateur() != null) {
            badgeAttributionService.attribuerBadgesAutomatiques(activite.getUtilisateur().getId());
        }
        return savedActivite;
    }

    private double[] getCoordonnees() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject("http://ip-api.com/json/", Map.class);
            if (response == null || response.get("lat") == null || response.get("lon") == null) {
                return new double[]{43.6047, 1.4442};
            }
            double lat = ((Number) response.get("lat")).doubleValue();
            double lon = ((Number) response.get("lon")).doubleValue();
            return new double[]{lat, lon};
        } catch (Exception e) {
            return new double[]{43.6047, 1.4442};
        }
    }

    private String recupererMeteo(double lat, double lon) {
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || response.get("current_weather") == null) {
                return "Météo indisponible";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> currentWeather = (Map<String, Object>) response.get("current_weather");
            return "Température: " + currentWeather.get("temperature") + "°C";
        } catch (Exception e) {
            return "Météo indisponible";
        }
    }

    private int calculerCalories(Activite activite) {
        Utilisateur user = activite.getUtilisateur();
        if (user == null || activite.getDuree() <= 0) {
            return 0;
        }

        float weight = user.getPoids() > 0 ? user.getPoids() : 70.0f;
        double durationHours = activite.getDuree() / 60.0;

        double met = determinerMet(activite, durationHours);

        return (int) Math.round(met * weight * durationHours);
    }

    private double determinerMet(Activite activite, double durationHours) {
        Sport sport = activite.getSport();
        if (sport != null) {
            return calculerMetAvecSport(sport, activite, durationHours);
        }
        return calculerMetParDefaut(activite.getNom());
    }

    private double calculerMetAvecSport(Sport sport, Activite activite, double durationHours) {
        double intensiteBase = (sport.getIntensiteBase() != null) ? sport.getIntensiteBase() : 0.0;
        double coeff = (sport.getCoeffIntensite() != null) ? sport.getCoeffIntensite() : 0.0;

        if (Boolean.TRUE.equals(sport.getEstBaseSurVitesse())) {
            double speedKmH = (durationHours > 0) ? (activite.getDistance() / durationHours) : 0;
            return intensiteBase + coeff * speedKmH;
        }

        int niveau = (activite.getNiveauIntensite() > 0) ? activite.getNiveauIntensite() : 3;
        return intensiteBase + coeff * niveau;
    }

    private double calculerMetParDefaut(String nomSport) {
        String type = (nomSport != null) ? nomSport : "Autre";
        if (type.equals("Course")) return 8.0;
        if (type.equals("Cyclisme")) return 6.0;
        return 4.0;
    }

    /**
     * Récupère l'intégralité des activités enregistrées dans le système.
     * @return La liste complète des objets Activite.
     */
    public List<Activite> getToutesLesActivites() {
        return activiteRepository.findAll();
    }

    /**
     * Récupère l'historique chronologique des activités d'un utilisateur spécifique.
     *
     * @param id L'identifiant de l'utilisateur.
     * @return La liste de ses activités triée de la plus récente à la plus ancienne.
     */
    public List<Activite> getProgresUtilisateur(Long id) {
        return activiteRepository.findByUtilisateurIdOrderByDateDesc(id);
    }

    /**
     * Surcharge de la méthode permettant de récupérer l'historique en passant directement l'objet Utilisateur.
     *
     * @param user L'objet Utilisateur concerné.
     * @return La liste de ses activités.
     */
    public List<Activite> getActivitesByUtilisateur(Utilisateur user) {
        return activiteRepository.findByUtilisateurIdOrderByDateDesc(user.getId());
    }

    /**
     * Agrége les données d'une liste d'activités pour générer un résumé global.
     * Utile pour alimenter les tableaux de bord et les widgets statistiques (ex: Total des calories).
     *
     * @param activites La collection d'activités à analyser.
     * @return Un Map (Dictionnaire) contenant les indicateurs clés: "count", "totalDuree", "totalDistance", "totalCalories".
     */
    public Map<String, Object> getStatsActivites(List<Activite> activites) {
        Map<String, Object> stats = new HashMap<>();
        int totalActivites = activites.size();
        double totalDuree = 0;
        double totalDistance = 0;
        int totalCalories = 0;

        for (Activite a : activites) {
            totalDuree += a.getDuree();
            totalDistance += a.getDistance();
            totalCalories += a.getCaloriesConsommees();
        }

        stats.put("count", totalActivites);
        stats.put("totalDuree", totalDuree);
        stats.put("totalDistance", totalDistance);
        stats.put("totalCalories", totalCalories);

        return stats;
    }

    /**
     * Supprime définitivement une activité de la base de données.
     *
     * @param idActivite L'identifiant de l'activité à supprimer.
     */
    public void supprimer(Long idActivite) {
        activiteRepository.deleteById(idActivite);
    }

    /**
     * Récupère les détails d'une activité spécifique grâce à son identifiant.
     *
     * @param idActivite L'identifiant de l'activité.
     * @return Un Optional contenant l'activité si elle existe.
     */
    public Optional<Activite> getById(Long idActivite) {
        return activiteRepository.findById(idActivite);
    }

    /**
     * Génère le flux d'actualité (Feed) d'un utilisateur en compilant les activités de ses amis.
     *
     * @param utilisateur L'utilisateur qui consulte le flux.
     * @return Une liste chronologique des activités réalisées par son réseau d'amis.
     */
    public List<Activite> getFluxActivitesAmis(Utilisateur utilisateur) {
        List<Utilisateur> amis = utilisateur.getAmis();

        if (amis == null || amis.isEmpty()) {
            return List.of();
        }

        return activiteRepository.findByUtilisateurInOrderByDateDesc(amis);
    }

    /**
     * Gère l'action d'applaudir (Kudo) sur une activité.
     * Si l'utilisateur a déjà donné un kudo, la méthode le retire (effet toggle).
     *
     * @param activiteId L'identifiant de l'activité ciblée.
     * @param userId L'identifiant de l'utilisateur effectuant l'action.
     */
    @Transactional
    public void toggleKudos(Long activiteId, Long userId) {
        Activite a = activiteRepository.findById(activiteId).orElseThrow();
        Utilisateur u = utilisateurRepository.findById(userId).orElseThrow();

        if (a.getLikers().contains(u)) {
            a.getLikers().remove(u);
        } else {
            a.getLikers().add(u);
        }

        activiteRepository.save(a);
    }

    /**
     * Permet à un utilisateur de publier un commentaire texte sur une activité spécifique.
     *
     * @param activiteId L'identifiant de l'activité commentée.
     * @param userId L'identifiant de l'auteur du commentaire.
     * @param contenu Le texte du commentaire.
     */
    @Transactional
    public void ajouterCommentaire(Long activiteId, Long userId, String contenu) {
        Activite a = activiteRepository.findById(activiteId).orElseThrow();
        Utilisateur u = utilisateurRepository.findById(userId).orElseThrow();

        Commentaire com = new Commentaire();
        com.setContenu(contenu);
        com.setActivite(a);
        com.setAuteur(u);
        com.setDateCreation(LocalDateTime.now());

        commentaireRepository.save(com);
    }
}