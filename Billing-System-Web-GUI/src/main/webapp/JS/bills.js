// STATUS FILTER
function filterStatus(status, element) {

    document.getElementById("statusInput").value = status;

    document.querySelectorAll(".status-card").forEach(card => {
        card.classList.remove("active");
    });

    element.classList.add("active");
}

// MONTHS
window.onload = function () {

    const select = document.getElementById("monthSelect");

    const months = [
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    ];

    const year = new Date().getFullYear();

    months.forEach((m, i) => {
        let option = document.createElement("option");
        option.value = `${year}-${i+1}`;
        option.text = `${m} ${year}`;
        select.appendChild(option);
    });

};