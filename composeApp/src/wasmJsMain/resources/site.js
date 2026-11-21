(() => {
  const earthquakeMaps = new Map();
  let mapLibreImport;
  const loadMapLibre = () => {
    mapLibreImport ??= import('https://unpkg.com/maplibre-gl@6.19.0/dist/maplibre-gl.mjs')
      .catch(error => {
        mapLibreImport = null;
        throw error;
      });
    return mapLibreImport;
  };

  const mapError = host => {
    host.classList.remove('map-loading');
    host.classList.add('map-unavailable');
    host.setAttribute('aria-busy', 'false');
    host.dataset.mapError = 'Harita yüklenemedi. Deprem listesini kullanmaya devam edebilirsiniz.';
  };

  window.EarthquakeMapRuntime = {
    mount(host, id, geoJson) {
      this.unmount(id);
      const record = { host, id, data: JSON.parse(geoJson), map: null, mounted: true };
      earthquakeMaps.set(id, record);
      host.classList.add('map-loading');
      host.setAttribute('aria-busy', 'true');
      loadMapLibre().then(maplibre => {
        if (!record.mounted || !host.isConnected) return;
        const map = new maplibre.Map({
          container: host,
          style: 'https://tiles.openfreemap.org/styles/liberty',
          center: [35, 39],
          zoom: 4.5,
          attributionControl: true,
          cooperativeGestures: true
        });
        record.map = map;
        map.on('error', () => {
          if (!record.mapLoaded && record.mounted) mapError(host);
        });
        map.addControl(new maplibre.NavigationControl({ showCompass: false }), 'top-right');
        map.addControl(new maplibre.ScaleControl({ maxWidth: 100, unit: 'metric' }), 'bottom-left');

        map.once('load', () => {
          if (!record.mounted) return;
          record.mapLoaded = true;
          host.classList.remove('map-loading', 'map-unavailable');
          host.setAttribute('aria-busy', 'false');
          map.addSource('earthquake-events', {
            type: 'geojson',
            data: record.data,
            cluster: true,
            clusterRadius: 48,
            clusterMaxZoom: 8
          });
          map.addLayer({
            id: 'earthquake-clusters',
            type: 'circle',
            source: 'earthquake-events',
            filter: ['has', 'point_count'],
            paint: {
              'circle-color': ['step', ['get', 'point_count'], '#12b8a6', 10, '#f4b740', 40, '#ef7657', 100, '#df4f69'],
              'circle-radius': ['step', ['get', 'point_count'], 17, 10, 22, 40, 27, 100, 32],
              'circle-stroke-color': '#f4fafc',
              'circle-stroke-width': 1.5,
              'circle-opacity': 0.92
            }
          });
          map.addLayer({
            id: 'earthquake-cluster-count',
            type: 'symbol',
            source: 'earthquake-events',
            filter: ['has', 'point_count'],
            layout: { 'text-field': ['get', 'point_count_abbreviated'], 'text-font': ['Open Sans Bold'], 'text-size': 13 },
            paint: { 'text-color': '#09202c' }
          });
          map.addLayer({
            id: 'earthquake-points',
            type: 'circle',
            source: 'earthquake-events',
            filter: ['!', ['has', 'point_count']],
            paint: {
              'circle-radius': ['interpolate', ['linear'], ['get', 'magnitude'], 0, 5, 3, 7, 5, 11, 7, 16],
              'circle-color': ['step', ['get', 'magnitude'], '#32c9b5', 3, '#f3c251', 4.5, '#f48a5e', 6, '#ed536a'],
              'circle-stroke-color': '#f8fbfc',
              'circle-stroke-width': 1.5,
              'circle-opacity': 0.94
            }
          });

          const features = record.data.features || [];
          if (features.length === 1) {
            const [longitude, latitude] = features[0].geometry.coordinates;
            map.easeTo({ center: [longitude, latitude], zoom: 6, duration: 0 });
          } else if (features.length > 1) {
            const longitudes = features.map(feature => feature.geometry.coordinates[0]);
            const latitudes = features.map(feature => feature.geometry.coordinates[1]);
            map.fitBounds([
              [Math.min(...longitudes), Math.min(...latitudes)],
              [Math.max(...longitudes), Math.max(...latitudes)]
            ], { padding: 40, maxZoom: 6, duration: 0 });
          }

          map.on('click', 'earthquake-clusters', event => {
            const feature = map.queryRenderedFeatures(event.point, { layers: ['earthquake-clusters'] })[0];
            const clusterId = feature?.properties?.cluster_id;
            const source = map.getSource('earthquake-events');
            if (clusterId == null || !source?.getClusterExpansionZoom) return;
            source.getClusterExpansionZoom(clusterId, (error, zoom) => {
              if (!error && Number.isFinite(zoom)) map.easeTo({ center: feature.geometry.coordinates, zoom });
            });
          });
          map.on('click', 'earthquake-points', event => {
            const earthquakeId = event.features?.[0]?.properties?.id;
            if (earthquakeId) host.dispatchEvent(new CustomEvent('quake-marker-select', { detail: earthquakeId }));
          });
          for (const layer of ['earthquake-clusters', 'earthquake-points']) {
            map.on('mouseenter', layer, () => { map.getCanvas().style.cursor = 'pointer'; });
            map.on('mouseleave', layer, () => { map.getCanvas().style.cursor = ''; });
          }
        });
      }).catch(() => mapError(host));
    },

    update(id, geoJson) {
      const record = earthquakeMaps.get(id);
      if (!record) return;
      try {
        record.data = JSON.parse(geoJson);
        record.map?.getSource('earthquake-events')?.setData(record.data);
      } catch {
        mapError(record.host);
      }
    },

    position(id, left, top, width, height) {
      const record = earthquakeMaps.get(id);
      if (!record) return;
      Object.assign(record.host.style, { left, top, width, height });
      record.map?.resize();
    },

    unmount(id) {
      const record = earthquakeMaps.get(id);
      if (!record) return;
      record.mounted = false;
      record.map?.remove();
      earthquakeMaps.delete(id);
    }
  };

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
