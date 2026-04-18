// LOGIN
function goToLogin() {
    window.location.href = "../HTML/login.html";
}

// FLOW ANIMATION
const steps = document.querySelectorAll(".step");
let current = 0;

function animateFlow() {
    steps.forEach(step => step.classList.remove("active"));
    steps[current].classList.add("active");

    current++;
    if (current >= steps.length)
        current = 0;
}

setInterval(animateFlow, 1200);