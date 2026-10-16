document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".admin-alert").forEach((alerta) => {
    const cerrar = document.createElement("button");
    cerrar.type = "button";
    cerrar.className = "admin-alert-close";
    cerrar.setAttribute("aria-label", "Cerrar aviso");
    cerrar.textContent = "×";
    alerta.append(cerrar);

    let cerrada = false;
    let temporizador;
    const ocultar = () => {
      if (cerrada) return;
      cerrada = true;
      clearTimeout(temporizador);
      alerta.classList.add("is-hiding");
      setTimeout(() => alerta.remove(), 200);
    };

    cerrar.addEventListener("click", ocultar);
    if (alerta.classList.contains("admin-alert-success")) {
      temporizador = setTimeout(ocultar, 5000);
    }
  });
});
