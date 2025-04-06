// Gestion des photos
const photos = {
    // Initialisation
    init: () => {
        photos.render();
        photos.setupEventListeners();
    },

    // Affichage des photos
    render: () => {
        if (!profile.data) return;

        const photosGrid = document.getElementById('photosGrid');
        photosGrid.innerHTML = '';

        profile.data.workPhotoUrls.forEach(photoUrl => {
            const photoElement = utils.createElement('div', { className: 'photo-item' }, [
                utils.createElement('img', { src: photoUrl, alt: 'Photo de réalisation' }),
                utils.createElement('button', {
                    className: 'delete-photo',
                    'data-url': photoUrl
                }, [
                    utils.createElement('i', { className: 'fas fa-trash' })
                ])
            ]);

            photosGrid.appendChild(photoElement);
        });
    },

    // Configuration des écouteurs d'événements
    setupEventListeners: () => {
        // Upload de photos
        const workPhotoInput = document.getElementById('workPhotoInput');
        workPhotoInput.addEventListener('change', photos.handlePhotoUpload);

        // Suppression de photos
        document.getElementById('photosGrid').addEventListener('click', (e) => {
            const deleteBtn = e.target.closest('.delete-photo');
            if (deleteBtn) {
                const photoUrl = deleteBtn.dataset.url;
                photos.handleDeletePhoto(photoUrl);
            }
        });

        // Drag & drop
        const uploadZone = document.querySelector('.upload-zone');
        
        uploadZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadZone.classList.add('dragover');
        });

        uploadZone.addEventListener('dragleave', () => {
            uploadZone.classList.remove('dragover');
        });

        uploadZone.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadZone.classList.remove('dragover');
            
            const files = Array.from(e.dataTransfer.files);
            files.forEach(file => {
                if (utils.isValidImage(file)) {
                    photos.uploadPhoto(file);
                }
            });
        });
    },

    // Upload d'une photo
    uploadPhoto: async (file) => {
        try {
            const photoUrl = await api.uploadWorkPhoto(file);
            profile.data.workPhotoUrls.push(photoUrl);
            photos.render();
            utils.showNotification('Photo ajoutée');
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Gestion de l'upload de photos
    handlePhotoUpload: (event) => {
        const files = Array.from(event.target.files);
        files.forEach(file => {
            if (utils.isValidImage(file)) {
                photos.uploadPhoto(file);
            }
        });
        event.target.value = ''; // Reset input
    },

    // Suppression d'une photo
    handleDeletePhoto: async (photoUrl) => {
        if (!confirm('Voulez-vous vraiment supprimer cette photo ?')) return;

        try {
            const artisanId = localStorage.getItem('artisanId');
            await api.deleteWorkPhoto(artisanId, photoUrl);
            
            profile.data.workPhotoUrls = profile.data.workPhotoUrls
                .filter(url => url !== photoUrl);
            
            photos.render();
            utils.showNotification('Photo supprimée');
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};