package utcapitole.miage.projet_web.util;

/**
 * Utilitaire pour mapper les entitulés de badges aux noms de fichiers images
 */
public class BadgeImageMapper {

    private BadgeImageMapper() {
        // Utility class
    }

    /**
     * Convertit un entitulé de badge en nom de fichier image
     * @param entitule L'entitulé du badge (ex: "10km", "10h musculation", "Objectif complété")
     * @return Le nom du fichier image correspondant (ex: "10KM", "10H", "OBJECTIF")
     */
    public static String mapBadgeToImageName(String entitule) {
        if (entitule == null || entitule.trim().isEmpty()) {
            return "BADGE";
        }

        String normalized = entitule.toLowerCase().trim();

        // Badges de distance
        if (normalized.equals("10km")) {
            return "10KM";
        } else if (normalized.equals("25km")) {
            return "25KM";
        } else if (normalized.equals("50km")) {
            return "50KM";
        } else if (normalized.equals("100km")) {
            return "100KM";
        }
        // Badges de musculation
        else if (normalized.contains("10h") || normalized.contains("10h musculation")) {
            return "10H";
        } else if (normalized.contains("25h") || normalized.contains("25h musculation")) {
            return "25H";
        } else if (normalized.contains("50h") || normalized.contains("50h musculation")) {
            return "50H";
        } else if (normalized.contains("100h") || normalized.contains("100h musculation")) {
            return "100H";
        }
        // Badges d'accomplissement
        else if (normalized.contains("objectif") || normalized.contains("objectif complété")) {
            return "OBJECTIF";
        } else if (normalized.contains("challenge") || normalized.contains("challenge gagné")) {
            return "CHALLENGE";
        }

        // Par défaut, retourner l'entitulé en majuscules
        return entitule.toUpperCase();
    }
}
