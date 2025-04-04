document.addEventListener('DOMContentLoaded', () => {
    // 🔐 Redirection si déjà connecté
    if (localStorage.getItem("token")) {
        window.location.href = "/dashboard";
        return;
    }

    // 🎞️ Carrousel d'images
    const imagePaths = [
        '/images/cleaning.avif',
        '/images/demenagement.avif',
        '/images/floorer.avif',
        '/images/plumber.avif'
    ];

    const headerBackground = document.getElementById('headerBackground');

    imagePaths.forEach(path => {
        const img = document.createElement('img');
        img.src = path;
        img.classList.add('background-image');
        headerBackground.appendChild(img);
    });

    const images = document.querySelectorAll('.background-image');
    let currentIndex = 0;

    function changeImage() {
        images[currentIndex].classList.remove('active');
        currentIndex = (currentIndex + 1) % images.length;
        images[currentIndex].classList.add('active');
    }

    if (images.length > 0) {
        images[0].classList.add('active');
        setInterval(changeImage, 5000);
    }

    // 📌 Gestion remember me
    const checkbox = document.getElementById('remember');
    const checkboxIcon = document.getElementById('checkbox-icon');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');
    const togglePassword = document.getElementById('togglePassword');

    const storedEmail = localStorage.getItem('rememberedEmail');
    const storedPassword = localStorage.getItem('rememberedPassword');

    if (storedEmail && storedPassword) {
        emailInput.value = storedEmail;
        passwordInput.value = storedPassword;
        checkbox.checked = true;
        if (checkboxIcon) checkboxIcon.textContent = 'check_box';
    }

    if (checkbox && checkboxIcon) {
        checkbox.addEventListener('change', () => {
            if (checkbox.checked) {
                checkboxIcon.textContent = 'check_box';
                localStorage.setItem('rememberedEmail', emailInput.value);
                localStorage.setItem('rememberedPassword', passwordInput.value);
            } else {
                checkboxIcon.textContent = 'check_box_outline_blank';
                localStorage.removeItem('rememberedEmail');
                localStorage.removeItem('rememberedPassword');
            }
        });
    }

    // 👁️ Toggle mot de passe
    if (togglePassword) {
        togglePassword.addEventListener('click', () => {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            togglePassword.classList.toggle('fa-eye');
            togglePassword.classList.toggle('fa-eye-slash');
        });
    }

    // 🔐 Soumission du formulaire
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = emailInput.value;
        const password = passwordInput.value;

        if (checkbox.checked) {
            localStorage.setItem('rememberedEmail', email);
            localStorage.setItem('rememberedPassword', password);
        }

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const { token } = await response.json();
                localStorage.setItem('token', token);
                errorMessage.style.display = "none";
            
                // Décodage du token pour rôle
                const payload = token.split('.')[1];
                const decoded = JSON.parse(atob(payload));
                const role = decoded.role || decoded.roles || decoded.authorities || [];
            
                const hasRole = (r) => Array.isArray(role) ? role.includes(r) : role === r;
            
                if (hasRole("ROLE_ARTISAN")) {
                    window.location.href = "/index-artisan";
                } else if (hasRole("ROLE_CLIENT")) {
                    window.location.href = "/dashboard-client.html";
                } else {
                    window.location.href = "/dashboard";
                }
            }
            
        } catch (err) {
            errorMessage.textContent = "Erreur réseau : serveur injoignable.";
            errorMessage.style.display = "block";
        }
    });
});
