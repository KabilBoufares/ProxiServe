document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('newPasswordForm');
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    const submitBtn = form.querySelector('.reset-btn');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (newPassword.value !== confirmPassword.value) {
            alert("Passwords do not match.");
            return;
        }

        const token = document.body.dataset.token;


        if (!token) {
            alert("Invalid or missing token.");
            return;
        }

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = "Saving...";

            const response = await fetch('/api/auth/reset-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    token: token,
                    newPassword: newPassword.value
                })
            });

            if (response.ok) {
                alert("✅ Your password has been successfully updated.");
                window.location.href = "/login";
            } else {
                const error = await response.text();
                alert("❌ Error: " + error);
                submitBtn.disabled = false;
                submitBtn.textContent = "Save New Password";
            }

        } catch (err) {
            alert("Unexpected error. Try again.");
            submitBtn.disabled = false;
            submitBtn.textContent = "Save New Password";
        }
    });

    // Background animation
    const bgImages = ['images/plumber.avif', 'images/floorer.avif', 'images/cleaning.avif'];
    const bg = document.getElementById('headerBackground');
    let index = 0;

    bgImages.forEach((url, i) => {
        const div = document.createElement('div');
        div.className = `background-image ${i === 0 ? 'active' : ''}`;
        div.style.backgroundImage = `url(${url})`;
        bg.appendChild(div);
    });

    setInterval(() => {
        const imgs = bg.getElementsByClassName('background-image');
        imgs[index].classList.remove('active');
        index = (index + 1) % imgs.length;
        imgs[index].classList.add('active');
    }, 4000);
});
