package utcapitole.miage.projet_web.model.jpa;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import utcapitole.miage.projet_web.model.Activite;

@Service
public class ActiviteService {

    @Autowired
    private ActiviteRepository activiteRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public Activite enregistrerActivite(Activite activite, double latitude, double longitude) {
        
        // 1. Calcul automatique des calories
        // Formule simplifiée (MET * poids * durée)
        int calories = calculerCalories(activite);
        activite.setCaloriesConsommees(calories);

        String meteo = recupererMeteo(latitude, longitude);
        activite.setConditionsMeteo(meteo);

        return activiteRepository.save(activite);
    }

    private int calculerCalories(Activite activite) {
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
}