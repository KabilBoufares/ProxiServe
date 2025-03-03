

### **1. Diagramme de Cas d’Utilisation (Use Case)**
**Acteurs** :  
- **Client** (Recherche, Réserve, Note).  
- **Artisan** (Gère Profil, Publie Services, Gère Réservations).  
- **Entreprise** (Gère Équipe, Consulte Statistiques).  
- **Système** (Envoie Notifications, Gère Paiements).  

**Cas d’utilisation** :  
- **Client** :  
  - `Rechercher un service` → Filtres (métier, localisation, budget).  
  - `Réserver un service` → Sélectionne créneau → Paiement.  
  - `Noter un artisan` → Laisse un avis + note (1-5 étoiles).  

- **Artisan/Entreprise** :  
  - `Créer un profil` → Décrit compétences, tarifs, portfolio.  
  - `Accepter/Refuser une réservation` → Met à jour le statut.  
  - `Consulter les demandes` → Filtre par date/statut.  

- **Système** :  
  - `Envoyer une notification` → Email/SMS (confirmation, rappel).  
  - `Calculer la note moyenne` → Basée sur les avis des 12 derniers mois.  

---

### **2. Diagramme de Classes (Modèles Principaux)**  
#### **User**  
- **Attributs** :  
  ```plaintext
  - id: String  
  - email: String  
  - password: String  
  - role: Enum (CLIENT, ARTISAN, COMPANY)  
  - createdAt: LocalDateTime  
  ```  
- **Relations** :  
  - `Client` → 1:1 (Un User est un Client).  
  - `Artisan` → 1:1 (Un User est un Artisan).  

#### **Client**  
- **Attributs** :  
  ```plaintext
  - id: String  
  - address: String  
  - bookings: List<Booking>  
  ```  

#### **Artisan**  
- **Attributs** :  
  ```plaintext
  - id: String  
  - profession: String  
  - companyName: String (optionnel)  
  - serviceCategories: List<String>  
  - rating: double  
  ```  
- **Relations** :  
  - `Service` → 1:N (Un Artisan propose plusieurs Services).  
  - `Review` → 1:N (Un Artisan a plusieurs Avis).  

#### **Service**  
- **Attributs** :  
  ```plaintext
  - id: String  
  - title: String  
  - description: String  
  - price: double  
  - tags: List<String>  
  ```  

#### **Booking**  
- **Attributs** :  
  ```plaintext
  - id: String  
  - date: LocalDateTime  
  - status: String (PENDING, CONFIRMED, COMPLETED)  
  ```  
- **Relations** :  
  - `Client` → N:1 (Plusieurs Réservations liées à un Client).  
  - `Artisan` → N:1 (Plusieurs Réservations liées à un Artisan).  

---

### **3. Diagramme de Séquence (Exemple : Réservation)**  
1. **Client** → **Application** : `Recherche un service`.  
2. **Application** → **Base de Données** : Renvoie une liste d’artisans.  
3. **Client** → **Application** : `Sélectionne un artisan et un créneau`.  
4. **Application** → **Stripe** : Crée un `PaymentIntent`.  
5. **Client** → **Stripe** : Valide le paiement.  
6. **Application** → **Artisan** : Envoie une notification ("Nouvelle réservation").  
7. **Artisan** → **Application** : Confirme la réservation.  

---

### **4. Diagramme de Composants**  
- **Frontend** (React/Angular) → **API REST** (Spring Boot).  
- **API REST** → **Services** :  
  - `Service d’Authentification` (JWT).  
  - `Service de Recherche` (Filtres géo, prix, etc.).  
  - `Service de Paiement` (Stripe).  
- **Services** → **MongoDB** : Stocke les données (Users, Services, Bookings).  
- **MongoDB** → **MongoDB Atlas** : Hébergement cloud.  

---

### **5. Diagramme d’Activité (Inscription)**  
1. **Utilisateur** : Remplit le formulaire (email, rôle, mot de passe).  
2. **Système** : Vérifie l’unicité de l’email.  
3. **Système** : Envoie un lien d’activation par email.  
4. **Utilisateur** : Clique sur le lien → Active le compte.  
5. **Système** : Redirige vers la page de connexion.  

---

### **6. Contraintes Techniques**  
- **Sécurité** :  
  - HTTPS obligatoire.  
  - RBAC (Client = READ sur les services, Artisan = WRITE sur ses services).  
- **Performance** :  
  - Index MongoDB sur `location` (recherche géospatiale).  
  - Cache des résultats de recherche (Redis).  

---
