package com.tomatoes.web.Controlador;

import com.tomatoes.web.Seguridad.UsuarioSesion;
import com.tomatoes.web.Servicio.ClienteApi;
import com.tomatoes.web.Servicio.ClienteApi.Ms;
import com.tomatoes.web.Util.Datos;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class HomeControlador {

    private final ClienteApi api;
    private final boolean mostrarDemo;

    public HomeControlador(ClienteApi api, @Value("${tomatoes.mostrar-demo:false}") boolean mostrarDemo) {
        this.api = api;
        this.mostrarDemo = mostrarDemo;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Authentication autenticacion, HttpSession sesion, Model model) {
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioSesion) return "redirect:/inicio";
        Object error = sesion.getAttribute("LOGIN_ERROR");
        if (error != null) {
            model.addAttribute("errorLogin", error);
            sesion.removeAttribute("LOGIN_ERROR");
        }
        model.addAttribute("mostrarDemo", mostrarDemo);
        return "login";
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/403";
    }

    /** HU-16: panel de indicadores para Gerente (y consulta del Administrador). */
    @GetMapping("/panel")
    @PreAuthorize("hasAnyRole('GERENTE','ADMINISTRADOR')")
    public String panel(Model model) {
        List<String> avisos = new ArrayList<>();
        List<Map<String, Object>> stockBajo = Datos.segura(() -> api.lista(Ms.INSUMOS, "/api/insumos/stock-bajo"), "insumos", avisos);
        List<Map<String, Object>> enCurso = Datos.segura(() -> api.lista(Ms.COSECHAS, "/api/cosechas", Datos.filtro("estado", "EN_CURSO")), "cosechas", avisos);
        List<Map<String, Object>> empleados = Datos.segura(() -> api.lista(Ms.EMPLEADOS, "/api/empleados", Datos.filtro("estado", "ACTIVO")), "empleados", avisos);
        List<Map<String, Object>> lotes = Datos.segura(() -> api.lista(Ms.COSECHAS, "/api/lotes", Datos.filtro("estado", "ACTIVO")), "cosechas", avisos);
        List<Map<String, Object>> produccion = Datos.segura(() -> api.lista(Ms.COSECHAS, "/api/reportes/produccion", Datos.filtro("desde", Datos.hace(90))), "cosechas", avisos);

        Map<String, String> nombresLotes = Datos.indexar(lotes, "id", "nombre");
        enCurso.forEach(c -> c.put("loteNombre", nombresLotes.getOrDefault(String.valueOf(c.get("loteId")), "Lote")));
        Datos.porcentajes(produccion, "totalKg", "pct");

        model.addAttribute("totalStockBajo", stockBajo.size());
        model.addAttribute("totalEnCurso", enCurso.size());
        model.addAttribute("totalEmpleados", empleados.size());
        model.addAttribute("totalLotes", lotes.size());
        model.addAttribute("stockBajo", stockBajo.stream().limit(6).toList());
        model.addAttribute("enCurso", enCurso.stream().limit(6).toList());
        model.addAttribute("produccion", produccion.stream().limit(6).toList());
        model.addAttribute("totalKg", Datos.suma(produccion, "totalKg"));
        model.addAttribute("avisos", avisos);
        return "panel";
    }
}
