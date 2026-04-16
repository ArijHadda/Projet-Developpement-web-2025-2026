package utcapitole.miage.projet_web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entité représentant un commentaire posté par un utilisateur sur une activité.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Commentaire")
public class Commentaire {
    /** Identifiant unique du commentaire */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Contenu textuel du commentaire */
    private String contenu;
    /** Date et heure de création du commentaire */
    private LocalDateTime dateCreation;

    /** Utilisateur ayant écrit le commentaire */
    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur auteur;

    /** Activité sur laquelle le commentaire a été posté */
    @ManyToOne
    @JoinColumn(name = "id_activite")
    private Activite activite;
}