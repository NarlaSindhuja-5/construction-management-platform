// E2E Construction Management System - Common Application Logic
// Professional • Classic • Clean • Construction Industry Focused

document.addEventListener('DOMContentLoaded', () => {
    // 1. Mobile menu toggle binding for all headers
    const mobileToggle = document.querySelector('.mobile-nav-toggle');
    const navLinks = document.querySelector('.nav-links');
    if (mobileToggle && navLinks) {
        mobileToggle.addEventListener('click', () => {
            navLinks.classList.toggle('is-open');
        });
    }

    // 2. Global image fallback handler: graceful fallback on broken image paths
    document.querySelectorAll('img').forEach(img => {
        img.addEventListener('error', function () {
            if (!this.dataset.hasFallback) {
                this.dataset.hasFallback = 'true';
                this.style.opacity = '0.7';
                this.alt = 'Equipment image preview unavailable';
            }
        });
    });

    // 3. Close open modal on Escape key press
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const openModal = document.querySelector('.modal-overlay.active, .modal.active');
            if (openModal) {
                openModal.classList.remove('active');
            }
        }
    });

    // 4. Global error listener for unhandled promise rejections
    window.addEventListener('unhandledrejection', (event) => {
        console.warn('Unhandled promise rejection:', event.reason);
    });
});
