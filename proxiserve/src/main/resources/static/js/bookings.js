const bookings = {
    data: [],
    currentFilter: 'all',

    // Initialisation
    init: async () => {
        try {
            bookings.data = await api.getBookings();
            await bookings.render();
            bookings.setupEventListeners();

            // 🔄 Auto-refresh toutes les 30 secondes (si l'onglet est actif)
            setInterval(async () => {
                if (document.visibilityState === 'visible') {
                    bookings.data = await api.getBookings();
                    await bookings.render();
                }
            }, 30000); // 30 sec
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Rendu des réservations
    render: async () => {
        const bookingsList = document.getElementById('bookingsList');
        bookingsList.innerHTML = '';

        const filteredBookings = bookings.currentFilter === 'all'
            ? bookings.data
            : bookings.data.filter(b => b.status === bookings.currentFilter);

        if (filteredBookings.length === 0) {
            bookingsList.innerHTML = '<p class="no-bookings">Aucune réservation trouvée</p>';
            return;
        }

        const sortedBookings = [...filteredBookings].sort((a, b) => {
            const statusOrder = {
                'PENDING': 0,
                'CONFIRMED': 1,
                'REJECTED': 2,
                'COMPLETED': 3
            };
            const statusComparison = statusOrder[a.status] - statusOrder[b.status];
            if (statusComparison !== 0) return statusComparison;
    
            // Tri par date décroissante
            return new Date(b.bookingDate) - new Date(a.bookingDate);
        });
        for (const booking of sortedBookings) {
            let address = 'Non spécifiée';
            if (booking.location?.coordinates) {
                const [lon, lat] = booking.location.coordinates;
                try {
                    const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
                    const data = await response.json();
                    address = data.display_name || 'Adresse introuvable';
                } catch (err) {
                    console.error("Erreur localisation :", err);
                }
            }

            const bookingElement = utils.createElement('div', { className: 'booking-card' }, [
                utils.createElement('div', { className: 'booking-header' }, [
                    utils.createElement('h3', {}, [booking.serviceTitle]),
                    utils.createElement('span', {
                        className: `booking-status status-${booking.status.toLowerCase()}`
                    }, [bookings.getStatusLabel(booking.status)])
                ]),
                utils.createElement('div', { className: 'booking-info' }, [
                    utils.createElement('p', {}, [`Client: ${booking.clientFullName ?? 'Non défini'} (${booking.clientEmail})`]),
                    utils.createElement('p', {}, [`Téléphone: ${booking.clientPhoneNumber ?? 'N/A'}`]),
                    utils.createElement('p', {}, [`Date: ${utils.formatDate(booking.bookingDate)}`]),
                    utils.createElement('p', {}, [`Description: ${booking.serviceDescription ?? 'Aucune'}`]),
                    utils.createElement('p', {}, [`📍 Localisation : ${address}`])
                ]),
                utils.createElement('div', { className: 'booking-actions' },
                    bookings.getActionButtons(booking)
                )
            ]);

            bookingsList.appendChild(bookingElement);
        }
    },

    // Boutons d'action selon statut
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

    // Labels
    getStatusLabel: (status) => {
        const labels = {
            'PENDING': 'En attente',
            'CONFIRMED': 'Confirmé',
            'REJECTED': 'Rejeté',
            'COMPLETED': 'Terminé'
        };
        return labels[status] || status;
    },

    // Gestion des clics sur les boutons
    setupEventListeners: () => {
        const bookingsListElement = document.getElementById('bookingsList');
        if (bookingsListElement) {
            bookingsListElement.addEventListener('click', async (e) => {
                const btn = e.target.closest('[data-action]');
                if (!btn) return;
                const { action, id } = btn.dataset;
                await bookings.handleBookingAction(action, id);
            });
        }

        const filters = document.querySelectorAll('.bookings-filters .filter');
        filters.forEach(filter => {
            filter.addEventListener('click', () => {
                filters.forEach(f => f.classList.remove('active'));
                filter.classList.add('active');
                bookings.currentFilter = filter.getAttribute('data-status');
                bookings.render();
            });
        });
    },

    // API backend
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
                    utils.showNotification('Réservation terminée');
                    break;
                default:
                    throw new Error('Action inconnue');
            }

            bookings.data = await api.getBookings();
            await bookings.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};
