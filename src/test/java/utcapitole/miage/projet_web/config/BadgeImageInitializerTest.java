package utcapitole.miage.projet_web.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utcapitole.miage.projet_web.model.Badge;
import utcapitole.miage.projet_web.model.jpa.BadgeRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeImageInitializerTest {

    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private BadgeImageInitializer initializer;

    @Test
    void testRun_InitialiseNouveauxBadgesEtMetAJourAnciens() throws Exception {
        // 1. Simuler que la base de données est VIDE (les 10 badges n'existent pas)
        when(badgeRepository.findByEntitule(anyString())).thenReturn(Optional.empty());

        // 2. Simuler qu'il y a 2 anciens badges dans la BDD qui n'ont pas d'imageName ou une image vide
        Badge ancienBadge1 = new Badge();
        ancienBadge1.setEntitule("10km");
        ancienBadge1.setImageName(null);

        Badge ancienBadge2 = new Badge();
        ancienBadge2.setEntitule("25km");
        ancienBadge2.setImageName("");

        when(badgeRepository.findAll()).thenReturn(Arrays.asList(ancienBadge1, ancienBadge2));

        // Exécuter l'initializer
        initializer.run();

        // 3. Vérifications :
        // - save() doit être appelé 10 fois pour créer les 10 nouveaux badges.
        // - save() doit être appelé 2 fois supplémentaires pour mettre à jour les 2 anciens badges.
        // Total = 12 fois
        verify(badgeRepository, times(12)).save(any(Badge.class));
    }

    @Test
    void testRun_BadgesDejaExistants_NeFaitRien() throws Exception {
        // 1. Simuler que tous les badges existent DÉJÀ dans la base de données
        when(badgeRepository.findByEntitule(anyString())).thenReturn(Optional.of(new Badge()));

        // 2. Simuler que les badges en base ont DÉJÀ leur imageName correctement rempli
        Badge badgeParfait = new Badge();
        badgeParfait.setEntitule("10km");
        badgeParfait.setImageName("10KM");

        when(badgeRepository.findAll()).thenReturn(Arrays.asList(badgeParfait));

        // Exécuter l'initializer
        initializer.run();

        // 3. Vérification : Aucun nouveau badge ne doit être créé, et aucun ancien ne doit être mis à jour.
        verify(badgeRepository, never()).save(any(Badge.class));
    }
}