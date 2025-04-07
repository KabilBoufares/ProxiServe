// Utilitaires généraux
const utils = {
    // Formater une date
    formatDate: (dateString) => {
        return new Date(dateString).toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    },

    // Formater un prix
    formatPrice: (price) => {
        return new Intl.NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(price);
    },

    // Afficher une notification
    showNotification: (message, type = 'success') => {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    },

    // Gérer les modales
    openModal: (modalId) => {
        const modal = document.getElementById(modalId);
        modal.classList.add('active');
    },

    closeModal: (modalId) => {
        const modal = document.getElementById(modalId);
        modal.classList.remove('active');
    },

    // Validation des formulaires
    validateForm: (formData, rules) => {
        const errors = {};
        
        for (const [field, value] of Object.entries(formData)) {
            if (rules[field]) {
                const fieldRules = rules[field];

                if (fieldRules.required && !value) {
                    errors[field] = 'Ce champ est requis';
                }

                if (fieldRules.minLength && value.length < fieldRules.minLength) {
                    errors[field] = `Minimum ${fieldRules.minLength} caractères requis`;
                }

                if (fieldRules.maxLength && value.length > fieldRules.maxLength) {
                    errors[field] = `Maximum ${fieldRules.maxLength} caractères autorisés`;
                }

                if (fieldRules.pattern && !fieldRules.pattern.test(value)) {
                    errors[field] = fieldRules.message || 'Format invalide';
                }
            }
        }

        return {
            isValid: Object.keys(errors).length === 0,
            errors
        };
    },

    // Gérer les erreurs API
    handleApiError: (error) => {
        console.error('Erreur:', error);
        utils.showNotification(
            error.message || 'Une erreur est survenue',
            'error'
        );
    },

   
    // Créer des étoiles pour les avis (version DOM-compatible)
    createStars: (rating) => {
        const container = document.createElement('span');
        for (let i = 1; i <= 5; i++) {
            const star = document.createElement('i');
            star.className = i <= rating ? 'fas fa-star' : 'far fa-star';
            container.appendChild(star);
        }
        return container;
    },


    // Générer un identifiant unique
    generateId: () => {
        return Math.random().toString(36).substr(2, 9);
    },

    // Débouncer une fonction
    debounce: (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    // Vérifier si une image est valide
    isValidImage: (file) => {
        const validTypes = ['image/jpeg', 'image/png'];
        const maxSize = 5 * 1024 * 1024; // 5MB

        if (!validTypes.includes(file.type)) {
            utils.showNotification('Format d\'image non supporté. Utilisez JPG ou PNG.', 'error');
            return false;
        }

        if (file.size > maxSize) {
            utils.showNotification('L\'image est trop volumineuse. Maximum 5MB.', 'error');
            return false;
        }

        return true;
    },

    // Créer un élément HTML avec des attributs
    createElement: (tag, attributes = {}, children = []) => {
        const element = document.createElement(tag);
        
        for (const [key, value] of Object.entries(attributes)) {
            if (key === 'className') {
                element.className = value;
            } else {
                element.setAttribute(key, value);
            }
        }

        children.forEach(child => {
            if (typeof child === 'string') {
                element.appendChild(document.createTextNode(child));
            } else {
                element.appendChild(child);
            }
        });

        return element;
    }
};