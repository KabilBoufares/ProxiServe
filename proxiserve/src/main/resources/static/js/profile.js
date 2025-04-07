const profile = {
    data: null,
    originalData: null,

    // Initialisation
    init: async () => {
        try {
            const artisanId = localStorage.getItem('artisanId');
            if (!artisanId) throw new Error('ID artisan non trouvé');

            profile.data = await api.getProfile(artisanId);
            profile.originalData = { ...profile.data };

            profile.render();
            profile.setupEventListeners();

            if (typeof certifications !== 'undefined') certifications.render();
            if (typeof skills !== 'undefined') skills.render();
            if (typeof photos !== 'undefined') photos.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Rendu du formulaire
    render: () => {
        if (!profile.data) return;

        const d = profile.data;

        document.getElementById('email').value = d.email || '';
        document.getElementById('phoneNumber').value = d.phoneNumber || '';
        document.getElementById('biography').value = d.biography || '';
        document.getElementById('profession').value = d.profession || '';
        document.getElementById('companyName').value = d.companyName || '';
        document.getElementById('workingHoursWeekdays').value = d.workingHoursWeekdays || '';
        document.getElementById('workingHoursSaturday').value = d.workingHoursSaturday || '';
        document.getElementById('workingHoursSunday').value = d.workingHoursSunday || '';

        const profilePicture = document.getElementById('profilePicture');
        if (profilePicture) {
            profilePicture.src = d.profilePictureUrl || 'https://placehold.co/100x100';
        }

        document.getElementById('artisanName').textContent =
            d.fullName || d.companyName || 'Artisan';

        profile.setReadonlyMode(true);
    },

    // Active ou désactive la lecture seule
    setReadonlyMode: (readonly) => {
        const fields = [
            'phoneNumber',
            'biography',
            'profession',
            'companyName',
            'workingHoursWeekdays',
            'workingHoursSaturday',
            'workingHoursSunday'
        ];

        fields.forEach(id => {
            const field = document.getElementById(id);
            if (readonly) {
                field.setAttribute('readonly', true);
            } else {
                field.removeAttribute('readonly');
            }
        });

        document.getElementById('editProfileBtn').style.display = readonly ? 'block' : 'none';
        document.getElementById('actionButtons').style.display = readonly ? 'none' : 'flex';
    },

    // Événements
    setupEventListeners: () => {
        document.getElementById('editProfileBtn').addEventListener('click', () => {
            profile.setReadonlyMode(false);
        });

        document.getElementById('cancelEdit').addEventListener('click', () => {
            profile.render(); // recharge données originales
        });

        document.getElementById('profileForm').addEventListener('submit', profile.handleProfileSubmit);

        document.getElementById('uploadPhotoBtn')?.addEventListener('click', () => {
            document.getElementById('profilePictureInput').click();
        });

        document.getElementById('profilePictureInput')?.addEventListener('change', profile.handleProfilePictureUpload);
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

        const validation = utils.validateForm(formData, {
            phoneNumber: { pattern: /^\d{8,}$/, message: 'Numéro invalide (au moins 8 chiffres)' },
            biography: { minLength: 10 }
        });

        if (!validation.isValid) {
            Object.entries(validation.errors).forEach(([_, msg]) =>
                utils.showNotification(msg, 'error')
            );
            return;
        }

        try {
            const artisanId = localStorage.getItem('artisanId');
            await api.updateProfile(artisanId, formData);
            profile.data = { ...profile.data, ...formData };
            utils.showNotification('Profil mis à jour avec succès');
            profile.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Upload de photo de profil
    handleProfilePictureUpload: async (event) => {
        const file = event.target.files[0];
        if (!file || !utils.isValidImage(file)) return;

        try {
            const url = await api.uploadProfilePicture(file);
            profile.data.profilePictureUrl = url;
            utils.showNotification('Photo de profil mise à jour');
            profile.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};
