const navToggle = document.querySelector("#navToggle");
const siteNav = document.querySelector("#mainNav");

siteNav?.addEventListener("click", (event) => {
  if (event.target.closest("a") && navToggle) navToggle.checked = false;
});
