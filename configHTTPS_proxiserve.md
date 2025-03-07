# Documentation : Configuration de HTTPS pour l'API Proxiserve

## 1️⃣ Introduction

### Pourquoi activer HTTPS ?
HTTPS (HyperText Transfer Protocol Secure) permet de sécuriser les échanges entre le client et le serveur en cryptant les données. Cela empêche les attaques de type "Man-in-the-Middle" et garantit l'intégrité des informations échangées.

### Différences entre HTTP et HTTPS
| Critère          | HTTP  | HTTPS  |
|----------------|-------|--------|
| Sécurité       | Non sécurisé (les données sont en clair) | Chiffré avec SSL/TLS |
| Utilisation recommandée | Tests locaux, APIs internes | Production, sites publics, APIs sécurisées |
| Authentification | Aucune garantie | Certificat validé par une autorité de confiance |

## 2️⃣ Méthode choisie

### 📌 Pourquoi utiliser un certificat auto-signé ?
Comme nous travaillons en local, nous avons utilisé un **certificat auto-signé** plutôt qu'un certificat d'une autorité de certification comme Let's Encrypt.

**Avantages :**
✅ Simple et rapide à mettre en place.
✅ Permet de tester HTTPS sans coût.
✅ Évite d'exposer l'API en ligne.

**Limites :**
❌ Non reconnu par les navigateurs ("Not Secure" s'affiche).
❌ Ne convient pas pour un environnement de production.

### 📌 Alternative : Utiliser Ngrok
Ngrok permet d'exposer un serveur local sur un domaine public avec HTTPS, sans configuration complexe. Il sera utile pour tester l'API avec des clients distants.

## 3️⃣ Étapes suivies pour configurer HTTPS

### 1️⃣ Génération du certificat auto-signé
Nous avons utilisé `keytool`, un utilitaire Java, pour générer un certificat auto-signé :

```sh
keytool -genkey -alias proxiserve -keyalg RSA -keystore keystore.p12 -storetype PKCS12 -validity 365 -storepass changeit
```

📌 **Explication des paramètres :**
- `-genkey` : Génère une clé privée.
- `-alias proxiserve` : Nom de l’alias du certificat.
- `-keyalg RSA` : Algorithme de cryptographie utilisé.
- `-keystore keystore.p12` : Fichier contenant le certificat.
- `-storetype PKCS12` : Format sécurisé du certificat.
- `-validity 365` : Durée de validité du certificat en jours.
- `-storepass changeit` : Mot de passe du certificat.

### 2️⃣ Configuration dans `application.properties`
Nous avons ajouté ces lignes dans `application.properties` pour activer HTTPS :

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=proxiserve
```

📌 **Explication :**
- `server.port=8443` → Définit le port sécurisé.
- `server.ssl.enabled=true` → Active HTTPS.
- `server.ssl.key-store` → Spécifie le chemin du fichier de certificat.
- `server.ssl.key-store-password` → Mot de passe du keystore.
- `server.ssl.key-alias` → Alias du certificat utilisé.

### 3️⃣ Test avec HTTPS
Une fois l'API démarrée, nous avons testé HTTPS avec :

📌 **URL :** `https://localhost:8443/ping`

Le navigateur a affiché : **"API is alive! Kabil is here"**, confirmant que l’API fonctionne en HTTPS.

![image](https://github.com/user-attachments/assets/7f2ee304-7f72-4f5f-bd13-d95c88f7b40a)
 
_Exemple de réponse obtenue sur le navigateur._

## 4️⃣ Prochaines étapes

 **1. Passer à un certificat officiel (Let's Encrypt) en production**
- Let's Encrypt permet d'obtenir un certificat gratuit et reconnu.
- Nous devrons héberger l'API sur un serveur avec un nom de domaine.

 **2. Héberger le projet sur GitHub avec CI/CD**
- Automatiser le déploiement via GitHub Actions.
- Assurer la gestion des mises à jour.

 **3. Déployer l'API sur un serveur cloud avec un domaine personnalisé**
- Exemples : AWS, Azure, DigitalOcean, OVH.
- Assurer la scalabilité et la haute disponibilité.

---

### **Conclusion**
Nous avons mis en place HTTPS pour sécuriser l'API en local avec un certificat auto-signé. Cette étape est essentielle pour préparer le passage en production avec un certificat valide. **Prochaine étape : Let's Encrypt et déploiement sur un serveur cloud !** 

