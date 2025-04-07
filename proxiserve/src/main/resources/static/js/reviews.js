const reviews = {
    data: {
        reviews: [],
        stats: null
    },

    // Initialisation
    init: async () => {
        try {
            const artisanId = localStorage.getItem('artisanId');

            const [reviewsData, statsData] = await Promise.all([
                api.getReviews(artisanId),
                api.getReviewStats(artisanId)
            ]);

            reviews.data.reviews = reviewsData;
            reviews.data.stats = statsData;

            reviews.render();
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Rendu global
    render: () => {
        reviews.renderStats();
        reviews.renderReviews();
    },

    // Rendu des statistiques
    renderStats: () => {
        const stats = reviews.data.stats;
        const statsContainer = document.getElementById('reviewsStats');
        statsContainer.innerHTML = '';

        if (!stats || stats.totalReviews === 0) {
            statsContainer.innerHTML = '<p class="no-stats">Aucune statistique disponible</p>';
            return;
        }

        const averageRating = utils.createElement('div', { className: 'stats-item' }, [
            utils.createElement('h3', {}, ['Note moyenne']),
            utils.createElement('div', { className: 'rating-big' }, [
                utils.createElement('span', { className: 'stars' }, [
                    utils.createStars(stats.averageRating)
                ]),
                utils.createElement('span', { className: 'rating-value' }, [
                    stats.averageRating.toFixed(1)
                ])
            ])
        ]);

        const totalReviews = utils.createElement('div', { className: 'stats-item' }, [
            utils.createElement('h3', {}, ['Nombre d\'avis']),
            utils.createElement('div', { className: 'total-reviews' }, [
                stats.totalReviews.toString()
            ])
        ]);

        const starsBreakdown = utils.createElement('div', { className: 'stats-item full-width' }, [
            utils.createElement('h3', {}, ['Répartition des notes']),
            utils.createElement('div', { className: 'stars-breakdown' },
                Object.entries(stats.ratingDistribution || {})
                    .sort((a, b) => b[0] - a[0])
                    .map(([stars, count]) => {
                        const percentage = stats.totalReviews > 0 ? (count / stats.totalReviews) * 100 : 0;
                        return utils.createElement('div', { className: 'breakdown-row' }, [
                            utils.createElement('span', { className: 'stars-count' }, [
                                `${stars} étoile${stars > 1 ? 's' : ''}`
                            ]),
                            utils.createElement('div', { className: 'progress-bar' }, [
                                utils.createElement('div', {
                                    className: 'progress',
                                    style: `width: ${percentage}%`
                                })
                            ]),
                            utils.createElement('span', { className: 'reviews-count' }, [
                                count.toString()
                            ])
                        ]);
                    })
            )
        ]);

        statsContainer.appendChild(averageRating);
        statsContainer.appendChild(totalReviews);
        statsContainer.appendChild(starsBreakdown);
    },

    // Rendu des avis
    renderReviews: () => {
        const reviewsList = document.getElementById('reviewsList');
        reviewsList.innerHTML = '';

        if (reviews.data.reviews.length === 0) {
            reviewsList.appendChild(
                utils.createElement('p', { className: 'no-reviews' }, [
                    'Aucun avis pour le moment'
                ])
            );
            return;
        }

        reviews.data.reviews.forEach(review => {
            const reviewElement = utils.createElement('div', { className: 'review-card' }, [
                utils.createElement('div', { className: 'review-header' }, [
                    utils.createElement('span', { className: 'client-name' }, [review.clientName]),
                    utils.createElement('span', { className: 'review-date' }, [
                        utils.formatDate(review.createdAt)
                    ])
                ]),
                utils.createElement('div', { className: 'stars' }, [
                    utils.createStars(review.rating)
                ]),
                utils.createElement('p', { className: 'review-comment' }, [
                    review.comment
                ])
            ]);

            reviewsList.appendChild(reviewElement);
        });
    }
};
