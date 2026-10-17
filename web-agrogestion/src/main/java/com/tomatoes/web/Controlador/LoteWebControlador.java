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
@PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
public class LoteWebControlador {

    private static final String SUP = "hasRole('SUPERVISOR')";
    private final ClienteApi api;

    public LoteWebControlador(ClienteApi api) {
        this.api = api;
    }

    @GetMapping("/lotes")
    public String lista(@RequestParam(required = false) String estado, Model model) {
        List<Map<String, Object>> lotes = api.lista(Ms.COSECHAS, "/api/lotes", Datos.filtro("estado", estado));
        model.addAttribute("lotes", lotes);
        model.addAttribute("estadoSel", estado == null ? "" : estado);
        model.addAttribute("totalHectareas", Datos.suma(lotes.stream().filter(l -> "ACTIVO".equals(l.get("estado"))).toList(), "areaHectareas"));
        return "lote/lista";
    }

    @GetMapping("/lotes/nuevo")
    @PreAuthorize(SUP)
    public String nuevo(Model model) {
        return formulario(model, new LinkedHashMap<>(), null);
    }

    @PostMapping("/lotes")
    @PreAuthorize(SUP)
    public String crear(@RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "areaHectareas");
        try {
            api.enviar(Ms.COSECHAS, HttpMethod.POST, "/api/lotes", cuerpo);
            ra.addFlashAttribute("exito", "Lote creado correctamente.");
            return "redirect:/lotes";
        } catch (ApiException e) {
            return formulario(model, cuerpo, e);
        }
    }

    @GetMapping("/lotes/{id}/editar")
    @PreAuthorize(SUP)
    public String editar(@PathVariable String id, Model model) {
        return formulario(model, api.mapa(Ms.COSECHAS, "/api/lotes/" + id), null);
    }

    @PostMapping("/lotes/{id}")
    @PreAuthorize(SUP)
    public String actualizar(@PathVariable String id, @RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "areaHectareas");
        try {
            api.enviar(Ms.COSECHAS, HttpMethod.PUT, "/api/lotes/" + id, cuerpo);
            ra.addFlashAttribute("exito", "Lote actualizado correctamente.");
            return "redirect:/lotes";
        } catch (ApiException e) {
            cuerpo.put("id", id);
            return formulario(model, cuerpo, e);
        }
    }

    @PostMapping("/lotes/{id}/desactivar")
    @PreAuthorize(SUP)
    public String desactivar(@PathVariable String id, RedirectAttributes ra) {
        return Accion.hacer(ra, "/lotes", () -> api.accion(Ms.COSECHAS, HttpMethod.DELETE, "/api/lotes/" + id), "Lote desactivado.");
    }

    @PostMapping("/lotes/{id}/activar")
    @PreAuthorize(SUP)
    public String activar(@PathVariable String id, RedirectAttributes ra) {
        return Accion.hacer(ra, "/lotes", () -> api.accion(Ms.COSECHAS, HttpMethod.PUT, "/api/lotes/" + id + "/activar"), "Lote reactivado.");
    }

    private String formulario(Model model, Map<String, Object> datos, ApiException e) {
        model.addAttribute("lote", datos);
        if (e != null) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("errores", e.getErrores());
        }
        return "lote/form";
    }
}
