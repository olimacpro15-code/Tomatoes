package com.tomatoes.web.Controlador;

import com.tomatoes.web.Excepciones.ApiException;
import com.tomatoes.web.Servicio.CatalogoService;
import com.tomatoes.web.Servicio.ClienteApi;
import com.tomatoes.web.Servicio.ClienteApi.Ms;
import com.tomatoes.web.Util.Accion;
import com.tomatoes.web.Util.Datos;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class EmpleadoWebControlador {

    private static final String ADMIN = "hasRole('ADMINISTRADOR')";
    private static final List<String> ROLES = List.of("ADMINISTRADOR", "GERENTE", "SUPERVISOR", "BODEGA", "OPERARIO");

    private final ClienteApi api;

    public EmpleadoWebControlador(ClienteApi api) {
        this.api = api;
    }

    @GetMapping("/empleados")
    public String lista(@RequestParam(required = false) String estado, Model model) {
        model.addAttribute("empleados", api.lista(Ms.EMPLEADOS, "/api/empleados", Datos.filtro("estado", estado)));
        model.addAttribute("estadoSel", estado == null ? "" : estado);
        return "empleado/lista";
    }

    @GetMapping("/empleados/nuevo")
    @PreAuthorize(ADMIN)
    public String nuevo(Model model) {
        return formulario(model, new LinkedHashMap<>(), null);
    }

    @PostMapping("/empleados")
    @PreAuthorize(ADMIN)
    public String crear(@RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p);
        try {
            api.enviar(Ms.EMPLEADOS, HttpMethod.POST, "/api/empleados", cuerpo);
            ra.addFlashAttribute("exito", "Empleado creado correctamente.");
            return "redirect:/empleados";
        } catch (ApiException e) {
            return formulario(model, cuerpo, e);
        }
    }

    @GetMapping("/empleados/{id}/editar")
    @PreAuthorize(ADMIN)
    public String editar(@PathVariable String id, Model model) {
        return formulario(model, api.mapa(Ms.EMPLEADOS, "/api/empleados/" + id), null);
    }

    @PostMapping("/empleados/{id}")
    @PreAuthorize(ADMIN)
    public String actualizar(@PathVariable String id, @RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p);
        try {
            api.enviar(Ms.EMPLEADOS, HttpMethod.PUT, "/api/empleados/" + id, cuerpo);
            ra.addFlashAttribute("exito", "Empleado actualizado correctamente.");
            return "redirect:/empleados";
        } catch (ApiException e) {
            cuerpo.put("id", id);
            return formulario(model, cuerpo, e);
        }
    }

    @PostMapping("/empleados/{id}/desactivar")
    @PreAuthorize(ADMIN)
    public String desactivar(@PathVariable String id, RedirectAttributes ra) {
        return Accion.hacer(ra, "/empleados", () -> api.accion(Ms.EMPLEADOS, HttpMethod.DELETE, "/api/empleados/" + id), "Empleado desactivado.");
    }

    @PostMapping("/empleados/{id}/activar")
    @PreAuthorize(ADMIN)
    public String activar(@PathVariable String id, RedirectAttributes ra) {
        return Accion.hacer(ra, "/empleados", () -> api.accion(Ms.EMPLEADOS, HttpMethod.PUT, "/api/empleados/" + id + "/activar"), "Empleado reactivado.");
    }

    /** HU-03: asignación de roles. */
    @GetMapping("/roles")
    @PreAuthorize(ADMIN)
    public String roles(Model model) {
        model.addAttribute("empleados", api.lista(Ms.EMPLEADOS, "/api/empleados"));
        model.addAttribute("roles", ROLES);
        return "empleado/roles";
    }

    @PostMapping("/roles/{id}")
    @PreAuthorize(ADMIN)
    public String cambiarRol(@PathVariable String id, @RequestParam String rol, RedirectAttributes ra) {
        return Accion.hacer(ra, "/roles", () -> api.enviar(Ms.EMPLEADOS, HttpMethod.PUT, "/api/empleados/" + id + "/rol", Map.of("rol", rol)), "Rol actualizado.");
    }

    private String formulario(Model model, Map<String, Object> datos, ApiException e) {
        model.addAttribute("empleado", datos);
        model.addAttribute("roles", ROLES);
        if (e != null) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("errores", e.getErrores());
        }
        return "empleado/form";
    }
}
