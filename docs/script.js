(() => {
  const sidebar   = document.getElementById('sidebar');
  const backdrop  = document.getElementById('backdrop');
  const menuBtn   = document.getElementById('menuBtn');
  const closeBtn  = document.getElementById('sidebarClose');
  const navLinks  = document.querySelectorAll('.nav-link');
  const sections  = document.querySelectorAll('.doc-section, .hero');

  function openSidebar() {
    sidebar.classList.add('open');
    backdrop.classList.add('visible');
    document.body.style.overflow = 'hidden';
  }

  function closeSidebar() {
    sidebar.classList.remove('open');
    backdrop.classList.remove('visible');
    document.body.style.overflow = '';
  }

  menuBtn.addEventListener('click', openSidebar);
  closeBtn.addEventListener('click', closeSidebar);
  backdrop.addEventListener('click', closeSidebar);

  navLinks.forEach(link => {
    link.addEventListener('click', () => {
      if (window.innerWidth <= 768) closeSidebar();
    });
  });

  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;
      const id = entry.target.id || 'introduction';
      navLinks.forEach(link => {
        const href = link.getAttribute('href').replace('#', '');
        link.classList.toggle('active', href === id);
      });
    });
  }, { rootMargin: '-20% 0px -70% 0px' });

  sections.forEach(section => observer.observe(section));
})();
