document.addEventListener('DOMContentLoaded', function () {
    // 🔍 Search elements
    const searchButtons = document.querySelectorAll('.search-btn, .search-button');
    const searchPage = document.getElementById('searchPage');
    const mainSearchInput = document.getElementById('searchInput');
    const searchPageInput = document.querySelector('#searchPage input[type="text"]') || mainSearchInput;
    const suggestionsContainer = document.getElementById('suggestions');
    const workerCards = document.querySelectorAll('.worker-card');

    const suggestions = ['plumber', 'electrician', 'carpenter', 'painter', 'gardener', 'handyman', 'roofing', 'repair', 'bathroom'];

    // 🔍 Open search page and focus input
    searchButtons.forEach(button => {
        button.addEventListener('click', function () {
            searchPage?.classList.add('active');
            if (button.classList.contains('search-button')) {
                searchPageInput.value = mainSearchInput.value;
                performSearch(mainSearchInput.value);
            }
            searchPageInput.focus();
        });
    });

    // 🔍 Perform search
    function performSearch(searchTerm) {
        searchTerm = searchTerm.toLowerCase().trim();

        workerCards.forEach(card => {
            const workerName = card.querySelector('h3')?.textContent.toLowerCase();
            const profession = card.querySelector('.profession')?.textContent.toLowerCase();
            const location = card.querySelector('.location')?.textContent.toLowerCase();

            const matches = [workerName, profession, location].some(field => field?.includes(searchTerm));
            card.style.display = matches ? 'block' : 'none';
        });
    }

    // 🔁 Update suggestions dropdown
    function updateSuggestions(searchTerm) {
        if (searchTerm.length > 0) {
            const filteredSuggestions = suggestions.filter(s => s.includes(searchTerm.toLowerCase()));

            suggestionsContainer.innerHTML = filteredSuggestions.length
                ? filteredSuggestions.map(s => `<div class="suggestion-item">${s}</div>`).join('')
                : '';

            suggestionsContainer.style.display = filteredSuggestions.length ? 'block' : 'none';
        } else {
            suggestionsContainer.style.display = 'none';
        }
    }

    // 🎯 Handle input changes
    [mainSearchInput, searchPageInput].forEach(inputField => {
        inputField.addEventListener('input', function (e) {
            const searchTerm = e.target.value;
            mainSearchInput.value = searchTerm;
            searchPageInput.value = searchTerm;
            updateSuggestions(searchTerm);
            performSearch(searchTerm);
        });
    });

    // ✅ Handle suggestion click
    suggestionsContainer.addEventListener('click', (e) => {
        if (e.target.classList.contains('suggestion-item')) {
            const selectedSuggestion = e.target.textContent;
            mainSearchInput.value = selectedSuggestion;
            searchPageInput.value = selectedSuggestion;
            suggestionsContainer.style.display = 'none';
            performSearch(selectedSuggestion);
        }
    });

    // ❌ Close search on outside click or Escape
    document.addEventListener('click', (e) => {
        if (!searchPage?.contains(e.target) &&
            !Array.from(searchButtons).some(btn => btn.contains(e.target)) &&
            e.target !== mainSearchInput) {
            searchPage?.classList.remove('active');
            suggestionsContainer.style.display = 'none';
        }
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            searchPage?.classList.remove('active');
            suggestionsContainer.style.display = 'none';
        }
    });

    // ↔️ Scroll horizontal for icon buttons
    const scrollContainer = document.querySelector('.icon-scroll');
    const scrollLeftBtn = document.querySelector('.scroll-left');
    const scrollRightBtn = document.querySelector('.scroll-right');

    if (scrollContainer) {
        const scrollAmount = 200;

        scrollLeftBtn?.addEventListener('click', () => {
            scrollContainer.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
        });

        scrollRightBtn?.addEventListener('click', () => {
            scrollContainer.scrollBy({ left: scrollAmount, behavior: 'smooth' });
        });
    }

    // 🖱️ Icon click triggers search
    document.querySelectorAll('.icon-item').forEach(icon => {
        icon.addEventListener('click', () => {
            const service = icon.querySelector('span').textContent.trim().toLowerCase();
            mainSearchInput.value = service;
            searchPageInput.value = service;
            suggestionsContainer.style.display = 'none';
            performSearch(service);
            searchPage?.classList.add('active');
        });
    });

    // 🗂️ Scroll reviews left/right
    const reviewSlider = document.querySelector('.review-cards');
    const reviewLeftBtn = document.querySelector('.review-left');
    const reviewRightBtn = document.querySelector('.review-right');

    if (reviewSlider && reviewLeftBtn && reviewRightBtn) {
        const scrollStep = 300;

        reviewLeftBtn.addEventListener('click', () => {
            reviewSlider.scrollBy({ left: -scrollStep, behavior: 'smooth' });
        });

        reviewRightBtn.addEventListener('click', () => {
            reviewSlider.scrollBy({ left: scrollStep, behavior: 'smooth' });
        });
    }

    // ✅ Initial state: show all
    performSearch('');
});


