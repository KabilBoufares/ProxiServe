// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({ behavior: 'smooth' });
    });
});

// Fade-in animation using IntersectionObserver
const observerOptions = {
    root: null,
    threshold: 0.1,
    rootMargin: '0px'
};
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('fade-in');
            observer.unobserve(entry.target);
        }
    });
}, observerOptions);

// Apply fade-in to all cards
document.querySelectorAll('.card').forEach(card => observer.observe(card));

// Load image animation for portfolio
document.querySelectorAll('.portfolio-item img').forEach(img => {
    img.addEventListener('load', function () {
        this.classList.add('loaded');
    });
});

// Mobile nav toggle (if present)
const menuButton = document.querySelector('.menu-toggle');
const navMenu = document.querySelector('.nav-menu');
if (menuButton) {
    menuButton.addEventListener('click', () => navMenu.classList.toggle('active'));
}

// Rating hover effects (UI only)
document.querySelectorAll('.stars i').forEach(star => {
    star.addEventListener('mouseover', function () {
        this.classList.add('hover');
    });
    star.addEventListener('mouseout', function () {
        this.classList.remove('hover');
    });
});

// DOM loaded : fetch artisan profile
document.addEventListener("DOMContentLoaded", async () => {
    const artisanId = new URLSearchParams(window.location.search).get("id");
    if (!artisanId) return;

    try {
        const res = await fetch(`/api/artisans/${artisanId}/profile`);
        if (!res.ok) throw new Error("Erreur lors du chargement du profil");

        const { artisan, certifications, fullName } = await res.json();

        // 🔹 Titre, nom complet, profession
        document.title = `${fullName} - ${artisan.profession} | Proxiserve`;
        document.querySelector("h1").textContent = fullName;
        document.querySelector(".profession").textContent = artisan.profession;
        document.querySelector(".about-text").textContent = artisan.biography || "";

        // 🔹 Photo de profil
        const profileImg = document.querySelector(".profile-photo img");
        if (profileImg && artisan.profilePictureUrl) {
            profileImg.src = artisan.profilePictureUrl;
            profileImg.alt = fullName;
        }

        // 🔹 Informations horaires
        const infoList = document.querySelector(".info-list");
        infoList.innerHTML = `
            <div class="info-item"><i class="fas fa-map-marker-alt"></i><span>${artisan.location ? "Localisé" : "Tunisia"}</span></div>
            ${artisan.workingHoursWeekdays ? `<div class="info-item"><i class="far fa-clock"></i><span>${artisan.workingHoursWeekdays}</span></div>` : ""}
            ${artisan.workingHoursSaturday ? `<div class="info-item"><i class="far fa-clock"></i><span>${artisan.workingHoursSaturday}</span></div>` : ""}
            ${artisan.workingHoursSunday ? `<div class="info-item"><i class="far fa-clock"></i><span>${artisan.workingHoursSunday}</span></div>` : ""}
        `;

        // 🔹 Réseaux sociaux
        const socials = document.querySelector(".social-links");
        socials.innerHTML = `
            ${artisan.facebook ? `<a href="${artisan.facebook}" class="social-link" target="_blank"><i class="fab fa-facebook"></i></a>` : ""}
            ${artisan.instagram ? `<a href="${artisan.instagram}" class="social-link" target="_blank"><i class="fab fa-instagram"></i></a>` : ""}
            ${artisan.linkedin ? `<a href="${artisan.linkedin}" class="social-link" target="_blank"><i class="fab fa-linkedin"></i></a>` : ""}
        `;

        // 🔹 Compétences
        const skillsList = document.querySelector(".skills-list");
        if (Array.isArray(artisan.skills)) {
            skillsList.innerHTML = artisan.skills.map(skill => `<li><i class="fas fa-check"></i>${skill}</li>`).join('');
        }

        // 🔹 Certifications
        const certList = document.querySelector(".cert-list");
        if (Array.isArray(certifications)) {
            certList.innerHTML = certifications.map(cert => `
                <div class="cert-item">
                    <i class="fas fa-certificate"></i>
                    <div class="cert-details">
                        <h3 class="cert-title">${cert.name}</h3>
                        <p class="cert-org">${cert.organization}</p>
                        <p class="cert-date">Obtained: ${cert.dateObtained}</p>
                        <p class="cert-desc">${cert.description}</p>
                    </div>
                </div>
            `).join('');
        }

        // 🔹 Portfolio
        const portfolioGrid = document.querySelector(".portfolio-grid");
        if (artisan.workPhotoUrls && Array.isArray(artisan.workPhotoUrls)) {
            portfolioGrid.innerHTML = artisan.workPhotoUrls.map(url => `
                <div class="portfolio-item">
                    <img src="${url}" alt="Work" onerror="this.src='https://via.placeholder.com/300x200?text=Image+indisponible';">
                </div>
            `).join('');
        }

        // 🔹 Services (dans pricing)
        const pricingGrid = document.querySelector(".pricing-grid");
        if (Array.isArray(artisan.serviceCategories)) {
            pricingGrid.innerHTML = artisan.serviceCategories.map(service => `
                <div class="price-card">
                    <h3>${service}</h3>
                    <p class="price">Tarif sur devis</p>
                </div>
            `).join('');
        }

    } catch (err) {
        console.error(err);
        document.querySelector(".main-content").innerHTML = "<p style='color:red;'>Erreur de chargement du profil artisan.</p>";
    }
});
