// Gestion du profil
const profile = {
    data: null,

    // Initialisation
    init: async () => {
        try {
            const artisanId = localStorage.getItem('artisanId');
            if (!artisanId) throw new Error('ID artisan non trouvé');

            profile.data = await api.getProfile(artisanId);
            profile.render();
            profile.setupEventListeners();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Affichage du profil
    render: () => {
        if (!profile.data) return;

        // Mise à jour des champs du formulaire
        document.getElementById('email').value = profile.data.email;
        document.getElementById('phoneNumber').value = profile.data.phoneNumber;
        document.getElementById('biography').value = profile.data.biography;
        document.getElementById('profession').value = profile.data.profession;
        document.getElementById('companyName').value = profile.data.companyName;

        // Mise à jour de la photo de profil
        const profilePicture = document.getElementById('profilePicture');
        profilePicture.src = profile.data.profilePictureUrl || 'https://placehold.co/100x100';

        // Mise à jour du nom dans la sidebar
        document.getElementById('artisanName').textContent = profile.data.companyName;
    },

    // Configuration des écouteurs d'événements
    setupEventListeners: () => {
        // Formulaire de profil
        const profileForm = document.getElementById('profileForm');
        profileForm.addEventListener('submit', profile.handleProfileSubmit);

        // Upload de photo de profil
        const profilePictureInput = document.getElementById('profilePictureInput');
        profilePictureInput.addEventListener('change', profile.handleProfilePictureUpload);
    },

    // Gestion de la soumission du formulaire
    handleProfileSubmit: async (event) => {
        event.preventDefault();

        try {
            const formData = {
                phoneNumber: document.getElementById('phoneNumber').value,
                biography: document.getElementById('biography').value,
                profession: document.getElementById('profession').value,
                companyName: document.getElementById('companyName').value
            };

            // Validation
            const validation = utils.validateForm(formData, {
                phoneNumber: { required: true, pattern: /^\d{8,}$/, message: 'Numéro invalide' },
                biography: { required: true, minLength: 10 },
                profession: { required: true },
                companyName: { required: true }
            });

            if (!validation.isValid) {
                Object.entries(validation.errors).forEach(([field, error]) => {
                    utils.showNotification(error, 'error');
                });
                return;
            }

            const artisanId = localStorage.getItem('artisanId');
            await api.updateProfile(artisanId, formData);
            
            utils.showNotification('Profil mis à jour avec succès');
            profile.data = { ...profile.data, ...formData };
            profile.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Gestion de l'upload de photo de profil
    handleProfilePictureUpload: async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        if (!utils.isValidImage(file)) return;

        try {
            const photoUrl = await api.uploadProfilePicture(file);
            profile.data.profilePictureUrl = photoUrl;
            profile.render();
            utils.showNotification('Photo de profil mise à jour');
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};