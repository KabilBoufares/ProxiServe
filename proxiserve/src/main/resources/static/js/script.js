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

    // Close search on outside click or Escape
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
    const scrollContainer = document.querySelector('.icon-scroll .icon-wrapper');
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
    const query = document.getElementById('searchInput').value.trim();
    const location = document.getElementById('locationInput').value.trim();
  
    const url = new URL(window.location.origin + '/services.html');
    if (query) url.searchParams.append('query', query);
  
    if (location === '') {
      // 🛰️ Utilisation du GPS
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;
          url.searchParams.append('latitude', lat);
          url.searchParams.append('longitude', lon);
          window.location.href = url.toString();
        }, function () {
          console.warn("Location access denied.");
        });
      } else {
        console.warn("Geolocation not supported.");
      }
    } else {
      // 🌍 Géocodage via backend
      fetch(`/api/geocode?city=${encodeURIComponent(location)}`)
        .then(res => res.json())
        .then(data => {
          if (data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lon = parseFloat(data[0].lon);
            url.searchParams.append('location', location);
            url.searchParams.append('latitude', lat);
            url.searchParams.append('longitude', lon);
          } else {
            console.warn("City not found.");
          }
        })
        .then(() => {
          window.location.href = url.toString();
        });
    }
  });
  
