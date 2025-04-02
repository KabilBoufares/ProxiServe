document.addEventListener('DOMContentLoaded', () => {
    // Toggle password visibility
    const setupPasswordToggle = (input, toggle) => {
        toggle.addEventListener('click', () => {
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);
            toggle.classList.toggle('fa-eye');
            toggle.classList.toggle('fa-eye-slash');
        });
    };

    setupPasswordToggle(document.getElementById('password'), document.getElementById('togglePassword'));
    setupPasswordToggle(document.getElementById('confirmPassword'), document.getElementById('toggleConfirmPassword'));

    // Format phone input
    const phoneInput = document.getElementById('phone');
    phoneInput.addEventListener('input', (e) => {
        let val = e.target.value.replace(/\D/g, '');
        if (val.length <= 3) {
            e.target.value = val;
        } else if (val.length <= 6) {
            e.target.value = `${val.slice(0,3)}-${val.slice(3)}`;
        } else {
            e.target.value = `${val.slice(0,3)}-${val.slice(3,6)}-${val.slice(6,10)}`;
        }
    });

    // Password match validation
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    const validatePasswords = () => {
        if (confirmPasswordInput.value && passwordInput.value !== confirmPasswordInput.value) {
            confirmPasswordInput.setCustomValidity("Passwords don't match");
        } else {
            confirmPasswordInput.setCustomValidity('');
        }
    };

    passwordInput.addEventListener('input', validatePasswords);
    confirmPasswordInput.addEventListener('input', validatePasswords);

    // Toggle location section based on role
    const roleClient = document.getElementById('roleClient');
    const roleArtisan = document.getElementById('roleArtisan');
    const locationGroup = document.getElementById('locationGroup');
    const latitudeInput = document.getElementById('latitude');
    const longitudeInput = document.getElementById('longitude');

    const toggleLocationVisibility = () => {
        if (roleArtisan.checked) {
            locationGroup.style.display = 'block';
        } else {
            locationGroup.style.display = 'none';
            latitudeInput.value = '';
            longitudeInput.value = '';
        }
    };

    toggleLocationVisibility();
    roleClient.addEventListener('change', toggleLocationVisibility);
    roleArtisan.addEventListener('change', toggleLocationVisibility);

    // Get GPS location
    window.getLocation = () => {
        if (!navigator.geolocation) {
            alert("Geolocation not supported");
            return;
        }
        navigator.geolocation.getCurrentPosition(
            (position) => {
                latitudeInput.value = position.coords.latitude;
                longitudeInput.value = position.coords.longitude;
                alert("Location captured!");
            },
            (err) => {
                alert("Could not get location");
                console.error(err);
            }
        );
    };

    // Submit form
    const signupForm = document.getElementById('signupForm');
    const signupBtn = document.querySelector('.signup-btn');

    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        signupBtn.disabled = true;
        signupBtn.textContent = 'Creating Account...';

        const role = document.querySelector('input[name="role"]:checked')?.value;
        const data = {
            fullName: document.getElementById('fullName').value,
            email: document.getElementById('email').value,
            phoneNumber: phoneInput.value,
            password: passwordInput.value,
            role: role
        };

        if (role === 'ROLE_ARTISAN') {
            const lat = latitudeInput.value;
            const lon = longitudeInput.value;
            if (!lat || !lon) {
                alert("Location is required for artisans");
                signupBtn.disabled = false;
                signupBtn.textContent = 'Create Account';
                return;
            }
            data.latitude = parseFloat(lat);
            data.longitude = parseFloat(lon);
        }

        try {
            const response = await fetch('/api/auth/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                alert('Account created! Please log in.');
                window.location.href = '/login';
            } else {
                const err = await response.text();
                throw new Error(err);
            }
        } catch (err) {
            alert(err.message || "Signup failed");
            signupBtn.disabled = false;
            signupBtn.textContent = 'Create Account';
        }
    });

    // Background slideshow
    const images = [
        'images/cleaning.avif',
        'images/floorer.avif',
        'images/plumber.avif',
        'images/demenagement.avif'
    ];
    const headerBackground = document.getElementById('headerBackground');
    let index = 0;

    images.forEach((src, i) => {
        const div = document.createElement('div');
        div.className = 'background-image' + (i === 0 ? ' active' : '');
        div.style.backgroundImage = `url(${src})`;
        headerBackground.appendChild(div);
    });

    setInterval(() => {
        const imgs = headerBackground.getElementsByClassName('background-image');
        imgs[index].classList.remove('active');
        index = (index + 1) % images.length;
        imgs[index].classList.add('active');
    }, 4000);
});
