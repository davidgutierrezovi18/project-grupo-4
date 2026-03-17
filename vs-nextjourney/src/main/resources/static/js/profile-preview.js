document.addEventListener("DOMContentLoaded", function () {
    const fileInput = document.getElementById("profileImage");
    const preview = document.getElementById("previewImage");

    if (fileInput && preview) {
        fileInput.addEventListener("change", function (event) {
            const file = event.target.files[0];

            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    preview.src = e.target.result; // cambia la imagen
                };
                reader.readAsDataURL(file);
            }
        });
    }
});