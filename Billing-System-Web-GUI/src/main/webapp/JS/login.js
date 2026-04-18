// TOGGLE PASSWORD + ICON
function togglePassword() {
    const pass = document.getElementById("password");
    const icon = document.getElementById("eyeIcon");

    if (pass.type === "password") {
        pass.type = "text";
        icon.classList.remove("fa-eye-slash");
        icon.classList.add("fa-eye");
    } else {
        pass.type = "password";
        icon.classList.remove("fa-eye");
        icon.classList.add("fa-eye-slash");
    }
}

// TELECOM ICONS BACKGROUND
const container = document.getElementById("particles");

const icons = [
    "fa-comment",      // SMS
    "fa-phone",        // Voice
    "fa-wifi",         // Data
    "fa-sack-dollar"   // Billing
];

for (let i = 0; i < 40; i++) {
    const icon = document.createElement("i");

    icon.classList.add("fa-solid");
    icon.classList.add(icons[Math.floor(Math.random() * icons.length)]);
    icon.classList.add("floating-icon");

    icon.style.left = Math.random() * 100 + "vw";
    icon.style.fontSize = (12 + Math.random() * 18) + "px";
    icon.style.animationDuration = (6 + Math.random() * 10) + "s";

    container.appendChild(icon);
}