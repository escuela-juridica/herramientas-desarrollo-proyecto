// Responsable: Enrique Prada (feature/base-navbar-footer)
// Cierra el menú móvil (el checkbox oculto #navToggle) cuando el
// usuario hace clic en cualquier enlace dentro de #mainNav. Sin
// esto, el menú desplegado en pantallas angostas se quedaría
// abierto tapando la pantalla después de navegar a una sección.
const navToggle = document.querySelector("#navToggle");
const siteNav = document.querySelector("#mainNav");

siteNav?.addEventListener("click", (event) => {
  if (event.target.closest("a") && navToggle) {
    navToggle.checked = false;
  }
});
