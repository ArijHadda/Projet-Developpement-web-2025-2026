# Documentation du Système de Style UI SportTrack

## Palette de Couleurs

### Couleurs Principales (Style Peinture Impressionniste)
- **Orange Corail** `#FF7F50` - Boutons CTA, éléments d'accentuation
- **Vert Printemps** `#90EE90` - État de succès, lié au sport
- **Bleu Ciel** `#87CEFA` - Info-bulles, fonds clairs
- **Or** `#FFD700` - Badges, points forts

### Gradients de Fond
- `page-login` - Gradient Coucher de Soleil (Orange chaud)
- `page-register` - Gradient de Rêve (Rose-Violet)
- `page-activities` - Gradient Océan (Bleu-Vert)
- `page-challenges` - Gradient Nature (Tons verts)
- `page-profile` - Gradient Mixte (Bleu-Vert-Jaune)

## Référence des Classes CSS

### Mise en Page
- `.container` - Conteneur principal (Largeur max 1200px)
- `.grid` / `.grid-2` / `.grid-3` / `.grid-4` - Mise en page en grille
- `.flex` / `.flex-col` - Mise en page Flex
- `.justify-between` / `.items-center` - Outils d'alignement

### Composants
- `.card` - Carte en verre (Semi-transparente + Flou)
- `.btn` / `.btn-primary` / `.btn-secondary` / `.btn-info` / `.btn-warning` / `.btn-danger` / `.btn-outline` - Boutons
- `.form-control` - Champs de saisie
- `.form-group` - Groupe de formulaire
- `.table` / `.table-container` - Tableaux
- `.alert` / `.alert-success` / `.alert-danger` / `.alert-warning` / `.alert-info` - Alertes
- `.progress` / `.progress-bar` - Barres de progression
- `.badge` / `.badge-success` / `.badge-info` - Badges

### Classes Utilitaires
- `.text-center` / `.text-right` - Alignement du texte
- `.mb-0` / `.mb-sm` / `.mb-md` / `.mb-lg` / `.mb-xl` - Marges inférieures
- `.mt-sm` / `.mt-md` / `.mt-lg` - Marges supérieures
- `.p-sm` / `.p-md` / `.p-lg` / `.p-xl` - Remplissage (Padding)
- `.fade-in` - Animation d'apparition en fondu

## Classes de Fond de Page
Utilisez les classes suivantes sur la balise `<body>` :
- `page-login` - Page de connexion
- `page-register` - Page d'inscription
- `page-activities` - Pages liées aux activités
- `page-challenges` - Pages liées aux défis
- `page-profile` - Pages de profil utilisateur

## Design Réactif (Responsive)
- Bureau : Mise en page multi-colonnes
- Tablette (<768px) : Mise en page sur une seule colonne, navigation masquée
- Mobile (<480px) : Boutons pleine largeur, espacement compact

## Exemple d'Utilisation
```html
<div class="card fade-in">
    <h2 class="text-center">Titre</h2>
    <div class="form-group">
        <label class="form-label">Libellé</label>
        <input type="text" class="form-control" placeholder="Entrez du texte...">
    </div>
    <button class="btn btn-primary">Soumettre</button>
</div>
```
