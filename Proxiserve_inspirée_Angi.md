

### **1. Vision Globale**
**Objectif** : Créer une plateforme de mise en relation entre clients et artisans/entreprises, permettant :
- Aux **clients** de rechercher, comparer, et réserver des services.
- Aux **artisans/entreprises** de gérer leur visibilité, leurs services, et leurs demandes.
- Une expérience fluide avec un système de notation, de paiement sécurisé, et de communication intégrée.

---

### **2. Architecture Technique**
#### **Stack Technique**
- **Backend** : Spring Boot + MongoDB (avec Spring Data MongoDB).
- **Frontend** : React.js ou Angular (non couvert ici, mais important pour l'UI/UX).
- **Authentification** : Spring Security + JWT.
- **Paiement** : Intégration Stripe/PayPal.
- **Communication** : WebSocket (pour le chat) ou API REST.

#### **Diagramme d'Architecture**
```
Client (Web/Mobile) 
  ↓ HTTPS ↑
API Gateway (Spring Boot Contrôleurs)
  ↓
Services Spring (Logique Métier)
  ↓
MongoDB (Base de Données)
  ↑
External Services (Stripe, SMS, Email)
```

---

### **3. Modèles de Données MongoDB**
#### **User (Utilisateur)**
```java
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private String role; // CLIENT, ARTISAN, COMPANY
    private LocalDateTime createdAt;
    // Champs communs (nom, téléphone, etc.)
}
```

#### **Client**
```java
@Document(collection = "clients")
public class Client {
    @Id
    private String id;
    private String userId; // Référence à User
    private String address;
    private List<Booking> bookings;
}
```

#### **Artisan/Entreprise**
```java
@Document(collection = "artisans")
public class Artisan {
    @Id
    private String id;
    private String userId; // Référence à User
    private String profession; // Plombier, Électricien, etc.
    private String companyName; // Si role=COMPANY
    private List<String> serviceCategories;
    private List<Review> reviews;
    private double rating;
    private Portfolio portfolio; // Photos, descriptions
}
```

#### **Service (Offre de Service)**
```java
@Document(collection = "services")
public class Service {
    @Id
    private String id;
    private String artisanId;
    private String title;
    private String description;
    private double price;
    private List<String> tags; // "Plomberie", "Urgence", etc.
}
```

#### **Booking (Réservation)**
```java
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    private String clientId;
    private String artisanId;
    private String serviceId;
    private LocalDateTime date;
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED
    private PaymentDetails payment;
}
```

---

### **4. Cas d'Utilisation Principaux**
#### **Pour les Clients**
1. **Rechercher un Service** :
   - Filtres : Catégorie, Localisation, Budget, Notes.
   - Tri : Par prix, proximité, ou notation.
2. **Consulter un Profil** :
   - Portfolio, Avis, Tarifs, Disponibilité.
3. **Réserver un Service** :
   - Choix de créneau, Paiement sécurisé.
4. **Noter un Artisan** :
   - Laisse un avis et une note (1-5 étoiles).

#### **Pour les Artisans/Entreprises**
1. **Gérer le Profil** :
   - Ajouter des photos, décrire les compétences, fixer les tarifs.
2. **Publier des Services** :
   - Créer des offres (ex: "Réparation de fuite d'eau - 50€").
3. **Gérer les Réservations** :
   - Accepter/Refuser des demandes, mise à jour du statut.
4. **Communiquer avec les Clients** :
   - Chat intégré pour discuter des détails.

#### **Pour les Entreprises**
1. **Gérer une Équipe** :
   - Ajouter des employés avec des rôles (ex: Administrateur, Technicien).
2. **Analytiques** :
   - Statistiques sur les demandes, revenus, etc.

---

### **5. Spécifications Fonctionnelles**
#### **Inscription/Connexion**
- **Choix du Rôle** : Client, Artisan, ou Entreprise.
- **Vérification d'Email** : Envoi d'un lien de confirmation.
- **Mot de Passe Oublié** : Réinitialisation via email.

#### **Recherche Avancée**
- **API Endpoint** : `GET /api/services/search?category=plomberie&location=Paris&minRating=4`.
- **Algorithmes** : Pertinence basée sur la notation, la proximité, et le prix.

#### **Système de Notation**
- **Calcul de la Note** : Moyenne des avis sur les 12 derniers mois.
- **Anti-Fraude** : Seuls les clients ayant réservé peuvent noter.

#### **Paiement**
- **Intégration Stripe** :
  - Création d'un `PaymentIntent` pour chaque réservation.
  - Remboursement partiel/annulation via API.

#### **Notifications**
- **Email/SMS** :
  - Confirmation de réservation.
  - Rappels de rendez-vous.
  - Nouveaux avis.

---

### **6. Conception de l'UI/UX**
#### **Wireframes (Exemples)**
- **Page d'Accueil** :
  - Barre de recherche en évidence.
  - Catégories populaires (Plomberie, Électricité, etc.).
- **Page de Profil Artisan** :
  - Galerie photo, bouton "Réserver", avis clients.
- **Tableau de Bord Artisan** :
  - Statistiques, demandes en attente, calendrier.

#### **Principes UX**
- **Responsive Design** : Adapté mobile/desktop.
- **Dark Mode** : Optionnel pour le confort visuel.
- **Micro-Interactions** : Animations lors de la réservation.

---

### **7. Sécurité**
- **Authentification** : JWT avec expiration de 24h.
- **RBAC (Role-Based Access Control)** :
  - `ROLE_CLIENT` : Accès aux réservations.
  - `ROLE_ARTISAN` : Gestion de profil/services.
- **Validation des Données** : Hibernate Validator pour les formulaires.

---

### **8. Déploiement et Scalabilité**
- **MongoDB Atlas** : Base de données cloud avec réplication.
- **AWS EC2/Heroku** : Hébergement de l'API Spring Boot.
- **Load Balancer** : Pour gérer le trafic élevé.

---

### **9. Étapes Suivantes**
1. **Prototypage** : Créer des maquettes Figma pour validation.
2. **MVP** : 
   - Inscription/Connexion.
   - Recherche de services.
   - Profils artisans.
3. **Tests Utilisateurs** : Feedback sur l'ergonomie.
4. **Intégration Paiement** : Stripe en mode sandbox.

---

### **10. Inspiration d'Angi**
- **Fonctionnalités Clés à Reprendre** :
  - Système de badges ("Artisan vérifié").
  - Estimation de prix en ligne.
  - Filtres de recherche avancés.
- **Différenciation** :
  - Focus sur les artisans locaux.
  - Profils d'entreprises avec gestion d'équipe.

---
![image](https://github.com/user-attachments/assets/98111db7-e056-4092-959c-610ba2476f4d)
