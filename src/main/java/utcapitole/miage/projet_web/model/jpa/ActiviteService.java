package utcapitole.miage.projet_web.model.jpa;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import utcapitole.miage.projet_web.model.Activite;
import utcapitole.miage.projet_web.model.Utilisateur;

@Service
public class ActiviteService {

    private final ActiviteRepository activiteRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public ActiviteService(ActiviteRepository activiteRepository) {
        this.activiteRepository = activiteRepository;
    }

    public Activite enregistrerActivite(Activite activite) {
        
        // 1. Calcul automatique des calories
        // Formule simplifiée (MET * poids * durée)
        int calories = calculerCalories(activite);
        activite.setCaloriesConsommees(calories);

        // 2. Récupérer automatiquement les coordonnées puis la météo
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

    private int calculerCalories(Activite activite) { // pour le moment on utilise une formule simplifiée
        return activite.getDuree() * 10; 
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
        return activiteRepository.findByUtilisateur(user);
    }
}