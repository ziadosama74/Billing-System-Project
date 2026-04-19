  console.log("Ziad");
  document.addEventListener("DOMContentLoaded", () => {

    const form = document.querySelector("form");
    const dropArea = document.getElementById("dropArea");
    const fileInput = document.getElementById("fileInput");
    const fileInfo = document.getElementById("fileInfo");

    let selectedFile = null;

    // CLICK → open file picker
    dropArea.addEventListener("click", () => fileInput.click());

    // SELECT FILE
    fileInput.addEventListener("change", () => {
        selectedFile = fileInput.files[0];
        showFile(selectedFile);
    });

    // DRAG OVER
    dropArea.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropArea.classList.add("active");
    });

    // DRAG LEAVE
    dropArea.addEventListener("dragleave", () => {
        dropArea.classList.remove("active");
    });

    // DROP FILE
    dropArea.addEventListener("drop", (e) => {
        e.preventDefault();
        dropArea.classList.remove("active");

        selectedFile = e.dataTransfer.files[0];

        const dt = new DataTransfer();
        dt.items.add(selectedFile);
        fileInput.files = dt.files;

        showFile(selectedFile);
    });

    // 🔴 IMPORTANT: BLOCK SUBMIT IF NO FILE
    form.addEventListener("submit", (e) => {
        if (!fileInput.files.length) {
            e.preventDefault(); // ❌ stop form

            fileInfo.innerHTML = `
                <span style="color:red;">
                    ❌ Please upload a file first
                </span>
            `;
        }
    });

    function showFile(file) {
        if (!file)
            return;

        fileInfo.innerHTML = `
            <i class="fa-solid fa-file"></i>
            ${file.name}
        `;
    }

});
// LOGOUT FUNCTION
function logout() {
    window.location.href = "../HTML/home.html";
}