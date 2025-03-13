

## **🌐 Architecture des Pages**
Chaque page aura une disposition claire et intuitive avec des boutons bien positionnés et des champs bien espacés pour une bonne expérience utilisateur.

---

### **1️⃣ Page de Connexion (Login)**
📍 **URL :** `/login`

#### 🏗️ **Disposition de la page :**
- **Logo ou Nom de l'Application** (en haut, centré)
- **Titre :** "Connexion à Proxiserve"
- **Champs :**
  - 📧 **Email** (`email`)
  - 🔒 **Mot de passe** (`password`)
- ✅ **Checkbox :** "Se souvenir de moi"
- 🔵 **Bouton "Se connecter"** (principal)
- 🔄 **Lien "Mot de passe oublié ?"** (vers la page Reset Password)
- 🆕 **Lien "Créer un compte"** (vers la page Signup)

---

### **2️⃣ Page d'Inscription (Signup)**
📍 **URL :** `/signup`

#### 🏗️ **Disposition de la page :**
- **Logo ou Nom de l'Application**
- **Titre :** "Créer un compte"
- **Champs :**
  - 📧 **Email** (`email`)
  - 🔒 **Mot de passe** (`password`)
  - 🔄 **Confirmer le mot de passe** (`confirm_password`)
  - 📛 **Nom complet** (`fullName`)
  - 📞 **Numéro de téléphone** (`phoneNumber`)
  - 🎭 **Sélecteur de rôle :**
    - 🏠 `ROLE_CLIENT` (Client)
    - 🛠️ `ROLE_ARTISAN` (Artisan)
- 🟢 **Bouton "Créer un compte"**
- 📌 **Lien "Déjà un compte ? Se connecter"** (vers la page Login)

---
➖ Séparateur ("ou s'inscrire / login avec")
🔴 Bouton "S'inscrire/login  avec Google"    remarque dans les deux page .

### **3️⃣ Page de Réinitialisation du Mot de Passe (Reset Password)**
📍 **URL :** `/reset-password`

#### 🏗️ **Disposition de la page :**
- **Logo ou Nom de l'Application**
- **Titre :** "Réinitialiser votre mot de passe"
- **Étape 1 : Demande de réinitialisation**
  - 📧 **Email** (`email`)
  - 📩 **Bouton "Envoyer un lien de réinitialisation"** (envoi d'un email)
- **Étape 2 : Nouvelle entrée du mot de passe**
  - 🔑 **Nouveau mot de passe** (`new_password`)
  - 🔄 **Confirmer le mot de passe** (`confirm_new_password`)
  - 🔵 **Bouton "Réinitialiser mon mot de passe"**
- 🔙 **Lien "Retour à la connexion"** (vers la page Login)
