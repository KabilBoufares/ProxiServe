// Material Design Animation Constants
const RIPPLE_DURATION = 600;
const ELEVATION_TRANSITION = 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
const SCALE_TRANSITION = 'transform 0.2s cubic-bezier(0.4, 0, 0.2, 1)';
const STAGGER_DELAY = 100; // Delay between staggered animations

// Material Design Ripple Effect
function createRipple(event) {
    const button = event.currentTarget;
    const ripple = document.createElement('span');
    const rect = button.getBoundingClientRect();
    
    const diameter = Math.max(rect.width, rect.height);
    const radius = diameter / 2;
    
    ripple.style.width = ripple.style.height = `${diameter}px`;
    ripple.style.left = `${event.clientX - rect.left - radius}px`;
    ripple.style.top = `${event.clientY - rect.top - radius}px`;
    ripple.className = 'ripple';
    
    // Remove existing ripple
    const existingRipple = button.querySelector('.ripple');
    if (existingRipple) {
        existingRipple.remove();
    }
    
    button.appendChild(ripple);
    
    // Remove ripple after animation
    setTimeout(() => ripple.remove(), RIPPLE_DURATION);
}

// Add Material elevation on hover with improved animation
function addElevation(element, restingElevation = 1, hoverElevation = 4) {
    element.style.transition = ELEVATION_TRANSITION;
    element.style.boxShadow = 'var(--shadow)';
    
    element.addEventListener('mouseenter', () => {
        element.style.boxShadow = 'var(--shadow)';
        // Add subtle scale effect
        element.style.transform = 'scale(1.01)';
    });
    
    element.addEventListener('mouseleave', () => {
        element.style.boxShadow = 'var(--shadow)';
        element.style.transform = 'scale(1)';
    });
}

// Add scale animation on click
function addScaleAnimation(element) {
    element.style.transition = SCALE_TRANSITION;
    
    element.addEventListener('mousedown', () => {
        element.style.transform = 'scale(0.95)';
    });
    
    element.addEventListener('mouseup', () => {
        element.style.transform = 'scale(1)';
    });
    
    element.addEventListener('mouseleave', () => {
        element.style.transform = 'scale(1)';
    });
}

// Add staggered fade-in animation
function addStaggeredFadeIn(elements, delay = STAGGER_DELAY) {
    elements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';
        element.style.transition = `opacity 0.6s ease, transform 0.6s ease`;
        
        setTimeout(() => {
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * delay);
    });
}

// Add subtle hover effect to certification items
function addCertificationAnimation(certItems) {
    certItems.forEach(item => {
        item.addEventListener('mouseenter', () => {
            item.style.transform = 'translateX(5px)';
            const icon = item.querySelector('i');
            if (icon) {
                icon.style.transform = 'rotate(10deg) scale(1.1)';
            }
        });
        
        item.addEventListener('mouseleave', () => {
            item.style.transform = 'translateX(0)';
            const icon = item.querySelector('i');
            if (icon) {
                icon.style.transform = 'rotate(0) scale(1)';
            }
        });
    });
}

// Add price card hover effect
function addPriceCardAnimation(priceCards) {
    priceCards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transform = 'translateY(-10px)';
            card.style.boxShadow = 'var(--shadow)';
            
            const price = card.querySelector('.price');
            if (price) {
                price.style.color = 'var(--accent)';
            }
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'translateY(0)';
            card.style.boxShadow = 'var(--shadow)';
            
            const price = card.querySelector('.price');
            if (price) {
                price.style.color = 'var(--primary)';
            }
        });
    });
}

// Add star rating animation
function addStarAnimation(starsContainers) {
    starsContainers.forEach(container => {
        const stars = container.querySelectorAll('i');
        
        stars.forEach((star, index) => {
            star.addEventListener('mouseenter', () => {
                // Animate stars up to current one
                for (let i = 0; i <= index; i++) {
                    stars[i].style.transform = 'scale(1.2)';
                    stars[i].style.color = 'var(--secondary)'; // Use secondary color variable
                }
            });
            
            star.addEventListener('mouseleave', () => {
                // Reset all stars
                stars.forEach(s => {
                    s.style.transform = 'scale(1)';
                    s.style.color = '';
                });
            });
        });
    });
}

