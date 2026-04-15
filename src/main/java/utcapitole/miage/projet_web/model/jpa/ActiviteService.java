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

@Service
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final SportRepository sportRepository;
    private final CommentaireRepository commentaireRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final BadgeAttributionService badgeAttributionService;
    private final RestTemplate restTemplate = new RestTemplate();

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

    //Refactorisation de calculerCalories pour réduire la complexité cognitive (SonarQube java:S3776)
    private int calculerCalories(Activite activite) {
        Utilisateur user = activite.getUtilisateur();
        if (user == null || activite.getDuree() <= 0) {
            return 0;
        }

        float weight = user.getPoids() > 0 ? user.getPoids() : 70.0f;
        double durationHours = activite.getDuree() / 60.0;

        // Délégation de la logique de calcul du MET à une autre méthode
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

    public List<Activite> getToutesLesActivites() {
        return activiteRepository.findAll();
    }

    public List<Activite> getProgresUtilisateur(Long id) {
        return activiteRepository.findByUtilisateurIdOrderByDateDesc(id);
    }

    public List<Activite> getActivitesByUtilisateur(Utilisateur user) {
        return activiteRepository.findByUtilisateurIdOrderByDateDesc(user.getId());
    }

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

    public void supprimer(Long idActivite) {
        activiteRepository.deleteById(idActivite);
    }

    public Optional<Activite> getById(Long idActivite) {
        return activiteRepository.findById(idActivite);
    }

    public List<Activite> getFluxActivitesAmis(Utilisateur utilisateur) {
        List<Utilisateur> amis = utilisateur.getAmis();

        if (amis == null || amis.isEmpty()) {
            return List.of();
        }

        return activiteRepository.findByUtilisateurInOrderByDateDesc(amis);
    }

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