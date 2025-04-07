// --- SCROLL DOUX ---
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({ behavior: 'smooth' });
    });
});

// --- ANIMATION APPARITION ---
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('fade-in');
            observer.unobserve(entry.target);
        }
    });
}, { threshold: 0.1 });
document.querySelectorAll('.card').forEach(card => observer.observe(card));

// --- IMAGE PORTFOLIO ---
document.querySelectorAll('.portfolio-item img').forEach(img => {
    img.addEventListener('load', function () {
        this.classList.add('loaded');
    });
});

// --- MAIN ---
document.addEventListener("DOMContentLoaded", async () => {
    const artisanId = new URLSearchParams(window.location.search).get("id");
    if (!artisanId) return;

    try {
        const res = await fetch(`/api/artisans/${artisanId}/profile`);
        if (!res.ok) throw new Error("Erreur lors du chargement du profil");

        const artisan = await res.json();

        // 🔹 TITRE, NOM, MÉTIER
        document.title = `${artisan.fullName} - ${artisan.profession} | Proxiserve`;
        document.querySelector("h1").textContent = artisan.fullName;
        document.querySelector(".profession").textContent = artisan.profession;
        document.querySelector(".about-text").textContent = artisan.biography || "";

        // 🔹 PHOTO
        const profileImg = document.querySelector(".profile-photo img");
        if (profileImg && artisan.profilePictureUrl) {
            profileImg.src = artisan.profilePictureUrl;
            profileImg.alt = artisan.fullName;
        }

        // 🔹 LOCATION & WORKING HOURS (format propre)
        const infoList = document.querySelector(".info-list");
        infoList.innerHTML = `
            <div class="info-item"><i class="fas fa-map-marker-alt"></i><span>${artisan.location || "Tunisia"}</span></div>
            ${artisan.workingHoursWeekdays ? `<div class="info-item"><i class="far fa-clock"></i><span>Monday to Friday: ${artisan.workingHoursWeekdays}</span></div>` : ""}
            ${artisan.workingHoursSaturday ? `<div class="info-item"><i class="far fa-clock"></i><span>Saturday: ${artisan.workingHoursSaturday}</span></div>` : ""}
            ${artisan.workingHoursSunday ? `<div class="info-item"><i class="far fa-clock"></i><span>Sunday: ${artisan.workingHoursSunday}</span></div>` : ""}
        `;

        // 🔹 RÉSEAUX SOCIAUX
        const socials = document.querySelector(".social-links");
        socials.innerHTML = `
            ${artisan.facebook ? `<a href="${artisan.facebook}" class="social-link" target="_blank"><i class="fab fa-facebook"></i></a>` : ""}
            ${artisan.instagram ? `<a href="${artisan.instagram}" class="social-link" target="_blank"><i class="fab fa-instagram"></i></a>` : ""}
            ${artisan.linkedin ? `<a href="${artisan.linkedin}" class="social-link" target="_blank"><i class="fab fa-linkedin"></i></a>` : ""}
        `;

        // 🔹 COMPÉTENCES
        const skillsList = document.querySelector(".skills-list");
        if (Array.isArray(artisan.skills)) {
            skillsList.innerHTML = artisan.skills.map(skill => `<li><i class="fas fa-check"></i>${skill}</li>`).join('');
        }

        // 🔹 CERTIFICATIONS
        const certList = document.querySelector(".cert-list");
        if (Array.isArray(artisan.certifications)) {
            certList.innerHTML = artisan.certifications.map(cert => `
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

        // 🔹 PORTFOLIO
        const portfolioGrid = document.querySelector(".portfolio-grid");
        if (Array.isArray(artisan.workPhotoUrls)) {
            portfolioGrid.innerHTML = artisan.workPhotoUrls.map(url => `
                <div class="portfolio-item">
                    <img src="${url}" alt="Work" onerror="this.src='https://via.placeholder.com/300x200?text=Image+indisponible';">
                </div>
            `).join('');
        }

        // 🔹 SERVICES (PRIX)
        const serviceRes = await fetch(`/api/services/artisan/${artisanId}`);
        const services = await serviceRes.json();
        const pricingGrid = document.querySelector(".pricing-grid");
        if (Array.isArray(services)) {
            pricingGrid.innerHTML = services.map(s => `
                <div class="price-card">
                    <h3>${s.title}</h3>
                    <p class="price">${s.price ? `${s.price} DT` : "Tarif sur devis"}</p>
                </div>
            `).join('');
        }

        // 🔹 BOUTON CONTACT → WhatsApp OU Mail
        const contactBtn = document.querySelector(".btn-white");
        if (artisan.phoneNumber) {
            contactBtn.onclick = () => {
                const clean = artisan.phoneNumber.replace(/[^0-9]/g, '');
                window.open(`https://wa.me/${clean}`, '_blank');
            };
        }

        // 🔹 BOUTON DEMANDE DE DEVIS → Mail
        const quoteBtn = document.querySelector(".btn-accent");
        if (artisan.email) {
            quoteBtn.onclick = () => {
                const subject = encodeURIComponent("Demande de devis via Proxiserve");
                const body = encodeURIComponent(`Bonjour ${artisan.fullName},\n\nJe suis intéressé(e) par vos services. Pourriez-vous me fournir un devis ?\n\nMerci.`);
                window.location.href = `mailto:${artisan.email}?subject=${subject}&body=${body}`;
            };
        }

        // 🔹 AVIS CLIENTS
        const statsRes = await fetch(`/api/reviews/stats/${artisanId}`);
        const reviewsRes = await fetch(`/api/reviews/artisan/${artisanId}`);
        const stats = await statsRes.json();
        const reviews = await reviewsRes.json();

        const starsContainer = document.getElementById("review-stars");
        const ratingScore = document.getElementById("rating-score");
        const reviewCount = document.getElementById("review-count");
        const reviewsList = document.getElementById("reviews-list");

        const avg = parseFloat(stats.averageRating || 0).toFixed(1);
        const full = Math.floor(avg);
        const half = avg - full >= 0.5;

        starsContainer.innerHTML = `
            ${'<i class="fas fa-star"></i>'.repeat(full)}
            ${half ? '<i class="fas fa-star-half-alt"></i>' : ''}
            ${'<i class="far fa-star"></i>'.repeat(5 - full - (half ? 1 : 0))}
        `;
        ratingScore.textContent = avg;
        reviewCount.textContent = `(${stats.totalReviews} review${stats.totalReviews > 1 ? 's' : ''})`;

        reviewsList.innerHTML = reviews.length > 0 ? reviews.map(r => `
            <div class="review-card">
                <div class="stars">${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}</div>
                <p>"${r.comment}"</p>
            </div>
        `).join('') : "<p>No reviews yet.</p>";

    } catch (err) {
        console.error(err);
        document.querySelector(".main-content").innerHTML = "<p style='color:red;'>Erreur de chargement du profil artisan.</p>";
    }
});
