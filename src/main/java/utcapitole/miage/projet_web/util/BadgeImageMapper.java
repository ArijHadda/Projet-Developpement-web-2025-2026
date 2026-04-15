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
        }
        if (normalized.equals("25km")) {
            return "25KM";
        }
        if (normalized.equals("50km")) {
            return "50KM";
        }
        if (normalized.equals("100km")) {
            return "100KM";
        }

        // Badges de musculation
        if (normalized.contains("10h")) {
            return "10H";
        }
        if (normalized.contains("25h")) {
            return "25H";
        }
        if (normalized.contains("50h")) {
            return "50H";
        }
        if (normalized.contains("100h")) {
            return "100H";
        }

        // Badges d'accomplissement
        if (normalized.contains("objectif")) {
            return "OBJECTIF";
        }
        if (normalized.contains("challenge")) {
            return "CHALLENGE";
        }

        // Par défaut, retourner l'entitulé en majuscules
        return entitule.toUpperCase();
    }
}