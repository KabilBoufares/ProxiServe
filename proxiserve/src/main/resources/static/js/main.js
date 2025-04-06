document.addEventListener('DOMContentLoaded', async () => {
    console.log("🔄 Chargement du dashboard...");

    const token = localStorage.getItem('token');

    // 🔐 Redirection vers login si token manquant
    if (!token) {
        console.warn("❌ Aucun token trouvé. Redirection vers /login");
        window.location.href = "/login";
        return;
    }

    try {
        // 📡 Récupération de l'artisan connecté via token
        const res = await fetch('http://localhost:8080/api/artisans/profile', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) {
            console.error("❌ Token invalide ou artisan introuvable");
            throw new Error("Token invalide ou artisan introuvable");
        }

        const data = await res.json();
        console.log("✅ Artisan récupéré :", data);

        // Stocker ID artisan
        localStorage.setItem('artisanId', data.id);

        // 🧩 Initialiser chaque module s'il existe
        if (typeof profile !== 'undefined') {
            console.log("🔧 Initialisation module : profile");
            profile.init();
        }
        if (typeof skills !== 'undefined') {
            console.log("🔧 Initialisation module : skills");
            skills.init();
        }
        if (typeof certifications !== 'undefined') {
            console.log("🔧 Initialisation module : certifications");
            certifications.init();
        }
        if (typeof photos !== 'undefined') {
            console.log("🔧 Initialisation module : photos");
            photos.init();
        }
        if (typeof services !== 'undefined') {
            console.log("🔧 Initialisation module : services");
            services.init();
        }
        if (typeof bookings !== 'undefined') {
            console.log("🔧 Initialisation module : bookings");
            bookings.init();
        }
        if (typeof reviews !== 'undefined') {
            console.log("🔧 Initialisation module : reviews");
            reviews.init();
        }

    } catch (err) {
        console.error("⚠️ Erreur lors du chargement du dashboard :", err.message);
        localStorage.removeItem('token');
        localStorage.removeItem('artisanId');
        window.location.href = "/login";
    }

    // 🧭 Navigation entre les onglets
    const navLinks = document.querySelectorAll('.nav-links li');
    const tabContents = document.querySelectorAll('.tab-content');

    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            const tabId = link.dataset.tab;
            navLinks.forEach(l => l.classList.remove('active'));
            tabContents.forEach(t => t.classList.remove('active'));

            link.classList.add('active');
            document.getElementById(tabId).classList.add('active');
        });
    });

    // 🚪 Bouton de déconnexion
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            console.log("🔓 Déconnexion...");
            localStorage.removeItem("token");
            localStorage.removeItem("artisanId");
            window.location.href = "/login";
        });
    }

    // ⛔ Fermer une modale quand on clique dehors
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.classList.remove('active');
        }
    });
});
