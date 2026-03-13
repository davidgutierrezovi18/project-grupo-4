(function () {
  const mapElement = document.getElementById("reviewsMap");
  if (!mapElement || typeof L === "undefined") {
    return;
  }

  const searchInput = document.getElementById("searchInput");
  const searchButton = document.getElementById("searchBtn");
  const searchStatus = document.getElementById("searchStatus");
  const resultsList = document.getElementById("resultsList");
  const resultsSummary = document.getElementById("resultsSummary");

  const defaultCenter = [40.4168, -3.7038];
  const defaultZoom = 6;
  const maxResults = 12;
  const locationStopWords = new Set(["de", "del", "la", "las", "el", "los", "en", "y"]);
  const strongTokenMatchThreshold = 0.82;

  const map = L.map(mapElement, {
    scrollWheelZoom: true,
    minZoom: 4,
  }).setView(defaultCenter, defaultZoom);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  }).addTo(map);

  const markersLayer = L.layerGroup().addTo(map);
  let activeMarkers = [];
  let activePopupMarker = null;
  let activeRequestController = null;

  function normalizeText(value) {
    return String(value || "")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLowerCase()
      .replace(/[^a-z0-9\s]/g, " ")
      .replace(/\s+/g, " ")
      .trim();
  }

  function tokenize(value) {
    return normalizeText(value).split(" ").filter(Boolean);
  }

  function escapeHtml(value) {
    return String(value || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function levenshteinDistance(left, right) {
    if (left === right) {
      return 0;
    }

    if (!left.length) {
      return right.length;
    }

    if (!right.length) {
      return left.length;
    }

    const previous = new Array(right.length + 1);
    const current = new Array(right.length + 1);

    for (let column = 0; column <= right.length; column += 1) {
      previous[column] = column;
    }

    for (let row = 1; row <= left.length; row += 1) {
      current[0] = row;

      for (let column = 1; column <= right.length; column += 1) {
        const substitutionCost = left[row - 1] === right[column - 1] ? 0 : 1;
        current[column] = Math.min(
          current[column - 1] + 1,
          previous[column] + 1,
          previous[column - 1] + substitutionCost
        );
      }

      for (let column = 0; column <= right.length; column += 1) {
        previous[column] = current[column];
      }
    }

    return previous[right.length];
  }

  function similarityScore(left, right) {
    const normalizedLeft = normalizeText(left);
    const normalizedRight = normalizeText(right);

    if (!normalizedLeft || !normalizedRight) {
      return 0;
    }

    if (normalizedLeft === normalizedRight) {
      return 1;
    }

    if (normalizedLeft.includes(normalizedRight) || normalizedRight.includes(normalizedLeft)) {
      const shortest = Math.min(normalizedLeft.length, normalizedRight.length);
      const longest = Math.max(normalizedLeft.length, normalizedRight.length);
      return 0.88 + shortest / (longest * 10);
    }

    const distance = levenshteinDistance(normalizedLeft, normalizedRight);
    return Math.max(0, 1 - distance / Math.max(normalizedLeft.length, normalizedRight.length));
  }

  function phraseScore(query, candidate) {
    const normalizedQuery = normalizeText(query);
    const normalizedCandidate = normalizeText(candidate);

    if (!normalizedQuery || !normalizedCandidate) {
      return 0;
    }

    if (normalizedCandidate.includes(normalizedQuery)) {
      return 1;
    }

    const queryTokens = tokenize(normalizedQuery);
    const candidateTokens = tokenize(normalizedCandidate);
    if (!queryTokens.length || !candidateTokens.length) {
      return 0;
    }

    const tokenAverage = queryTokens.reduce((sum, token) => {
      let bestTokenScore = similarityScore(token, normalizedCandidate);

      for (const candidateToken of candidateTokens) {
        if (candidateToken.startsWith(token) || candidateToken.includes(token)) {
          bestTokenScore = Math.max(bestTokenScore, 0.95);
          continue;
        }

        bestTokenScore = Math.max(bestTokenScore, similarityScore(token, candidateToken));
      }

      return sum + bestTokenScore;
    }, 0) / queryTokens.length;

    return tokenAverage;
  }

  function isStrictTokenMatch(queryToken, candidateToken) {
    if (!queryToken || !candidateToken) {
      return false;
    }

    if (queryToken === candidateToken) {
      return true;
    }

    if (queryToken.length >= 4 && candidateToken.startsWith(queryToken)) {
      return true;
    }

    if (candidateToken.length >= 4 && queryToken.startsWith(candidateToken) && (queryToken.length - candidateToken.length) <= 1) {
      return true;
    }

    const distance = levenshteinDistance(queryToken, candidateToken);
    return distance <= 1 && Math.abs(queryToken.length - candidateToken.length) <= 1;
  }

  function getMeaningfulTokens(value) {
    return tokenize(value).filter((token) => token.length > 1 && !locationStopWords.has(token));
  }

  function getNameMatchMetrics(query, candidate) {
    const queryTokens = getMeaningfulTokens(query);
    const candidateTokens = getMeaningfulTokens(candidate);
    const normalizedCandidate = normalizeText(candidate);

    if (!queryTokens.length || !candidateTokens.length) {
      return {
        average: 0,
        best: 0,
        coverage: 0,
      };
    }

    let total = 0;
    let best = 0;
    let covered = 0;

    queryTokens.forEach((queryToken) => {
      let bestTokenScore = similarityScore(queryToken, normalizedCandidate);

      candidateTokens.forEach((candidateToken) => {
        if (candidateToken.startsWith(queryToken) || candidateToken.includes(queryToken)) {
          bestTokenScore = Math.max(bestTokenScore, 0.97);
          return;
        }

        bestTokenScore = Math.max(bestTokenScore, similarityScore(queryToken, candidateToken));
      });

      if (bestTokenScore >= strongTokenMatchThreshold) {
        covered += 1;
      }

      best = Math.max(best, bestTokenScore);
      total += bestTokenScore;
    });

    return {
      average: total / queryTokens.length,
      best: best,
      coverage: covered / queryTokens.length,
    };
  }

  function getStrictNameMatchMetrics(query, candidates) {
    const queryTokens = getMeaningfulTokens(query);
    const candidateTokens = candidates
      .flatMap((candidate) => getMeaningfulTokens(candidate))
      .filter(Boolean);

    if (!queryTokens.length || !candidateTokens.length) {
      return {
        average: 0,
        best: 0,
        coverage: 0,
        allMatched: false,
      };
    }

    let total = 0;
    let best = 0;
    let covered = 0;
    let allMatched = true;

    queryTokens.forEach((queryToken) => {
      let bestTokenScore = 0;
      let matchedStrictly = false;

      candidateTokens.forEach((candidateToken) => {
        const tokenScore = similarityScore(queryToken, candidateToken);
        bestTokenScore = Math.max(bestTokenScore, tokenScore);

        if (isStrictTokenMatch(queryToken, candidateToken)) {
          matchedStrictly = true;
          bestTokenScore = Math.max(bestTokenScore, 0.99);
        }
      });

      if (matchedStrictly) {
        covered += 1;
      } else {
        allMatched = false;
      }

      best = Math.max(best, bestTokenScore);
      total += bestTokenScore;
    });

    return {
      average: total / queryTokens.length,
      best: best,
      coverage: covered / queryTokens.length,
      allMatched: allMatched,
    };
  }

  function passesNameMatch(query, candidates) {
    const queryTokens = getMeaningfulTokens(query);
    if (!queryTokens.length) {
      return false;
    }

    const metrics = getStrictNameMatchMetrics(query, candidates);

    if (queryTokens.length === 1) {
      return metrics.allMatched && metrics.best >= 0.98;
    }

    return metrics.allMatched && metrics.average >= 0.92 && metrics.coverage >= 1;
  }

  function dedupeBy(items, keyBuilder) {
    const seen = new Set();
    return items.filter((item) => {
      const key = keyBuilder(item);
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  }

  function buildResultAddress(tags, fallback) {
    const pieces = [
      tags && tags["addr:street"],
      tags && tags["addr:housenumber"],
      tags && tags["addr:suburb"],
      tags && tags["addr:city"],
      tags && tags["addr:state"],
    ].filter(Boolean);

    if (pieces.length) {
      return pieces.join(", ");
    }

    return fallback || "Dirección no disponible";
  }

  function buildOsmLink(lat, lon) {
    return "https://www.openstreetmap.org/?mlat=" + encodeURIComponent(lat) + "&mlon=" + encodeURIComponent(lon) + "#map=17/" + encodeURIComponent(lat) + "/" + encodeURIComponent(lon);
  }

  function buildAddReviewLink(result) {
    const url = new URL("/add-review", window.location.origin);
    if (typeof result.placeId === "number") {
      url.searchParams.set("placeId", String(result.placeId));
      return url.pathname + url.search;
    }

    url.searchParams.set("placeName", result.name || "");
    url.searchParams.set("placeType", result.subtitle || "");
    url.searchParams.set("placeLat", String(result.lat || ""));
    url.searchParams.set("placeLon", String(result.lon || ""));
    return url.pathname + url.search;
  }

  function buildViewReviewsLink(result) {
    const url = new URL("/place_reviews", window.location.origin);
    if (typeof result.placeId === "number") {
      url.searchParams.set("placeId", String(result.placeId));
    }
    url.searchParams.set("placeName", result.name || "");
    url.searchParams.set("placeType", result.subtitle || "");
    url.searchParams.set("placeLat", String(result.lat || ""));
    url.searchParams.set("placeLon", String(result.lon || ""));
    return url.pathname + url.search;
  }

  function updateStatus(message, type) {
    searchStatus.textContent = message;
    searchStatus.className = "small mt-3 mb-0 text-center";

    if (type === "error") {
      searchStatus.classList.add("text-danger");
      return;
    }

    if (type === "success") {
      searchStatus.classList.add("text-success");
      return;
    }

    searchStatus.classList.add("text-muted");
  }

  function clearMarkers() {
    markersLayer.clearLayers();
    activeMarkers = [];
    activePopupMarker = null;
  }

  function setLoadingState(isLoading) {
    searchButton.disabled = isLoading;
    searchInput.disabled = isLoading;
    searchButton.textContent = isLoading ? "Buscando..." : "Buscar";
  }

  function focusResult(index) {
    const selected = activeMarkers[index];
    if (!selected) {
      return;
    }

    activePopupMarker = selected.marker;
    map.setView([selected.result.lat, selected.result.lon], Math.max(map.getZoom(), 16), {
      animate: true,
    });
    selected.marker.openPopup();
  }

  function renderEmptyResults(message) {
    resultsSummary.textContent = message;
    resultsList.innerHTML = "";

    const emptyNode = document.createElement("div");
    emptyNode.className = "list-group-item reviews-placeholder-item";
    emptyNode.innerHTML = [
      '<div class="d-flex align-items-start gap-3">',
      '  <span class="reviews-placeholder-icon"><i class="bi bi-geo-alt"></i></span>',
      '  <div>',
      '    <h5 class="mb-1">No he encontrado coincidencias</h5>',
      '    <p class="mb-1 text-muted small">Prueba a cambiar la zona o escribir solo el nombre del lugar y la ciudad.</p>',
      '    <small class="text-muted">Ejemplos: “mcdonalds leganes”, “museo madrid”, “catedral sevilla”.</small>',
      '  </div>',
      '</div>',
    ].join("");

    resultsList.appendChild(emptyNode);
  }

  function renderResults(results, sourceLabel) {
    resultsSummary.textContent = results.length + " resultado" + (results.length === 1 ? "" : "s") + " en " + sourceLabel + ".";
    resultsList.innerHTML = "";

    results.forEach((result, index) => {
      const hasReviews = Number(result.reviewCount) > 0;
      const starClass = hasReviews ? "text-warning" : "text-secondary";
      const ratingLabel = hasReviews
        ? Number(result.averageRating || 0).toFixed(1) + " (" + Number(result.reviewCount) + ")"
        : "0.0 (0)";

      const item = document.createElement("button");
      item.type = "button";
      item.className = "list-group-item list-group-item-action text-start";
      item.innerHTML = [
        '<div class="d-flex w-100 justify-content-between gap-3">',
        '  <h5 class="mb-1"><i class="bi bi-geo-alt-fill text-primary me-2"></i>' + escapeHtml(result.name) + '</h5>',
        '  <small class="text-primary fw-semibold">' + Math.round(result.score * 100) + '%</small>',
        '</div>',
        '<p class="mb-1 text-muted small">' + escapeHtml(result.subtitle) + '</p>',
        '<small>📍 ' + escapeHtml(result.address) + '</small>',
        '<div class="small mt-1"><i class="bi bi-star-fill ' + starClass + '"></i> ' + escapeHtml(ratingLabel) + '</div>',
      ].join("");

      item.addEventListener("click", function () {
        focusResult(index);
      });

      resultsList.appendChild(item);
    });
  }

  function renderMarkers(results) {
    clearMarkers();

    const bounds = [];

    results.forEach((result) => {
      const hasReviews = Number(result.reviewCount) > 0;
      const starClass = hasReviews ? "text-warning" : "text-secondary";
      const ratingLabel = hasReviews
        ? Number(result.averageRating || 0).toFixed(1) + " (" + Number(result.reviewCount) + ")"
        : "0.0 (0)";
      const viewReviewsLinkHtml = '<a class="btn btn-sm btn-outline-primary mt-2 me-2" href="' + buildViewReviewsLink(result) + '">Ver reseñas</a>';
      const addReviewLinkHtml = '<a class="btn btn-sm btn-primary mt-2" href="' + buildAddReviewLink(result) + '">Añadir reseña</a>';

      const marker = L.marker([result.lat, result.lon]);
      marker.bindPopup(
        '<div class="reviews-map-popup">' +
          '<strong>' + escapeHtml(result.name) + '</strong><br>' +
          '<span>' + escapeHtml(result.subtitle) + '</span><br>' +
          '<small>' + escapeHtml(result.address) + '</small><br>' +
          '<small><i class="bi bi-star-fill ' + starClass + '"></i> ' + escapeHtml(ratingLabel) + '</small><br>' +
          '<div>' + viewReviewsLinkHtml + addReviewLinkHtml + '</div>' +
          '<a href="' + buildOsmLink(result.lat, result.lon) + '" target="_blank" rel="noopener noreferrer">Abrir en OpenStreetMap</a>' +
        '</div>'
      );
      marker.addTo(markersLayer);
      activeMarkers.push({ marker: marker, result: result });
      bounds.push([result.lat, result.lon]);
    });

    if (bounds.length === 1) {
      map.setView(bounds[0], 16);
    } else if (bounds.length > 1) {
      map.fitBounds(bounds, { padding: [40, 40] });
    }
  }

  async function fetchJson(url, options) {
    const response = await fetch(url, Object.assign({
      headers: {
        Accept: "application/json",
      },
    }, options || {}));

    if (!response.ok) {
      throw new Error("La búsqueda externa no ha respondido correctamente.");
    }

    return response.json();
  }

  async function enrichResultsWithPlaceMetrics(results, signal) {
    if (!results.length) {
      return results;
    }

    const names = dedupeBy(
      results
        .map((result) => result.name)
        .filter(Boolean),
      (name) => normalizeText(name)
    );

    if (!names.length) {
      return results;
    }

    const url = new URL("/api/reviews/place-metrics", window.location.origin);
    names.forEach((name) => url.searchParams.append("names", name));

    try {
      const metricsMap = await fetchJson(url.toString(), { signal: signal });
      return results.map((result) => {
        const metrics = metricsMap[result.name] || {};
        return Object.assign({}, result, {
          placeId: typeof metrics.placeId === "number" ? metrics.placeId : null,
          reviewCount: Number(metrics.reviewCount) || 0,
          averageRating: Number(metrics.averageRating) || 0,
        });
      });
    } catch (error) {
      return results.map((result) => Object.assign({}, result, {
        placeId: null,
        reviewCount: 0,
        averageRating: 0,
      }));
    }
  }

  function buildNominatimUrl(path, params) {
    const url = new URL(path, "https://nominatim.openstreetmap.org");
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, value);
      }
    });
    return url.toString();
  }

  function extractAreaCandidates(query) {
    const tokens = tokenize(query);
    const candidates = [];

    for (let startIndex = 1; startIndex < tokens.length; startIndex += 1) {
      const suffixTokens = tokens.slice(startIndex);
      const cleanedTokens = suffixTokens.filter((token) => !locationStopWords.has(token));
      const rawCandidate = suffixTokens.join(" ");
      const cleanedCandidate = cleanedTokens.join(" ");

      if (rawCandidate) {
        candidates.push({ text: rawCandidate, searchTerm: tokens.slice(0, startIndex).join(" ") });
      }

      if (cleanedCandidate && cleanedCandidate !== rawCandidate) {
        candidates.push({ text: cleanedCandidate, searchTerm: tokens.slice(0, startIndex).join(" ") });
      }
    }

    return dedupeBy(candidates, (item) => item.text + "|" + item.searchTerm);
  }

  async function resolveSearchArea(query, signal) {
    const candidates = extractAreaCandidates(query);
    const preferredTypes = new Set(["administrative", "city", "town", "village", "municipality", "suburb", "neighbourhood", "county"]);

    for (const candidate of candidates) {
      const url = buildNominatimUrl("/search", {
        q: candidate.text,
        format: "jsonv2",
        addressdetails: 1,
        limit: 5,
      });

      const results = await fetchJson(url, { signal: signal });
      if (!Array.isArray(results) || !results.length) {
        continue;
      }

      const preferredMatch = results.find((result) => preferredTypes.has(result.type));
      const selectedArea = preferredMatch || results[0];
      if (!selectedArea.boundingbox) {
        continue;
      }

      return {
        area: selectedArea,
        searchTerm: candidate.searchTerm,
      };
    }

    return null;
  }

  async function searchWithinArea(areaResult, query, signal) {
    const area = areaResult.area;
    const searchTerm = areaResult.searchTerm;
    const boundingBox = area.boundingbox.map(Number);
    const south = boundingBox[0];
    const north = boundingBox[1];
    const west = boundingBox[2];
    const east = boundingBox[3];

    const overpassQuery = [
      "[out:json][timeout:20];",
      "(",
      '  nwr["name"]["amenity"](' + south + "," + west + "," + north + "," + east + ");",
      '  nwr["name"]["shop"](' + south + "," + west + "," + north + "," + east + ");",
      '  nwr["name"]["tourism"](' + south + "," + west + "," + north + "," + east + ");",
      '  nwr["name"]["leisure"](' + south + "," + west + "," + north + "," + east + ");",
      '  nwr["name"]["historic"](' + south + "," + west + "," + north + "," + east + ");",
      '  nwr["brand"]["amenity"](' + south + "," + west + "," + north + "," + east + ");",
      '  nwr["brand"]["shop"](' + south + "," + west + "," + north + "," + east + ");",
      ");",
      "out center;",
    ].join("\n");

    const response = await fetchJson("https://overpass-api.de/api/interpreter", {
      method: "POST",
      body: overpassQuery,
      signal: signal,
    });

    const elements = Array.isArray(response.elements) ? response.elements : [];
    const searchText = searchTerm || query;
    const areaName = area.display_name || area.name || "la zona buscada";

    const mappedResults = elements
      .map((element) => {
        const tags = element.tags || {};
        const lat = element.lat || (element.center && element.center.lat);
        const lon = element.lon || (element.center && element.center.lon);
        const searchableFields = [tags.name, tags.brand].filter(Boolean);
        const searchableName = searchableFields.join(" ").trim();
        const primaryName = tags.name || tags.brand;

        if (!primaryName || typeof lat !== "number" || typeof lon !== "number") {
          return null;
        }

        if (!passesNameMatch(searchText, searchableFields)) {
          return null;
        }

        const typeLabel = tags.amenity || tags.shop || tags.tourism || tags.leisure || tags.historic || "Lugar";
        const address = buildResultAddress(tags, areaName);
        const combinedText = [primaryName, tags.brand, typeLabel, address, areaName].filter(Boolean).join(" ");
        const strictMetrics = getStrictNameMatchMetrics(searchText, searchableFields);
        const labelScore = Math.max(phraseScore(searchText, searchableName), strictMetrics.average);
        const areaScore = phraseScore(areaName, address + " " + combinedText);
        const fullQueryScore = phraseScore(query, combinedText);
        const finalScore = (labelScore * 0.9) + (areaScore * 0.05) + (fullQueryScore * 0.05);

        return {
          name: primaryName,
          subtitle: typeLabel.charAt(0).toUpperCase() + typeLabel.slice(1),
          address: address,
          lat: lat,
          lon: lon,
          score: finalScore,
        };
      })
      .filter(Boolean);

    const dedupedResults = dedupeBy(mappedResults, (result) => {
      return normalizeText(result.name) + "|" + result.lat.toFixed(4) + "|" + result.lon.toFixed(4);
    });

    return dedupedResults
      .filter((result) => result.score >= (searchTerm ? 0.9 : 0.88))
      .sort((left, right) => right.score - left.score)
      .slice(0, maxResults);
  }

  async function searchGlobally(query, signal) {
    const url = buildNominatimUrl("/search", {
      q: query,
      format: "jsonv2",
      addressdetails: 1,
      limit: 20,
      dedupe: 1,
    });

    const results = await fetchJson(url, { signal: signal });

    return (Array.isArray(results) ? results : [])
      .map((result) => {
        const lat = Number(result.lat);
        const lon = Number(result.lon);
        const name = result.name || (result.display_name ? result.display_name.split(",")[0] : "Lugar");
        const searchableFields = [name];
        const searchableName = searchableFields.join(" ");
        const subtitle = [result.type, result.category].filter(Boolean).join(" · ") || "Lugar";
        const address = result.display_name || "Dirección no disponible";
        const strictMetrics = getStrictNameMatchMetrics(query, searchableFields);
        const score = (Math.max(phraseScore(query, name), strictMetrics.average) * 0.95) + (phraseScore(query, address) * 0.05);

        if (Number.isNaN(lat) || Number.isNaN(lon)) {
          return null;
        }

        if (!passesNameMatch(query, searchableFields)) {
          return null;
        }

        return {
          name: name,
          subtitle: subtitle,
          address: address,
          lat: lat,
          lon: lon,
          score: score,
        };
      })
      .filter(Boolean)
      .filter((result) => result.score >= 0.9)
      .sort((left, right) => right.score - left.score)
      .slice(0, maxResults);
  }

  async function performSearch() {
    const query = searchInput.value.trim();
    if (!query) {
      clearMarkers();
      map.setView(defaultCenter, defaultZoom);
      updateStatus("Escribe algo como “mcdonalds leganes” para empezar la búsqueda.", "default");
      renderEmptyResults("Aún no hay resultados. Lanza una búsqueda para ver los lugares en el mapa.");
      return;
    }

    if (activeRequestController) {
      activeRequestController.abort();
    }

    activeRequestController = new AbortController();
    setLoadingState(true);
    updateStatus("Buscando lugares y calculando coincidencias aproximadas...", "default");

    try {
      const areaContext = await resolveSearchArea(query, activeRequestController.signal);
      let results = [];
      let sourceLabel = "OpenStreetMap";

      if (areaContext && areaContext.searchTerm) {
        results = await searchWithinArea(areaContext, query, activeRequestController.signal);
        sourceLabel = areaContext.area.display_name || areaContext.area.name || "la zona encontrada";
      }

      if (!results.length) {
        results = await searchGlobally(query, activeRequestController.signal);
        sourceLabel = "búsqueda global";
      }

      results = await enrichResultsWithPlaceMetrics(results, activeRequestController.signal);

      if (!results.length) {
        clearMarkers();
        renderEmptyResults("No he encontrado coincidencias para esa búsqueda.");
        updateStatus("No hay coincidencias. Prueba con otra zona o con menos palabras.", "error");
        return;
      }

      renderResults(results, sourceLabel);
      renderMarkers(results);
      updateStatus("He encontrado " + results.length + " coincidencia" + (results.length === 1 ? "" : "s") + ".", "success");
    } catch (error) {
      if (error && error.name === "AbortError") {
        return;
      }

      clearMarkers();
      renderEmptyResults("La búsqueda no se ha podido completar.");
      updateStatus("No se pudo consultar OpenStreetMap en este momento. Inténtalo de nuevo en unos segundos.", "error");
    } finally {
      setLoadingState(false);
    }
  }

  searchButton.addEventListener("click", performSearch);
  searchInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      event.preventDefault();
      performSearch();
    }
  });
})();