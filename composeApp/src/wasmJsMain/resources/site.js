(() => {
  if ('serviceWorker' in navigator && (location.protocol === 'https:' || location.hostname === 'localhost' || location.hostname === '127.0.0.1')) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/service-worker.js').catch(() => {});
    }, { once: true });
  }

  const header = document.querySelector('[data-header]');
  const menuButton = document.querySelector('[data-menu-button]');
  const menu = document.querySelector('[data-menu]');

  const updateHeader = () => header?.classList.toggle('scrolled', window.scrollY > 18);
  updateHeader();
  window.addEventListener('scroll', updateHeader, { passive: true });

  menuButton?.addEventListener('click', () => {
    const open = menu?.classList.toggle('open') ?? false;
    menuButton.setAttribute('aria-expanded', String(open));
  });

  menu?.addEventListener('click', event => {
    if (event.target instanceof HTMLAnchorElement) {
      menu.classList.remove('open');
      menuButton?.setAttribute('aria-expanded', 'false');
    }
  });

  document.querySelectorAll('[data-year]').forEach(node => {
    node.textContent = String(new Date().getFullYear());
  });

  const planner = document.querySelector('[data-prep-planner]');
  if (planner) {
    const storageKey = 'anlik-depremler-preparedness-v1';
    const checks = [...planner.querySelectorAll('[data-kit-check]')];
    const progress = planner.querySelector('[data-kit-progress]');
    const progressBar = planner.querySelector('[data-kit-progress-bar]');
    const kitStatus = planner.querySelector('[data-kit-status]');
    const form = planner.querySelector('[data-family-form]');
    const familyStatus = planner.querySelector('[data-family-status]');
    const fields = form ? [...form.querySelectorAll('input[name]')] : [];
    let saved = { checks: {}, family: {} };

    try {
      const raw = window.localStorage.getItem(storageKey);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed && typeof parsed === 'object') saved = {
          checks: parsed.checks && typeof parsed.checks === 'object' ? parsed.checks : {},
          family: parsed.family && typeof parsed.family === 'object' ? parsed.family : {}
        };
      }
    } catch {
      if (kitStatus) kitStatus.textContent = 'Tarayıcı depolaması kapalı; bu oturumdaki işaretler kaydedilemeyebilir.';
    }

    const persist = () => {
      try {
        window.localStorage.setItem(storageKey, JSON.stringify(saved));
        return true;
      } catch {
        return false;
      }
    };
    const updateProgress = () => {
      const completed = checks.filter(input => input.checked).length;
      if (progress) progress.textContent = `${completed} / ${checks.length}`;
      if (progressBar) progressBar.style.width = `${checks.length ? completed / checks.length * 100 : 0}%`;
    };

    checks.forEach(input => {
      input.checked = saved.checks[input.dataset.kitCheck] === true;
      input.addEventListener('change', () => {
        saved.checks[input.dataset.kitCheck] = input.checked;
        const stored = persist();
        updateProgress();
        if (kitStatus) kitStatus.textContent = stored ? 'İşaretlerin bu cihazda saklandı.' : 'Tarayıcı depolama alanına yazılamadı; işaretler kalıcı olmayabilir.';
      });
    });
    updateProgress();

    fields.forEach(field => {
      const value = saved.family[field.name];
      if (typeof value === 'string') field.value = value;
    });
    if (Object.values(saved.family).some(value => typeof value === 'string' && value.length > 0) && familyStatus) {
      familyStatus.textContent = 'Bu cihazda kayıtlı plan yüklendi.';
    }

    form?.addEventListener('submit', event => {
      event.preventDefault();
      saved.family = Object.fromEntries(fields.map(field => [field.name, field.value.trim()]));
      const stored = persist();
      if (familyStatus) familyStatus.textContent = stored ? 'Aile planın yalnızca bu cihazda saklandı.' : 'Kayıt yapılamadı. Tarayıcı depolama ayarlarını kontrol et.';
    });
    planner.querySelector('[data-print-plan]')?.addEventListener('click', () => window.print());
  }

  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const revealItems = document.querySelectorAll('.reveal');
  if (reduceMotion || !('IntersectionObserver' in window)) {
    revealItems.forEach(item => item.classList.add('is-visible'));
  } else {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          observer.unobserve(entry.target);
        }
      });
    }, { rootMargin: '0px 0px -8% 0px', threshold: 0.08 });
    revealItems.forEach(item => observer.observe(item));
  }
})();
