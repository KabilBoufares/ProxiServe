// Configuration de l'API
const API_URL = 'http://localhost:8080/api';

// Gestionnaire d'erreurs
const handleError = (error) => {
    console.error('Erreur API:', error);
    throw error;
};

// Fonction pour récupérer le token JWT (désactivée temporairement)
const getToken = () => localStorage.getItem('token');


// Configuration des headers (authentification désactivée)
const getHeaders = (contentType = 'application/json') => {
    const headers = {
        'Content-Type': contentType
    };

    const token = getToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    return headers;
};

// API Client
const api = {
    // Profil
    getProfile: async (id) => {
        try {
            const response = await fetch(`${API_URL}/artisans/${id}/profile`, {
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération du profil');
            return await response.json();
        } catch (error) {
            handleError(error);
        }
    },

    updateProfile: async (id, data) => {
        try {
            const response = await fetch(`${API_URL}/artisans/${id}/profile`, {
                method: 'PUT',
                headers: getHeaders(),
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error('Erreur lors de la mise à jour du profil');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    uploadProfilePicture: async (file) => {
        try {
            const formData = new FormData();
            formData.append('file', file);
            const response = await fetch(`${API_URL}/artisans/profile-picture`, {
                method: 'POST',
                headers: {
                    // Pas de Content-Type pour FormData
                },
                body: formData
            });
            if (!response.ok) throw new Error('Erreur lors de l\'upload de la photo de profil');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    uploadWorkPhoto: async (file) => {
        try {
            const formData = new FormData();
            formData.append('file', file);
            const response = await fetch(`${API_URL}/artisans/work-photo`, {
                method: 'POST',
                headers: {
                    // Pas de Content-Type pour FormData
                },
                body: formData
            });
            if (!response.ok) throw new Error('Erreur lors de l\'upload de la photo');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    deleteWorkPhoto: async (id, photoUrl) => {
        try {
            const response = await fetch(`${API_URL}/artisans/${id}/photos?photoUrl=${encodeURIComponent(photoUrl)}`, {
                method: 'DELETE',
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la suppression de la photo');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    deleteSkill: async (id, skill) => {
        try {
            const response = await fetch(`${API_URL}/artisans/${id}/skills?skill=${encodeURIComponent(skill)}`, {
                method: 'DELETE',
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la suppression de la compétence');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    // Certifications
    addCertification: async (data) => {
        try {
            const response = await fetch(`${API_URL}/certifications`, {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error('Erreur lors de l\'ajout de la certification');
            return await response.json();
        } catch (error) {
            handleError(error);
        }
    },

    deleteCertification: async (id) => {
        try {
            const response = await fetch(`${API_URL}/certifications/${id}`, {
                method: 'DELETE',
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la suppression de la certification');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    // Services
    createService: async (data) => {
        try {
            const response = await fetch(`${API_URL}/services`, {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error('Erreur lors de la création du service');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    getArtisanServices: async (artisanId) => {
        try {
            const response = await fetch(`${API_URL}/services/artisan/${artisanId}`, {
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des services');
            return await response.json();
        } catch (error) {
            handleError(error);
        }
    },

    updateService: async (id, data) => {
        try {
            const response = await fetch(`${API_URL}/services/${id}`, {
                method: 'PUT',
                headers: getHeaders(),
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error('Erreur lors de la mise à jour du service');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    deleteService: async (id) => {
        try {
            const response = await fetch(`${API_URL}/services/${id}`, {
                method: 'DELETE',
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la suppression du service');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    // Réservations
    getBookings: async () => {
        try {
            const response = await fetch(`${API_URL}/bookings/artisan`, {
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des réservations');
            return await response.json();
        } catch (error) {
            handleError(error);
        }
    },

    confirmBooking: async (id) => {
        try {
            const response = await fetch(`${API_URL}/bookings/${id}/confirm`, {
                method: 'PUT',
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la confirmation de la réservation');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    rejectBooking: async (id) => {
        try {
            const response = await fetch(`${API_URL}/bookings/${id}/reject`, {
                method: 'PUT',
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors du rejet de la réservation');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    completeBooking: async (id) => {
        try {
            const response = await fetch(`${API_URL}/bookings/${id}/complete`, {
                method: 'PUT',
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la completion de la réservation');
            return await response.text();
        } catch (error) {
            handleError(error);
        }
    },

    // Avis
    getReviews: async (artisanId) => {
        try {
            const response = await fetch(`${API_URL}/reviews/artisan/${artisanId}`, {
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des avis');
            return await response.json();
        } catch (error) {
            handleError(error);
        }
    },

    getReviewStats: async (artisanId) => {
        try {
            const response = await fetch(`${API_URL}/reviews/stats/${artisanId}`, {
                headers: getHeaders()
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des statistiques');
            return await response.json();
        } catch (error) {
            handleError(error);
        }
    }
};