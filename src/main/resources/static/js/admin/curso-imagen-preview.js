const imagenArchivo = document.getElementById('imagenArchivo');
const nuevaImagenVista = document.getElementById('nuevaImagenVista');
const nuevaImagenMiniatura = document.getElementById('nuevaImagenMiniatura');
const nuevaImagenCompleta = document.getElementById('nuevaImagenCompleta');

if (imagenArchivo && nuevaImagenVista && nuevaImagenMiniatura && nuevaImagenCompleta) {
  let imagenTemporal = null;

  imagenArchivo.addEventListener('change', () => {
    if (imagenTemporal) {
      URL.revokeObjectURL(imagenTemporal);
      imagenTemporal = null;
    }

    const archivo = imagenArchivo.files?.[0];
    if (!archivo || !['image/jpeg', 'image/png'].includes(archivo.type)) {
      nuevaImagenMiniatura.removeAttribute('src');
      nuevaImagenCompleta.removeAttribute('href');
      nuevaImagenVista.hidden = true;
      return;
    }

    imagenTemporal = URL.createObjectURL(archivo);
    nuevaImagenMiniatura.src = imagenTemporal;
    nuevaImagenCompleta.href = imagenTemporal;
    nuevaImagenVista.hidden = false;
  });
}
