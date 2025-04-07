document.addEventListener("DOMContentLoaded", async () => {
  const servicesGrid = document.querySelector(".workers-grid");
  const searchInput = document.getElementById("searchInput");
  const searchBtn = document.querySelector(".search-btn");
  const locationInput = document.getElementById("locationInput");
  const gpsBtn = document.querySelector(".location-btn");
  const priceRange = document.getElementById("priceRange");
  const minPrice = document.getElementById("minPrice");
  const maxPrice = document.getElementById("maxPrice");
  const ratingStars = document.querySelectorAll(".rating-filter .star");
  const pageContainer = document.querySelector(".page-numbers");
  const viewButtons = document.querySelectorAll(".view-btn");

  let filters = {
    query: "",
    lat: 36.8065,
    lon: 10.1815,
    minRating: 0,
    minPrice: 0,
    maxPrice: 200,
    page: 0,
    size: 6,
    view: "grid"
  };

  // 🧠 Lecture des paramètres depuis l’URL
  const params = new URLSearchParams(window.location.search);
  const urlQuery = params.get('query') || '';
  const urlLocation = params.get('location') || '';

  if (searchInput) searchInput.value = urlQuery;
  if (locationInput) locationInput.value = urlLocation;

  if (urlQuery) filters.query = urlQuery;

  if (urlLocation) {
    try {
      const coords = await fetchCoordinates(urlLocation);
      filters.lat = coords.lat;
      filters.lon = coords.lon;
    } catch (err) {
      console.warn("Erreur de géocodage au chargement :", err);
    }
  }

  async function fetchCoordinates(city) {
    const encoded = encodeURIComponent(city);
    const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encoded}`);
    const data = await res.json();
    if (data.length > 0) {
      return {
        lat: parseFloat(data[0].lat),
        lon: parseFloat(data[0].lon)
      };
    } else {
      throw new Error("Ville introuvable.");
    }
  }

  async function fetchServices() {
    const url = new URL("http://localhost:8080/api/services/search/advanced");
    url.searchParams.append("query", filters.query);
    url.searchParams.append("latitude", filters.lat);
    url.searchParams.append("longitude", filters.lon);
    url.searchParams.append("disableDistanceCheck", true);
    url.searchParams.append("page", filters.page);
    url.searchParams.append("size", filters.size);

    try {
      const res = await fetch(url);
      const data = await res.json();
      renderServices(data);
      renderPagination(data.length);
    } catch (e) {
      servicesGrid.innerHTML = `<p style="color:red;">Erreur de chargement.</p>`;
      console.error(e);
    }
  }

  function renderServices(services) {
    const filtered = services.filter(s =>
      (s.rating || 0) >= filters.minRating &&
      s.price >= filters.minPrice &&
      s.price <= filters.maxPrice
    );

    if (filtered.length === 0) {
      servicesGrid.innerHTML = `<p style="color:#0d3b66;">Aucun service trouvé.</p>`;
      return;
    }

    servicesGrid.innerHTML = filtered.map(service => `
      <div class="service-card">
        <img src="https://via.placeholder.com/300x200" class="service-image" alt="${service.title}">
        <div class="service-info">
          <h3 class="service-name">${service.title}</h3>
          <p class="service-specialty">${service.description || ''}</p>
          <div class="service-details">
            <span><i class="fas fa-dollar-sign"></i> ${service.price} DT</span>
            <span><i class="fas fa-route"></i> ${service.distanceKm} km</span>
          </div>
          <div class="service-rating">
            <div class="stars">${generateStars(service.rating)}</div>
            <span>${(service.rating || 0).toFixed(1)} ★</span>
          </div>
          <div class="service-contact">
           <button class="contact-btn primary-btn" onclick="handleBooking('${service.id}', '${service.artisanId}')">Réserver</button>

          <button class="contact-btn secondary-btn" onclick="window.location.href='/clientViewProfile?id=${service.artisanId}','${service.artisanId}'">Voir Profil</button>


          </div>
        </div>
      </div>
    `).join('');
    updateServicesView();
  }

  function renderPagination(total) {
    const pages = Math.ceil(total / filters.size);
    let html = '';
    for (let i = 0; i < pages; i++) {
      html += `<span class="${i === filters.page ? 'active' : ''}">${i + 1}</span>`;
    }
    pageContainer.innerHTML = html;
    document.querySelectorAll(".page-numbers span").forEach(span => {
      span.addEventListener("click", () => {
        filters.page = parseInt(span.textContent) - 1;
        fetchServices();
      });
    });
  }

  function generateStars(rating = 0) {
    const full = Math.floor(rating);
    return Array.from({ length: 5 }, (_, i) => i < full ? "★" : "☆").join('');
  }

  window.handleBooking = function (serviceId, artisanId) {
    const token = localStorage.getItem("token");
    if (!token) {
      if (confirm("Vous devez être connecté pour réserver. Souhaitez-vous vous connecter ?")) {
        localStorage.setItem("pendingServiceId", serviceId);
        localStorage.setItem("pendingArtisanId", artisanId);
        window.location.href = "/login";
      }
      return;
    }
  
    try {
      const payloadBase64 = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payloadBase64));
      const role = decodedPayload.role;
  
      if (role !== "ROLE_CLIENT") {
        alert("Seuls les clients peuvent effectuer une réservation.");
        return;
      }
  
      window.location.href = `/bookings?serviceId=${serviceId}&artisanId=${artisanId}`;
    } catch (err) {
      console.error("Erreur lors de la lecture du token :", err);
      alert("Token invalide. Veuillez vous reconnecter.");
      localStorage.clear();
      window.location.href = "/login";
    }
  };
  

  // Rating
  ratingStars.forEach((star, i) => {
    star.addEventListener("click", () => {
      filters.minRating = 5 - i;
      updateStars(5 - i);
      fetchServices();
    });
  });

  function updateStars(selected) {
    ratingStars.forEach((s, i) => {
      s.classList.toggle("active", 5 - i <= selected);
    });
  }

  // Geolocation
  gpsBtn?.addEventListener("click", () => {
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        filters.lat = pos.coords.latitude;
        filters.lon = pos.coords.longitude;

        try {
          const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${filters.lat}&lon=${filters.lon}`);
          const data = await res.json();
          const city = data.address.city || data.address.town || data.address.village || data.address.county || "Unknown";
          locationInput.value = city;

          const coords = await fetchCoordinates(city);
          filters.lat = coords.lat;
          filters.lon = coords.lon;
          fetchServices();
        } catch (err) {
          console.warn("Reverse geocoding échoué.", err);
          alert("Impossible d'obtenir la ville depuis votre position.");
        }
      },
      () => alert("Impossible d’accéder à la localisation.")
    );
  });

  // Location input
  locationInput?.addEventListener("change", async () => {
    const city = locationInput.value.trim();
    if (!city) return;

    try {
      const coords = await fetchCoordinates(city);
      filters.lat = coords.lat;
      filters.lon = coords.lon;
      fetchServices();
    } catch {
      alert("Ville introuvable.");
    }
  });

  // Search input
  searchInput?.addEventListener("input", () => {
    filters.query = searchInput.value.trim();
    filters.page = 0;
    fetchServices();
  });

  // Price
  priceRange?.addEventListener("input", (e) => {
    filters.maxPrice = parseInt(e.target.value);
    maxPrice.value = filters.maxPrice;
    fetchServices();
  });

  minPrice?.addEventListener("change", (e) => {
    filters.minPrice = parseInt(e.target.value) || 0;
    fetchServices();
  });

  maxPrice?.addEventListener("change", (e) => {
    filters.maxPrice = parseInt(e.target.value) || 200;
    priceRange.value = filters.maxPrice;
    fetchServices();
  });

  // View buttons
  viewButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      filters.view = btn.dataset.view;
      updateServicesView();
    });
  });

  function updateServicesView() {
    servicesGrid.className = `services-${filters.view} workers-grid`;
    viewButtons.forEach(btn => {
      btn.classList.toggle("active", btn.dataset.view === filters.view);
    });
  }

  // Initial fetch
  fetchServices();
});
