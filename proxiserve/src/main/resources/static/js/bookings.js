// Gestion des réservations
const bookings = {
    data: [],
    currentFilter: 'all',

    // Initialisation
    init: async () => {
        try {
            bookings.data = await api.getBookings();
            bookings.render();
            bookings.setupEventListeners();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Affichage des réservations
    render: () => {
        const bookingsList = document.getElementById('bookingsList');
        bookingsList.innerHTML = '';

        // Filtrer les réservations selon le statut sélectionné
        const filteredBookings = bookings.currentFilter === 'all'
            ? bookings.data
            : bookings.data.filter(booking => booking.status === bookings.currentFilter);

        // Afficher un message si aucune réservation
        if (filteredBookings.length === 0) {
            bookingsList.innerHTML = '<p class="no-bookings">Aucune réservation trouvée</p>';
            return;
        }

        // Trier les réservations par date (plus récentes en premier)
        const sortedBookings = filteredBookings.sort((a, b) => 
            new Date(b.bookingDate) - new Date(a.bookingDate)
        );

        sortedBookings.forEach(booking => {
            const bookingElement = utils.createElement('div', { className: 'booking-card' }, [
                utils.createElement('div', { className: 'booking-header' }, [
                    utils.createElement('h3', {}, [booking.serviceTitle]),
                    utils.createElement('span', {
                        className: `booking-status status-${booking.status.toLowerCase()}`
                    }, [bookings.getStatusLabel(booking.status)])
                ]),
                utils.createElement('div', { className: 'booking-info' }, [
                    utils.createElement('p', {}, [
                        `Client: ${booking.clientName} (${booking.clientEmail})`
                    ]),
                    utils.createElement('p', {}, [
                        `Date: ${utils.formatDate(booking.bookingDate)}`
                    ]),
                    utils.createElement('p', {}, [
                        `Prix: ${utils.formatPrice(booking.servicePrice)}`
                    ])
                ]),
                utils.createElement('div', { className: 'booking-actions' }, 
                    bookings.getActionButtons(booking)
                )
            ]);

            bookingsList.appendChild(bookingElement);
        });
    },

    // Configuration des écouteurs d'événements
    setupEventListeners: () => {
        // Filtres de réservations
        const filters = document.querySelectorAll('.bookings-filters .filter');
        
        filters.forEach(filter => {
            filter.addEventListener('click', () => {
                // Mise à jour des classes actives
                filters.forEach(f => f.classList.remove('active'));
                filter.classList.add('active');

                // Mise à jour du filtre et rafraîchissement
                bookings.currentFilter = filter.getAttribute('data-status');
                bookings.render();
            });
        });

        // Actions sur les réservations
        const bookingsListElement = document.getElementById('bookingsList');
        if (bookingsListElement) {
            bookingsListElement.addEventListener('click', async (e) => {
                const actionBtn = e.target.closest('[data-action]');
                if (!actionBtn) return;

                const { action, id } = actionBtn.dataset;
                await bookings.handleBookingAction(action, id);
            });
        }
    },

    // Obtention du label de statut
    getStatusLabel: (status) => {
        const labels = {
            'PENDING': 'En attente',
            'CONFIRMED': 'Confirmé',
            'REJECTED': 'Rejeté',
            'COMPLETED': 'Terminé'
        };
        return labels[status] || status;
    },

    // Obtention des boutons d'action selon le statut
    getActionButtons: (booking) => {
        const buttons = [];

        switch (booking.status) {
            case 'PENDING':
                buttons.push(
                    utils.createElement('button', {
                        className: 'btn-success',
                        'data-action': 'confirm',
                        'data-id': booking.id
                    }, ['Confirmer']),
                    utils.createElement('button', {
                        className: 'btn-danger',
                        'data-action': 'reject',
                        'data-id': booking.id
                    }, ['Rejeter'])
                );
                break;
            case 'CONFIRMED':
                buttons.push(
                    utils.createElement('button', {
                        className: 'btn-primary',
                        'data-action': 'complete',
                        'data-id': booking.id
                    }, ['Marquer comme terminé'])
                );
                break;
        }

        return buttons;
    },

    // Gestion des actions sur les réservations
    handleBookingAction: async (action, bookingId) => {
        try {
            switch (action) {
                case 'confirm':
                    await api.confirmBooking(bookingId);
                    utils.showNotification('Réservation confirmée');
                    break;
                case 'reject':
                    await api.rejectBooking(bookingId);
                    utils.showNotification('Réservation rejetée');
                    break;
                case 'complete':
                    await api.completeBooking(bookingId);
                    utils.showNotification('Réservation marquée comme terminée');
                    break;
                default:
                    throw new Error('Action non reconnue');
            }

            // Mise à jour des données et rafraîchissement
            bookings.data = await api.getBookings();
            bookings.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};