document.addEventListener('DOMContentLoaded', () => {
    const resetForm = document.getElementById('resetForm');
    const resetBtn = document.querySelector('.reset-btn');
    
    resetForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = document.getElementById('email').value.trim();
        
        // Disable button and show loading state
        resetBtn.disabled = true;
        resetBtn.textContent = 'Sending...';
        
        try {
            // Validate email format
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                throw new Error('Veuillez entrer une adresse email valide.');
            }

            const response = await fetch('/api/auth/request-reset-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Erreur lors de l'envoi du lien de réinitialisation.");
            }

            alert(data.message || 'Un lien de réinitialisation vous a été envoyé. Vérifiez votre boîte mail.');
            window.location.href = '/login'; // Redirection vers la page de login

        } catch (error) {
            alert(error.message || 'Une erreur est survenue. Veuillez réessayer.');
        } finally {
            // Réactiver le bouton
            resetBtn.disabled = false;
            resetBtn.textContent = 'Send Reset Link';
        }
    });

    /* ==== Background Images === */
    const backgroundImages = [
        'images/cleaning.avif',
        'images/floorer.avif',
        'images/plumber.avif',
        'images/demenagement.avif'
    ];

    const headerBackground = document.getElementById('headerBackground');
    let currentImageIndex = 0;

    // Create initial background images
    backgroundImages.forEach((imageUrl, index) => {
        const backgroundImage = document.createElement('div');
        backgroundImage.className = `background-image ${index === 0 ? 'active' : ''}`;
        backgroundImage.style.backgroundImage = `url(${imageUrl})`;
        headerBackground.appendChild(backgroundImage);
    });

    function changeBackgroundImage() {
        const images = headerBackground.getElementsByClassName('background-image');
        images[currentImageIndex].classList.remove('active');
        currentImageIndex = (currentImageIndex + 1) % backgroundImages.length;
        images[currentImageIndex].classList.add('active');
    }

    setInterval(changeBackgroundImage, 4000);
});
