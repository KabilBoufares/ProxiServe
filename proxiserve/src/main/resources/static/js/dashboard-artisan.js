document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("No token found. Please login.");
        return;
    }

    const payloadBase64 = token.split('.')[1];
    const payloadDecoded = JSON.parse(atob(payloadBase64));
    const userId = payloadDecoded.userId || payloadDecoded.id || payloadDecoded.sub;

    if (!userId) {
        console.error("User ID not found in token.");
        return;
    }

    // Fetch artisan by userId
    fetch(`/api/artisans/user/${userId}`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
    .then(res => res.json())
    .then(artisan => {
        const artisanId = artisan.id;

        // Total services
        fetch(`/api/services/artisan/${artisanId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        .then(res => res.json())
        .then(services => {
            document.getElementById("total-services").textContent = services.length;
        });

        // Average rating
        fetch(`/api/reviews/stats/${artisanId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        .then(res => res.json())
        .then(stats => {
            document.getElementById("average-rating").textContent = stats.averageRating?.toFixed(1) || "0.0";
        });

        // Bookings
        fetch(`/api/bookings/artisan`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        .then(res => res.json())
        .then(bookings => {
            document.getElementById("total-bookings").textContent = bookings.length;
            const tableBody = document.getElementById("bookings-body");
            tableBody.innerHTML = "";

            bookings.forEach(booking => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${booking.bookingDate?.split("T")[0]}</td>
                    <td><strong>${booking.clientFullName}</strong></td>
                    <td>${booking.serviceTitle}</td>
                    <td><span class="badge bg-info">${booking.status}</span></td>
                    <td><button class="btn btn-sm btn-outline-primary">Details</button></td>
                `;
                tableBody.appendChild(row);
            });
        });
    })
    .catch(err => {
        console.error("Error fetching artisan data:", err);
    });
});
