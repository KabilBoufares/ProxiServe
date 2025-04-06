// Gestion des certifications
const certifications = {
    // Initialisation
    init: () => {
        certifications.render();
        certifications.setupEventListeners();
    },

    // Affichage des certifications
    render: () => {
        if (!profile.data) return;

        const certificationsList = document.getElementById('certificationsList');
        certificationsList.innerHTML = '';

        profile.data.certifications.forEach(cert => {
            const certElement = utils.createElement('div', { className: 'certification-card' }, [
                utils.createElement('div', { className: 'certification-header' }, [
                    utils.createElement('h3', {}, [cert.name]),
                    utils.createElement('button', {
                        className: 'btn-danger',
                        'data-id': cert.id
                    }, [
                        utils.createElement('i', { className: 'fas fa-trash' })
                    ])
                ]),
                utils.createElement('p', { className: 'organization' }, [cert.organization]),
                utils.createElement('p', { className: 'date' }, [
                    `Obtenue le ${utils.formatDate(cert.dateObtained)}`
                ]),
                utils.createElement('p', { className: 'description' }, [cert.description])
            ]);

            certificationsList.appendChild(certElement);
        });
    },

    // Configuration des écouteurs d'événements
    setupEventListeners: () => {
        // Ouverture du modal d'ajout
        document.getElementById('addCertificationBtn')
            .addEventListener('click', () => utils.openModal('certificationModal'));

        // Soumission du formulaire
        document.getElementById('certificationForm')
            .addEventListener('submit', certifications.handleAddCertification);

        // Suppression
        document.getElementById('certificationsList').addEventListener('click', (e) => {
            const deleteBtn = e.target.closest('.btn-danger');
            if (deleteBtn) {
                const certId = deleteBtn.dataset.id;
                certifications.handleDeleteCertification(certId);
            }
        });
    },

    // Ajout d'une certification
    handleAddCertification: async (event) => {
        event.preventDefault();

        const formData = {
            name: document.getElementById('certName').value,
            organization: document.getElementById('certOrganization').value,
            dateObtained: document.getElementById('certDate').value,
            description: document.getElementById('certDescription').value
        };

        try {
            const newCert = await api.addCertification(formData);
            profile.data.certifications.push(newCert);
            
            certifications.render();
            utils.closeModal('certificationModal');
            event.target.reset();
            utils.showNotification('Certification ajoutée');
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Suppression d'une certification
    handleDeleteCertification: async (certId) => {
        if (!confirm('Voulez-vous vraiment supprimer cette certification ?')) return;

        try {
            await api.deleteCertification(certId);
            profile.data.certifications = profile.data.certifications
                .filter(cert => cert.id !== certId);
            
            certifications.render();
            utils.showNotification('Certification supprimée');
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};