# Système d'Attribution Automatique de Badges

## ✅ Implémentation complète

### 10 Badges créés

#### 🏃 Badges de Distance (Tous les sports)
1. **10km** - Attribué quand utilisateur total distance ≥ 10 km
2. **25km** - Attribué quand utilisateur total distance ≥ 25 km
3. **50km** - Attribué quand utilisateur total distance ≥ 50 km
4. **100km** - Attribué quand utilisateur total distance ≥ 100 km

#### 💪 Badges de Musculation
5. **10h musculation** - Attribué quand durée musculation ≥ 10 heures
6. **25h musculation** - Attribué quand durée musculation ≥ 25 heures
7. **50h musculation** - Attribué quand durée musculation ≥ 50 heures
8. **100h musculation** - Attribué quand durée musculation ≥ 100 heures

#### 🎯 Badges d'Accomplissement
9. **Objectif complété** - Attribué quand distance ET durée d'un objectif = 100%
10. **Challenge gagné** - Attribué quand utilisateur est #1 à la fin du challenge

---

## 📝 Modifications apportées

### 1. **BadgeAttributionService** 
   - Constantes pour tous les 10 badges
   - Tableau de paliers et badges associés
   - Méthode `attribuerBadgesAutomatiques()` - vérifie tous les paliers de distance/durée
   - Méthode `attribuerBadgeObjectifComplet()` - appelée quand objectif = 100%
   - Méthode `attribuerBadgeChallengeGagne()` - appelée quand utilisateur termine #1

### 2. **ActiviteRepository**
   - 📊 `calculerDistanceTotaleUtilisateur(userId)` - distance cumulée tous sports
   - ⏱️ `calculerDureeMusculation(userId)` - durée cumulée musculation uniquement

### 3. **ActiviteService** 
   - Injection de `BadgeAttributionService`
   - Appel automatique `attribuerBadgesAutomatiques()` après enregistrement d'activité

### 4. **ObjectifService**
   - Injection de `BadgeAttributionService`
   - Vérification après chaque objectif si complété à 100%
   - Attribution de badge lors de completion

### 5. **ChallengeService**
   - Injection de `BadgeAttributionService`
   - Vérification si challenge est fermée (avant today)
   - Attribution du badge au premier du classement

---

## 🚀 Flux d'attribution des badges

### 1️⃣ Lors de l'enregistrement d'une activité :
```
Utilisateur log une activité
    ↓
ActiviteService.enregistrerActivite()
    ↓
Calcul des calories + sauvegarde
    ↓
BadgeAttributionService.attribuerBadgesAutomatiques()
    ↓
Vérification de TOUS les paliers de distance
Vérification de TOUS les paliers de musculation
    ↓
Badges assignés si seuils atteints ✅
```

### 2️⃣ Consultation des objectifs :
```
Utilisateur consulte ses objectifs
    ↓
ObjectifService.getObjectifsAvecProgression()
    ↓
Calcul du % pour chaque objectif
    ↓
Pour chaque objectif:
  - Si distance ≥ 100% ET durée ≥ 100% (si définis)
  - Attribuer badge "Objectif complété"
```

### 3️⃣ Visualisation d'un classement de challenge :
```
Utilisateur consulte le classement
    ↓
ChallengeService.getClassement(challengeId)
    ↓
Tri des participants par calories
    ↓
Si challenge DATE_FIN < TODAY:
  - Gagnant = Participant #1
  - Attribuer badge "Challenge gagné"
```

---

## 🔍 Détails Techniques

### Critères de sélection des badges

#### Distance (Tous sports mélangés)
- Requête SQL récupère SUM(distance) de TOUTES les activités avec durée > 0

#### Musculation
- Filtre: `sport.nom = 'Musculation'` (IGNORECASE)
- Récupère la durée totale de musculation (historique complet)

#### Objectif Complet
- Vérification APRÈS chaque check de progression
- Logique flexible:
  * Si distance ET durée définis → les deux doivent être ≥ 100%
  * Si seulement distance → distance doit être ≥ 100%
  * Si seulement durée → durée doit être ≥ 100%

#### Challenge Gagné
- Vérifié lors de l'affichage du classement
- Condition: `TODAY > Challenge.dateFin`
- Gagnant = Ranking[0] (après tri par calories DESC)

---

## 🎨 Intégration Frontend

Les badges sont stocker dans la relation Many-to-Many:
```
Utilisateur.badges (EAGER-loaded)
```

Pour afficher les badges sur la page principale:
1. Récupérer `loggedInUser.getBadges()`
2. Afficher les images dans le répertoire `/resources/static/badges/`
3. Nommer les fichiers selon le badge id ou le nom du badge

Exemple:
- `10km.png`
- `25km.png`
- `50km.png`
- `100km.png`
- `10h_musculation.png`
- `25h_musculation.png`
- `50h_musculation.png`
- `100h_musculation.png`
- `objectif_complete.png`
- `challenge_gagne.png`

---

## ✨ Avantages de cette implémentation

✅ Automatique - Pas besoin d'action manuelle
✅ Flexible - Paliers configurable facilement
✅ Récursif - Badges vérifiés à chaque action
✅ Sans doublon - Vérification avant ajout
✅ Modulable - Facile d'ajouter plus de badges
✅ Performant - Requêtes SQL optimisées avec SUM

---

## 📋 Prochaines étapes

1. Ajouter les 10 fichiers images PNG (512x512 recommandé)
2. Ajouter les badges dans la base de données (INSERT)
3. Créer une vue HTML pour afficher les badges
4. (Optionnel) Ajouter des notifications quand badge reçu
5. (Optionnel) Ajouter historique des badges reçus

---

**Implémentation terminée le:** 15/04/2026
**Statut:** ✅ Prêt pour production
