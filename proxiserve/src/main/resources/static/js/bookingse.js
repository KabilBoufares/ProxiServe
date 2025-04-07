// ✅ Vérification authentification + récupération serviceId + artisanId
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("Veuillez vous connecter pour accéder à cette page.");
        window.location.href = "/login";
        return;
    }

    try {
        const payloadBase64 = token.split('.')[1];
        const decodedPayload = JSON.parse(atob(payloadBase64));
        const role = decodedPayload.role;

        if (role !== "ROLE_CLIENT") {
            alert("Seuls les clients peuvent accéder à cette page.");
            window.location.href = "/login";
            return;
        }
    } catch (err) {
        console.error("Erreur lors du décodage du token :", err);
        localStorage.clear();
        alert("Token invalide. Veuillez vous reconnecter.");
        window.location.href = "/login";
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const serviceId = params.get("serviceId");
    const artisanId = params.get("artisanId");
    console.log("👉 ID de l'artisan reçu :", artisanId); 

    if (!serviceId || !artisanId) {
        alert("Informations de réservation manquantes. Retour à la recherche.");
        window.location.href = "/services";
        return;
    }

    const serviceIdField = document.getElementById("serviceId");
    if (serviceIdField) {
        serviceIdField.value = serviceId;
        serviceIdField.readOnly = true;
    }

    const artisanIdField = document.createElement("input");
    artisanIdField.type = "hidden";
    artisanIdField.id = "artisanId";
    artisanIdField.value = artisanId;
    document.getElementById("reservationForm").appendChild(artisanIdField);

    fetchBookings();
});

// ✅ Variables
let currentBookings = [];

// ✅ Références DOM
const bookingForm = document.getElementById('bookingForm');
const bookingsList = document.getElementById('bookingsList');
const bookingsContainer = document.getElementById('bookingsContainer');
const artisanModal = document.getElementById('artisanModal');
const artisanProfile = document.getElementById('artisanProfile');

// ✅ Navigation
document.getElementById('showBookingsBtn').addEventListener('click', () => {
    bookingForm.classList.add('hidden');
    bookingsList.classList.remove('hidden');
    fetchBookings();
});

document.getElementById('newBookingBtn').addEventListener('click', () => {
    bookingsList.classList.add('hidden');
    bookingForm.classList.remove('hidden');
});

document.getElementById('backToSearchBtn').addEventListener('click', () => {
    window.location.href = '/services';
});

document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = '/login';
});

// ✅ Fermeture modale
document.querySelector('.close').addEventListener('click', () => {
    artisanModal.style.display = 'none';
});
window.addEventListener('click', (event) => {
    if (event.target === artisanModal) artisanModal.style.display = 'none';
});

// ✅ Géocodage réel de l’adresse
async function getCoordinates(address) {
    const encodedAddress = encodeURIComponent(address);
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodedAddress}`;

    try {
        const response = await fetch(url);
        const data = await response.json();
        if (data.length === 0) throw new Error("Adresse introuvable.");
        return {
            latitude: parseFloat(data[0].lat),
            longitude: parseFloat(data[0].lon)
        };
    } catch {
        return { latitude: 0.0, longitude: 0.0 };
    }
}

// ✅ Récupérer réservations du client
async function fetchBookings() {
    try {
        const token = localStorage.getItem("token");
        const response = await fetch('/api/bookings/client', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur fetch');
        currentBookings = await response.json();
        displayBookings();
    } catch (error) {
        console.error('Erreur chargement des réservations:', error);
        alert('Erreur lors du chargement des réservations');
    }
}

// ✅ Affichage des réservations
function displayBookings() {
    console.log("🛠️ Bookings reçus :", currentBookings);
    const urlParams = new URLSearchParams(window.location.search);
    const artisanId = urlParams.get("artisanId");
    bookingsContainer.innerHTML = currentBookings.map(booking => `
        <div class="booking-card">
            <div class="booking-header">
                <h3>${booking.serviceTitle}</h3>
                <span class="booking-status status-${booking.status.toLowerCase()}">
                    ${booking.status}
                </span>
            </div>
            <p><strong>Date :</strong> ${new Date(booking.bookingDate).toLocaleString()}</p>
            <p><strong>Prix :</strong> ${booking.servicePrice} DT</p>
            <p><strong>Description :</strong> ${booking.description || 'Aucune description'}</p>
            <div class="button-group">
                <button class="btn-primary" onclick="window.location.href='/clientViewProfile?id=${artisanId}'">Voir Artisan</button>

                ${booking.status === 'PENDING' ? `
                    <button onclick="cancelBooking('${booking.id}')" class="btn-secondary btn-danger">Annuler</button>
                ` : ''}
            </div>
        </div>
    `).join('');
}

// ✅ Envoi de la réservation (catch retiré)
bookingForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const coordinates = await getCoordinates(document.getElementById('address').value);
    const bookingData = {
        serviceId: document.getElementById('serviceId').value,
        artisanId: document.getElementById('artisanId').value,
        bookingDate: document.getElementById('bookingDate').value,
        latitude: coordinates.latitude,
        longitude: coordinates.longitude,
        description: document.getElementById('description').value
    };

    const token = localStorage.getItem("token");

    const response = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(bookingData)
    });

    alert('Réservation effectuée avec succès !');
    bookingForm.reset();
    document.getElementById('showBookingsBtn').click();
});

// ✅ Annuler une réservation
async function cancelBooking(bookingId) {
    if (!confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')) return;
    try {
        const token = localStorage.getItem("token");
        const response = await fetch(`/api/bookings/${bookingId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Échec annulation');
        alert('Réservation annulée avec succès');
        fetchBookings();
    } catch (error) {
        console.error('Erreur annulation :', error);
        alert('Erreur lors de l’annulation');
    }
}

// ✅ Profil artisan
async function viewArtisanProfile(artisanId) {
    try {
        const response = await fetch(`/api/artisans/${artisanId}/profile`);
        console.log("👉 ID de l'artisan reçu :", artisanId); 
        if (!response.ok) throw new Error('Échec profil');

        const profile = await response.json();
        artisanProfile.innerHTML = `
            <h2>${profile.name}</h2>
            <p><strong>Spécialité :</strong> ${profile.specialty}</p>
            <p><strong>Expérience :</strong> ${profile.experience} ans</p>
            <p><strong>Note :</strong> ${profile.rating}/5</p>
            <p>${profile.description}</p>
        `;
        artisanModal.style.display = 'block';
    } catch (error) {
        console.error('Erreur profil artisan :', error);
        alert('Impossible de charger le profil de l’artisan');
    }
}