// Scroll animation with Intersection Observer
function initScrollAnimations() {
    const sections = [
        ...document.querySelectorAll('.card'),
        ...document.querySelectorAll('.action-buttons')
    ];
    
    const observer = new IntersectionObserver(
        (entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animated');
                    observer.unobserve(entry.target);
                }
            });
        },
        { threshold: 0.1, rootMargin: '0px 0px -50px 0px' }
    );
    
    sections.forEach(section => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(30px)';
        observer.observe(section);
    });
}

// Initialize Material Design animations
document.addEventListener('DOMContentLoaded', () => {
    // Add ripple effect to buttons
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', createRipple);
        addScaleAnimation(button);
    });
    
    // Add elevation to cards
    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        addElevation(card);
    });
    
    // Add staggered animation to specific elements
    const skillsItems = document.querySelectorAll('.skills-list li');
    addStaggeredFadeIn(skillsItems, 100);
    
    // Add certification animation effects
    const certItems = document.querySelectorAll('.cert-item');
    certItems.forEach(item => {
        item.style.transition = 'transform 0.3s ease';
    });
    addCertificationAnimation(certItems);
    
    // Add smooth reveal animation to portfolio items
    const portfolioItems = document.querySelectorAll('.portfolio-item');
    const portfolioObserver = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0) scale(1)';
                    }, index * 100);
                }
            });
        },
        { threshold: 0.1 }
    );
    
    portfolioItems.forEach(item => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px) scale(0.95)';
        item.style.transition = 'opacity 0.6s cubic-bezier(0.4, 0, 0.2, 1), transform 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
        portfolioObserver.observe(item);
    });
    
    // Add price card animations
    const priceCards = document.querySelectorAll('.price-card');
    priceCards.forEach(card => {
        card.style.transition = 'transform 0.3s ease, box-shadow 0.3s ease';
    });
    addPriceCardAnimation(priceCards);
    
    // Add star rating animations
    const starsContainers = document.querySelectorAll('.stars');
    starsContainers.forEach(container => {
        const stars = container.querySelectorAll('i');
        stars.forEach(star => {
            star.style.transition = 'transform 0.2s ease, color 0.2s ease';
        });
    });
    addStarAnimation(starsContainers);
    
    // Initialize scroll animations
    initScrollAnimations();
});

// Add this CSS to the head
const style = document.createElement('style');
style.textContent = `
    .ripple {
        position: absolute;
        border-radius: 50%;
        transform: scale(0);
        animation: ripple ${RIPPLE_DURATION}ms linear;
        background-color: rgba(250, 243, 221, 0.7); /* faf3dd with opacity */
        pointer-events: none;
    }
    
    @keyframes ripple {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .btn {
        position: relative;
        overflow: hidden;
    }
    
    .card {
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), 
                    opacity 0.4s cubic-bezier(0.4, 0, 0.2, 1), 
                    box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        will-change: transform, opacity, box-shadow;
    }
    
    .portfolio-item {
        will-change: transform, opacity;
    }
    
    .cert-item i {
        transition: transform 0.3s ease;
    }
    
    .animated {
        opacity: 1 !important;
        transform: translateY(0) !important;
        transition: opacity 0.6s ease-out, transform 0.6s ease-out;
    }
    
    /* Pulse animation for badges */
    @keyframes pulse {
        0% { transform: scale(1); }
        50% { transform: scale(1.05); }
        100% { transform: scale(1); }
    }
    
    .cert-title::after {
        content: "";
        display: inline-block;
        width: 8px;
        height: 8px;
        background-color: var(--secondary);
        border-radius: 50%;
        margin-left: 8px;
        animation: pulse 2s infinite;
    }
    
    /* Smooth transition for all interactive elements */
    a, button, .social-link, .portfolio-item, .price-card, .cert-item, .skills-list li {
        transition: all 0.3s ease;
    }
`;
document.head.appendChild(style);