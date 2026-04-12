package utcapitole.miage.projet_web.model.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

@Service
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final SportRepository sportRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public ActiviteService(ActiviteRepository activiteRepository, SportRepository sportRepository) {
        this.activiteRepository = activiteRepository;
        this.sportRepository = sportRepository;
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

        return activiteRepository.save(activite);
    }

    private double[] getCoordonnees() {
        try {
            // Utilisation d'un API IP pour obtenir les coordonnées automatiquement (via ip-api)
            Map<String, Object> response = restTemplate.getForObject("http://ip-api.com/json/", Map.class);
            double lat = ((Number) response.get("lat")).doubleValue();
            double lon = ((Number) response.get("lon")).doubleValue();
            return new double[]{lat, lon};
        } catch (Exception e) {
            // Coordonnées par défaut (Toulouse) en cas d'échec de l'API
            return new double[]{43.6047, 1.4442};
        }
    }

    private int calculerCalories(Activite activite) {
        Utilisateur user = activite.getUtilisateur();
        utcapitole.miage.projet_web.model.Sport sport = activite.getSport();
        
        if (user == null || activite.getDuree() <= 0) {
            return 0;
        }

        float weight = user.getPoids();
        if (weight <= 0) {
            weight = 70.0f; // Poids par défaut si non renseigné
        }

        double durationMinutes = activite.getDuree();
        double durationHours = durationMinutes / 60.0;
        
        double met = 4.0; // Valeur par défaut

       /* if (sport != null) {
            if (Boolean.TRUE.equals(sport.getEstBaseSurVitesse())) {
                double distance = activite.getDistance();
                double speedKmH = (durationHours > 0) ? (distance / durationHours) : 0;
                met = sport.getIntensiteBase() + sport.getCoeffIntensite() * speedKmH;
            } else {
                // Utilise le niveau d'intensité de l'activité (par défaut 3 si non renseigné)
                int niveau = (activite.getNiveauIntensite() > 0) ? activite.getNiveauIntensite() : 3;
                met = sport.getIntensiteBase() + sport.getCoeffIntensite() * niveau;
            }
        }
        */
        if (sport != null) {

            double intensiteBase = (sport.getIntensiteBase() != null) ? sport.getIntensiteBase() : 0.0;
            double coeff = (sport.getCoeffIntensite() != null) ? sport.getCoeffIntensite() : 0.0;

            if (Boolean.TRUE.equals(sport.getEstBaseSurVitesse())) {
                double distance = activite.getDistance();
                double speedKmH = (durationHours > 0) ? (distance / durationHours) : 0;
                met = intensiteBase + coeff * speedKmH;
            } else {
                int niveau = (activite.getNiveauIntensite() > 0) ? activite.getNiveauIntensite() : 3;
                met = intensiteBase + coeff * niveau;
            }
        }
        else {

            // Logique de repli minimaliste si le Sport n'est pas trouvé
            String type = (activite.getNom() != null) ? activite.getNom() : "Autre";
            if (type.equals("Course")) met = 8.0;
            else if (type.equals("Cyclisme")) met = 6.0;
            else met = 4.0;
        }

        // Formule: Calories = MET * Poids * Heures
        return (int) Math.round(met * weight * durationHours);
    }

    private String recupererMeteo(double lat, double lon) {
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Map<String, Object> currentWeather = (Map<String, Object>) response.get("current_weather");
            
            return "Température: " + currentWeather.get("temperature") + "°C";
        } catch (Exception e) {
            return "Météo indisponible";
        }
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
}