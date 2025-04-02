document.addEventListener('DOMContentLoaded', function() {
    // Search elements
    const searchButtons = document.querySelectorAll('.search-btn, .search-button');
    const searchPage = document.getElementById('searchPage');
    const mainSearchInput = document.getElementById('searchInput');
    const searchPageInput = document.querySelector('#searchPage input[type="text"]');
    const suggestionsContainer = document.getElementById('suggestions');
    const workerCards = document.querySelectorAll('.worker-card');
    
    // Sample suggestions
    const suggestions = ['plumber', 'electrician', 'carpenter', 'painter', 'gardener', 'handyman', 'roofing', 'repair', 'bathroom'];
  
    // Open search page and focus input
    searchButtons.forEach(button => {
        button.addEventListener('click', function() {
            searchPage.classList.add('active');
            if (button.classList.contains('search-button')) {
                // If it's the main search button, transfer the input value
                searchPageInput.value = mainSearchInput.value;
                performSearch(mainSearchInput.value);
            }
            searchPageInput.focus();
        });
    });
  
    // Main search function
    function performSearch(searchTerm) {
        searchTerm = searchTerm.toLowerCase();
        
        workerCards.forEach(card => {
            const workerName = card.querySelector('h3')?.textContent.toLowerCase();
            const profession = card.querySelector('.profession')?.textContent.toLowerCase();
            const location = card.querySelector('.location')?.textContent.toLowerCase();
            
            if (workerName && profession && location) {
                card.style.display = (workerName.includes(searchTerm) || 
                    profession.includes(searchTerm) || 
                    location.includes(searchTerm))
                    ? 'block'
                    : 'none';
            }
        });
    }
  
    // Input event for main search
    mainSearchInput.addEventListener('input', function(e) {
        const searchTerm = e.target.value;
        updateSuggestions(searchTerm);
        performSearch(searchTerm);
    });
  
    // Input event for search page input
    searchPageInput.addEventListener('input', function(e) {
        const searchTerm = e.target.value;
        mainSearchInput.value = searchTerm; // Sync with main search
        updateSuggestions(searchTerm);
        performSearch(searchTerm);
    });
  
    // Update suggestions dropdown
    function updateSuggestions(searchTerm) {
        if (searchTerm.length > 0) {
            const filteredSuggestions = suggestions.filter(suggestion => 
                suggestion.toLowerCase().includes(searchTerm.toLowerCase())
            );
            
            if (filteredSuggestions.length > 0) {
                suggestionsContainer.innerHTML = filteredSuggestions
                    .map(suggestion => `<div class="suggestion-item">${suggestion}</div>`)
                    .join('');
                suggestionsContainer.style.display = 'block';
            } else {
                suggestionsContainer.style.display = 'none';
            }
        } else {
            suggestionsContainer.style.display = 'none';
        }
    }
  
    // Handle suggestion clicks
    suggestionsContainer.addEventListener('click', (e) => {
        if (e.target.classList.contains('suggestion-item')) {
            const selectedSuggestion = e.target.textContent;
            mainSearchInput.value = selectedSuggestion;
            searchPageInput.value = selectedSuggestion;
            suggestionsContainer.style.display = 'none';
            performSearch(selectedSuggestion);
        }
    });
  
    // Close search page when clicking outside or pressing Escape
    document.addEventListener('click', (e) => {
        if (!searchPage.contains(e.target) && 
            !Array.from(searchButtons).some(btn => btn.contains(e.target)) &&
            e.target !== mainSearchInput) {
            searchPage.classList.remove('active');
            suggestionsContainer.style.display = 'none';
        }
    });
  
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            searchPage.classList.remove('active');
            suggestionsContainer.style.display = 'none';
        }
    });
  
    // Horizontal scroll functionality
    const scrollContainer = document.querySelector('.icon-scroll');
    if (scrollContainer) {
        const scrollContent = document.querySelector('.icon-wrapper');
        const scrollLeftBtn = document.querySelector('.scroll-left');
        const scrollRightBtn = document.querySelector('.scroll-right');
        
        let scrollPosition = 0;
        const scrollAmount = 200;
  
        if (scrollLeftBtn) {
            scrollLeftBtn.addEventListener('click', () => {
                scrollPosition = Math.max(scrollPosition - scrollAmount, 0);
                scrollContent.style.transform = `translateX(-${scrollPosition}px)`;
            });
        }
  
        if (scrollRightBtn) {
            scrollRightBtn.addEventListener('click', () => {
                const maxScroll = scrollContent.scrollWidth - scrollContainer.clientWidth;
                scrollPosition = Math.min(scrollPosition + scrollAmount, maxScroll);
                scrollContent.style.transform = `translateX(-${scrollPosition}px)`;
            });
        }
  
        // Handle icon clicks
        document.querySelectorAll('.icon-item').forEach(icon => {
            icon.addEventListener('click', () => {
                const service = icon.querySelector('span').textContent.toLowerCase();
                mainSearchInput.value = service;
                searchPageInput.value = service;
                suggestionsContainer.style.display = 'none';
                performSearch(service);
                searchPage.classList.add('active');
            });
        });
    }
  
    // Initialize with empty search to show all workers
    performSearch('');
  });