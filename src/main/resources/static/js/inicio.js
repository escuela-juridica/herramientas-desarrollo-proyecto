// SECCIÓN: NAVBAR
const navToggle = document.querySelector("#navToggle");
const siteNav = document.querySelector("#mainNav");

siteNav?.addEventListener("click", (event) => {
  if (event.target.closest("a") && navToggle) {
    navToggle.checked = false;
  }
});

// GLOBAL
document.addEventListener("click", (event) => {
  const link = event.target.closest('a[href="#"]');
  if (link) {
    event.preventDefault();
  }
});
