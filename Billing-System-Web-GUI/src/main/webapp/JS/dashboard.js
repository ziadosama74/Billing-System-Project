function go(page) {
    window.location.href = page;
}

function logout() {
     window.location.href = "../HTML/login.html";
}

let Bill_BOX = document.getElementById("Billbox");
let Subscribers_BOX = document.getElementById("subbox");
let Revenue_BOX = document.getElementById("Revbox");
console.log(Bill_BOX);
console.log(Subscribers_BOX);
console.log(Revenue_BOX);
fetch('/Billing-System-Web-GUI/JSON/parameters.json')
    .then(response => response.json())
    .then(data => {
        Bill_BOX.innerHTML = data.Bills;
        Subscribers_BOX.innerHTML = data.Subscribers;
        Revenue_BOX.innerHTML = data.Revenue + " K";
});