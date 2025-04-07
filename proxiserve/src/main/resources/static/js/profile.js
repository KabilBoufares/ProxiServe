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

            if (typeof certifications !== 'undefined') certifications.render();
            if (typeof skills !== 'undefined') skills.render();
            if (typeof photos !== 'undefined') photos.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Affichage du profil
    render: () => {
        if (!profile.data) return;

        document.getElementById('email').value = profile.data.email || '';
        document.getElementById('phoneNumber').value = profile.data.phoneNumber || '';
        document.getElementById('biography').value = profile.data.biography || '';
        document.getElementById('profession').value = profile.data.profession || '';
        document.getElementById('companyName').value = profile.data.companyName || '';
        document.getElementById('workingHoursWeekdays').value = profile.data.workingHoursWeekdays || '';
        document.getElementById('workingHoursSaturday').value = profile.data.workingHoursSaturday || '';
        document.getElementById('workingHoursSunday').value = profile.data.workingHoursSunday || '';

        const profilePicture = document.getElementById('profilePicture');
        profilePicture.src = profile.data.profilePictureUrl || 'https://placehold.co/100x100';

        document.getElementById('artisanName').textContent = profile.data.fullName || profile.data.companyName || 'Artisan';
    },

    // Événements
    setupEventListeners: () => {
        const profileForm = document.getElementById('profileForm');
        if (profileForm) profileForm.addEventListener('submit', profile.handleProfileSubmit);

        const profilePictureInput = document.getElementById('profilePictureInput');
        if (profilePictureInput) {
            profilePictureInput.addEventListener('change', profile.handleProfilePictureUpload);
        }
    },

    // Soumission du profil
    handleProfileSubmit: async (event) => {
        event.preventDefault();

        const fields = [
            'phoneNumber',
            'biography',
            'profession',
            'companyName',
            'workingHoursWeekdays',
            'workingHoursSaturday',
            'workingHoursSunday'
        ];

        const formData = {};
        fields.forEach(field => {
            const value = document.getElementById(field).value.trim();
            if (value !== '') formData[field] = value;
        });

        if (Object.keys(formData).length === 0) {
            utils.showNotification('Veuillez remplir au moins un champ avant de valider.', 'warning');
            return;
        }

        const validation = utils.validateForm(formData, {
            phoneNumber: { pattern: /^\d{8,}$/, message: 'Numéro invalide (au moins 8 chiffres)' },
            biography: { minLength: 10 },
            profession: {},
            companyName: {}
        });

        if (!validation.isValid) {
            Object.entries(validation.errors).forEach(([field, error]) => {
                utils.showNotification(error, 'error');
            });
            return;
        }

        try {
            const artisanId = localStorage.getItem('artisanId');
            await api.updateProfile(artisanId, formData);

            profile.data = { ...profile.data, ...formData };
            profile.render();
            utils.showNotification('Profil mis à jour avec succès');
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Upload de la photo
    handleProfilePictureUpload: async (event) => {
        const file = event.target.files[0];
        if (!file || !utils.isValidImage(file)) return;

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