document.getElementById('searchBtn').addEventListener('click', function () {
    const query = document.getElementById('searchInput').value;
    const locationInput = document.getElementById('locationInput').value;

    if (locationInput.trim() === '') {
        // 🛰️ Utilise GPS si aucun nom de ville
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function (position) {
                const lat = position.coords.latitude;
                const lon = position.coords.longitude;
                launchSearch(query, lat, lon);
            }, function (error) {
                alert("Please allow location access or enter a city.");
            });
        } else {
            alert("Geolocation not supported.");
        }
    } else {
        // 🌍 Géocodage avec OpenStreetMap (Nominatim)
        const city = encodeURIComponent(locationInput);
        fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${city}`)
            .then(res => res.json())
            .then(data => {
                if (data.length > 0) {
                    const lat = parseFloat(data[0].lat);
                    const lon = parseFloat(data[0].lon);
                    launchSearch(query, lat, lon);
                } else {
                    alert("City not found.");
                }
            })
            .catch(err => {
                console.error(err);
                alert("Error during geocoding.");
            });
    }
});

document.getElementById('searchBtn').addEventListener('click', function () {
    const query = document.getElementById('searchInput').value.trim();
    const locationInput = document.getElementById('locationInput').value.trim();
    const resultsContainer = document.getElementById('results');
    const latitudeInput = document.getElementById('latitude');
    const longitudeInput = document.getElementById('longitude');
  
    resultsContainer.innerHTML = '<p class="search-loading">Searching, please wait...</p>';
  
    if (locationInput === '') {
      // 🛰️ Utiliser le GPS si aucun nom de ville n'est saisi
      if ('geolocation' in navigator) {
        navigator.geolocation.getCurrentPosition(
          position => {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            latitudeInput.value = lat;
            longitudeInput.value = lon;
            launchSearch(query, lat, lon);
          },
          error => {
            resultsContainer.innerHTML = '<p style="color: red;">Location access denied. Please enter a city manually.</p>';
          },
          { enableHighAccuracy: true, timeout: 10000 }
        );
      } else {
        resultsContainer.innerHTML = '<p style="color: red;">Geolocation is not supported by your browser.</p>';
      }
    } else {
      // 🌍 Géocodage de la ville avec OpenStreetMap (Nominatim)
      const city = encodeURIComponent(locationInput);
      fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${city}`)
        .then(res => res.json())
        .then(data => {
          if (data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lon = parseFloat(data[0].lon);
            latitudeInput.value = lat;
            longitudeInput.value = lon;
            launchSearch(query, lat, lon);
          } else {
            resultsContainer.innerHTML = '<p style="color: red;">City not found. Please try again.</p>';
          }
        })
        .catch(err => {
          console.error(err);
          resultsContainer.innerHTML = '<p style="color: red;">Error during geolocation. Try again.</p>';
        });
    }
  });
  
  function launchSearch(query, lat, lon, fallback = false) {
    const url = `/api/services/search/advanced?query=${encodeURIComponent(query)}&latitude=${lat}&longitude=${lon}&radiusKm=10${fallback ? '&disableDistanceCheck=true' : ''}`;
    const container = document.getElementById('results');
  
    fetch(url)
      .then(res => res.json())
      .then(data => {
        container.innerHTML = ''; // clear loading
  
        if (data.length === 0 && !fallback) {
          // 🔁 Essai avec disableDistanceCheck
          console.warn('No results, retrying with disableDistanceCheck=true');
          launchSearch(query, lat, lon, true);
          return;
        }
  
        if (data.length === 0) {
          container.innerHTML = '<p style="color: var(--beige);">No results found.</p>';
          return;
        }
  
        data.forEach(service => {
          const div = document.createElement('div');
          div.className = 'service-result-card';
          div.innerHTML = `
            <h3>${service.title}</h3>
            <p>${service.description}</p>
            <p><strong>Price:</strong> ${service.price} DT</p>
            <p><strong>Distance:</strong> ${service.distanceKm.toFixed(2)} km</p>
            <p><strong>Rating:</strong> ${service.rating?.toFixed(1) ?? 'N/A'} ⭐</p>
          `;
          container.appendChild(div);
        });
      })
      .catch(err => {
        console.error(err);
        container.innerHTML = '<p style="color: red;">Search failed. Please try again.</p>';
      });
  }
  
  
