// Gestion des services
const services = {
    data: [],

    // Initialisation
    init: async () => {
        try {
            const artisanId = localStorage.getItem('artisanId');
            services.data = await api.getArtisanServices(artisanId);
            services.render();
            services.setupEventListeners();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Affichage des services
    render: () => {
        const servicesList = document.getElementById('servicesList');
        servicesList.innerHTML = '';

        services.data.forEach(service => {
            const serviceElement = utils.createElement('div', { className: 'service-card' }, [
                utils.createElement('div', { className: 'service-info' }, [
                    utils.createElement('h3', {}, [service.title]),
                    utils.createElement('p', {}, [service.description]),
                    utils.createElement('p', { className: 'price' }, [
                        utils.formatPrice(service.price)
                    ])
                ]),
                utils.createElement('div', { className: 'service-actions' }, [
                    utils.createElement('button', {
                        className: 'btn-secondary',
                        'data-action': 'edit',
                        'data-id': service.id
                    }, [
                        utils.createElement('i', { className: 'fas fa-edit' })
                    ]),
                    utils.createElement('button', {
                        className: 'btn-danger',
                        'data-action': 'delete',
                        'data-id': service.id
                    }, [
                        utils.createElement('i', { className: 'fas fa-trash' })
                    ])
                ])
            ]);

            servicesList.appendChild(serviceElement);
        });
    },

    // Configuration des écouteurs d'événements
    setupEventListeners: () => {
        // Ouverture du modal d'ajout
        const addServiceBtn = document.getElementById('addServiceBtn');
        if (addServiceBtn) {
            addServiceBtn.addEventListener('click', () => {
                const form = document.getElementById('serviceForm');
                if (form) {
                    form.reset();
                    delete form.dataset.serviceId;
                    utils.openModal('serviceModal');
                }
            });
        }

        // Soumission du formulaire
        const serviceForm = document.getElementById('serviceForm');
        if (serviceForm) {
            serviceForm.addEventListener('submit', services.handleSubmitService);
        }

        // Actions sur les services
        const servicesList = document.getElementById('servicesList');
        if (servicesList) {
            servicesList.addEventListener('click', (e) => {
                const button = e.target.closest('button[data-action]');
                if (!button) return;

                const { action, id } = button.dataset;
                if (action === 'edit') {
                    services.handleEditService(id);
                } else if (action === 'delete') {
                    services.handleDeleteService(id);
                }
            });
        }
    },

    // Ajout/Modification d'un service
    handleSubmitService: async (event) => {
        event.preventDefault();

        const formData = {
            title: document.getElementById('serviceTitle').value,
            description: document.getElementById('serviceDescription').value,
            price: parseFloat(document.getElementById('servicePrice').value)
        };

        // Validation
        const validation = utils.validateForm(formData, {
            title: { required: true, minLength: 3 },
            description: { required: true, minLength: 10 },
            price: { required: true, min: 0 }
        });

        if (!validation.isValid) {
            Object.values(validation.errors).forEach(error => {
                utils.showNotification(error, 'error');
            });
            return;
        }

        try {
            const serviceId = event.target.dataset.serviceId;
            
            if (serviceId) {
                // Modification
                await api.updateService(serviceId, formData);
                services.data = services.data.map(service =>
                    service.id === serviceId ? { ...service, ...formData } : service
                );
                utils.showNotification('Service mis à jour');
            } else {
                // Création
                const response = await api.createService(formData);
                services.data.push({ id: response.id, ...formData });
                utils.showNotification('Service créé');
            }

            services.render();
            utils.closeModal('serviceModal');
            event.target.reset();
            delete event.target.dataset.serviceId;
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Édition d'un service
    handleEditService: (serviceId) => {
        const service = services.data.find(s => s.id === serviceId);
        if (!service) return;

        const form = document.getElementById('serviceForm');
        if (!form) return;

        form.dataset.serviceId = serviceId;
        
        const titleInput = document.getElementById('serviceTitle');
        const descriptionInput = document.getElementById('serviceDescription');
        const priceInput = document.getElementById('servicePrice');

        if (titleInput) titleInput.value = service.title;
        if (descriptionInput) descriptionInput.value = service.description;
        if (priceInput) priceInput.value = service.price.toString();

        utils.openModal('serviceModal');
    },

    // Suppression d'un service
    handleDeleteService: async (serviceId) => {
        if (!confirm('Voulez-vous vraiment supprimer ce service ?')) return;

        try {
            await api.deleteService(serviceId);
            services.data = services.data.filter(service => service.id !== serviceId);
            services.render();
            utils.showNotification('Service supprimé');
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};