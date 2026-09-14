// NAVBAR — Enrique Prada (feature/base-navbar-footer)
// COMPLETO Y FUNCIONAL — no modificar.
const navToggle = document.querySelector("#navToggle");
const siteNav = document.querySelector("#mainNav");

siteNav?.addEventListener("click", (event) => {
  if (event.target.closest("a") && navToggle) {
    navToggle.checked = false;
  }
});
