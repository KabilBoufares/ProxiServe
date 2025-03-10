**Plan de Développement et Premières Étapes pour la Plateforme Artisans-Clients**

---

### **Phase 1 : Configuration Initiale (Semaines 1-2)**
#### **1.1. Setup du Projet Spring Boot**
- **Outils** : Spring Initializr (Web, Data MongoDB, Security, Validation, Lombok).
- **Structure** : 
  - Packages : `config`, `controller`, `service`, `repository`, `model`, `security`, `dto`, `exception`.
- **Gestion de dépendances** : Maven/Gradle (ex: `spring-boot-starter-data-mongodb`, `spring-boot-starter-security`).

#### **1.2. Base de Données MongoDB**
- **Configuration** : 
  - Local avec Docker (`docker-compose.yml`) ou MongoDB Atlas.
  - Création des collections : `users`, `clients`, `artisans`, `services`, `bookings`.
- **Indexes** : 
  - Index géospatial pour `Artisan.location` (recherche par proximité).
  - Index sur `User.email` (unicité).

#### **1.3. Authentification (Spring Security + JWT)**
- **Endpoints** : 
  - `POST /api/auth/signup` (avec vérification email).
  - `POST /api/auth/login` (renvoie un JWT).
  - `POST /api/auth/reset-password`.
- **Rôles** : `ROLE_CLIENT`, `ROLE_ARTISAN`, `ROLE_COMPANY`.
- **Sécurité** :
  - HTTPS via Let's Encrypt (en production).
  - Hachage des mots de passe avec BCrypt.

---

### **Phase 2 : Modèles de Données et API de Base (Semaines 3-4)**
#### **2.1. Implémentation des Modèles MongoDB**
- **Classes** :
  - `User`, `Client`, `Artisan`, `Service`, `Booking` (annotées avec `@Document`).
  - Relations via `@DBRef` ou références manuelles (ex: `userId` dans `Client`).
- **Validation** : 
  - `@Email`, `@NotBlank`, `@Size` pour les DTOs.

#### **2.2. API CRUD (Endpoints Initiaux)**
- **Artisans** :
  - `GET /api/artisans` (liste filtrée).
  - `POST /api/artisans` (réservé aux artisans).
- **Services** :
  - `POST /api/services` (création par artisan).
  - `GET /api/services/search` (filtres par catégorie, prix, localisation).

---

### **Phase 3 : Fonctionnalités Clés MVP (Semaines 5-7)**
#### **3.1. Recherche Avancée**
- **API** : `GET /api/services/search?category=plomberie&location=Paris&maxPrice=100`.
- **Implémentation** :
  - Filtres MongoDB avec `Criteria` dans `MongoTemplate`.
  - Tri par note, prix, ou proximité (géospatial).

#### **3.2. Réservation et Paiement**
- **Workflow** :
  1. Client sélectionne un service et un créneau.
  2. Création d’un `PaymentIntent` Stripe.
  3. Webhook Stripe pour confirmer le paiement.
  4. Notification à l’artisan (email/SMS).
- **Endpoints** :
  - `POST /api/bookings` (avec `StripeToken`).
  - `GET /api/bookings/{id}/status`.

#### **3.3. Système de Notation**
- **Règles** :
  - Seuls les clients ayant une réservation `COMPLETED` peuvent noter.
  - Calcul de la moyenne sur 12 mois.
- **API** : 
  - `POST /api/reviews` (avec vérification de l’historique).

---

### **Phase 4 : Intégrations et Sécurité (Semaines 8-9)**
#### **4.1. Notifications (Email/SMS)**
- **Outils** : SendGrid (email), Twilio (SMS).
- **Asynchrone** : Utilisation de `@Async` et `ThreadPoolTaskExecutor`.

#### **4.2. RBAC (Role-Based Access Control)**
- **Exemples** :
  - `ROLE_CLIENT` : Accès en lecture aux services, écriture aux réservations .
  - `ROLE_ARTISAN` : Gestion de ses services et réservations .
- **Configuration** : `@PreAuthorize` dans les contrôleurs .

---

### **Phase 5 : Tests et Déploiement (Semaines 10-12)**
#### **5.1. Tests**
- **Unitaires** : JUnit/Mockito pour les services.
- **Intégration** : TestContainers pour MongoDB.
- **Postman** : Collection pour les cas d’utilisation critiques.

#### **5.2. Déploiement**
- **Backend** : 
  - Hébergement : Heroku (gratuit pour MVP) ou AWS EC2.
  - Variables d’environnement : `STRIPE_API_KEY`, `MONGODB_URI`.
- **Base de données** : MongoDB Atlas (cluster M0 gratuit).

---

### **Étapes Suivantes (Post-MVP)**
1. **Chat en Temps Réel** : Intégration WebSocket (ex: STOMP avec SockJS).
2. **Gestion d’Équipe** : Ajout de rôles `EMPLOYEE` pour les entreprises.(peut étre annuler ) 
3. **Analytiques** : Dashboard avec des graphiques (ex: Spring Boot + React).
4. **Cache Redis** : Optimisation des performances de recherche.

---

### **Risques et Atténuation**
- **Paiement Échoué** : Logs détaillés et relances automatiques.
- **Concurrence** : Verrouillage optimiste pour les mises à jour de réservations.
- **Scalabilité** : Ajout de Load Balancers (NGINX) et réplication MongoDB.

---

**Livrables Initiaux** :
- Code source sur GitHub/GitLab.
- Documentation Swagger (http://localhost:8080/swagger-ui.html).
- Scripts de déploiement (Dockerfile, docker-compose.yml).
