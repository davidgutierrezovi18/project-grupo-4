document.addEventListener("DOMContentLoaded", () => {
  const previews = document.querySelectorAll(".js-review-preview");
  const maxChars = 220;

  previews.forEach((preview) => {
    const fullText = (preview.textContent || "").trim();
    const reviewCard = preview.closest(".card-body");
    const imageGallery = reviewCard ? reviewCard.querySelector(".js-review-images") : null;
    const hasImages = imageGallery && imageGallery.children.length > 0;
    const needsTextCut = fullText.length > maxChars;

    if (!needsTextCut && !hasImages) {
      return;
    }

    let shortText = fullText;
    if (needsTextCut) {
      const cutPoint = findCutPoint(fullText, maxChars);
      shortText = `${fullText.slice(0, cutPoint).trimEnd()}...`;
    }

    preview.dataset.fullText = fullText;
    preview.dataset.shortText = shortText;
    preview.dataset.expanded = "false";
    preview.textContent = shortText;

    if (hasImages) {
      imageGallery.classList.add("d-none");
    }

    const toggleButton = document.createElement("button");
    toggleButton.type = "button";
    toggleButton.className = "btn btn-link btn-sm p-0 ms-1 align-baseline";
    toggleButton.textContent = "Ver mas";
    toggleButton.setAttribute("aria-expanded", "false");

    toggleButton.addEventListener("click", () => {
      const isExpanded = preview.dataset.expanded === "true";
      preview.dataset.expanded = isExpanded ? "false" : "true";
      preview.textContent = isExpanded || !needsTextCut ? preview.dataset.shortText : preview.dataset.fullText;
      toggleButton.textContent = isExpanded ? "Ver mas" : "Ver menos";
      toggleButton.setAttribute("aria-expanded", isExpanded ? "false" : "true");

      if (hasImages) {
        imageGallery.classList.toggle("d-none", !isExpanded ? false : true);
      }
    });

    preview.insertAdjacentElement("afterend", toggleButton);
  });

  initReviewImageModal();
});

function findCutPoint(text, maxChars) {
  const candidate = text.slice(0, maxChars);
  const lastSpace = candidate.lastIndexOf(" ");

  if (lastSpace > Math.floor(maxChars * 0.6)) {
    return lastSpace;
  }

  return maxChars;
}

function initReviewImageModal() {
  const thumbs = document.querySelectorAll(".js-review-image-thumb");
  if (!thumbs.length || typeof bootstrap === "undefined") {
    return;
  }

  const modalElement = document.createElement("div");
  modalElement.className = "modal fade";
  modalElement.id = "reviewImageModal";
  modalElement.tabIndex = -1;
  modalElement.setAttribute("aria-hidden", "true");
  modalElement.innerHTML = `
    <div class="modal-dialog modal-dialog-centered modal-xl">
      <div class="modal-content bg-dark border-0">
        <div class="modal-header border-0 pb-0">
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body text-center pt-2">
          <img src="" alt="Foto ampliada" class="img-fluid rounded" style="max-height: 80vh; object-fit: contain;" />
        </div>
      </div>
    </div>
  `;

  document.body.appendChild(modalElement);
  const modalImage = modalElement.querySelector("img");
  const imageModal = new bootstrap.Modal(modalElement);

  thumbs.forEach((thumb) => {
    thumb.addEventListener("click", () => {
      modalImage.src = thumb.src;
      modalImage.alt = thumb.alt || "Foto ampliada";
      imageModal.show();
    });
  });

  modalElement.addEventListener("hidden.bs.modal", () => {
    modalImage.src = "";
  });
}
