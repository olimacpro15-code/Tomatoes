(function () {
  'use strict';
  var sidebar = document.getElementById('sidebar');
  var fondo = document.getElementById('sidebarFondo');
  function alternar(abrir) {
    if (!sidebar) return;
    sidebar.classList.toggle('abierto', abrir);
    if (fondo) fondo.classList.toggle('visible', abrir);
  }
  document.querySelectorAll('[data-menu]').forEach(function (b) {
    b.addEventListener('click', function () { alternar(!sidebar.classList.contains('abierto')); });
  });
  if (fondo) fondo.addEventListener('click', function () { alternar(false); });

  // Confirmación antes de acciones delicadas: <form data-confirmar="¿Seguro?">
  document.querySelectorAll('form[data-confirmar]').forEach(function (f) {
    f.addEventListener('submit', function (e) {
      if (!window.confirm(f.getAttribute('data-confirmar'))) e.preventDefault();
    });
  });

  // Buscador rápido de tablas: <input data-filtro="#idTabla">
  document.querySelectorAll('input[data-filtro]').forEach(function (inp) {
    var filas = document.querySelectorAll(inp.getAttribute('data-filtro') + ' tbody tr');
    inp.addEventListener('input', function () {
      var q = inp.value.trim().toLowerCase();
      filas.forEach(function (tr) { tr.style.display = tr.textContent.toLowerCase().indexOf(q) === -1 ? 'none' : ''; });
    });
  });

  // Los mensajes de éxito se ocultan solos
  document.querySelectorAll('.alert-success.alerta-flash').forEach(function (a) {
    setTimeout(function () { a.classList.add('d-none'); }, 6000);
  });

  // Mostrar/ocultar contraseña
  document.querySelectorAll('[data-ver-clave]').forEach(function (b) {
    b.addEventListener('click', function () {
      var i = document.querySelector(b.getAttribute('data-ver-clave'));
      if (!i) return;
      i.type = i.type === 'password' ? 'text' : 'password';
      b.querySelector('i').className = i.type === 'password' ? 'bi bi-eye' : 'bi bi-eye-slash';
    });
  });
})();
