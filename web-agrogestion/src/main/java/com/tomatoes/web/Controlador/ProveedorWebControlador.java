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
@PreAuthorize("hasAnyRole('BODEGA','ADMINISTRADOR','GERENTE')")
public class ProveedorWebControlador {

    private static final String BODEGA = "hasRole('BODEGA')";
    private final ClienteApi api;

    public ProveedorWebControlador(ClienteApi api) {
        this.api = api;
    }

    @GetMapping("/proveedores")
    public String lista(Model model) {
        model.addAttribute("proveedores", api.lista(Ms.INSUMOS, "/api/proveedores"));
        return "proveedor/lista";
    }

    @GetMapping("/proveedores/nuevo")
    @PreAuthorize(BODEGA)
    public String nuevo(Model model) {
        return formulario(model, new LinkedHashMap<>(), null);
    }

    @PostMapping("/proveedores")
    @PreAuthorize(BODEGA)
    public String crear(@RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p);
        try {
            api.enviar(Ms.INSUMOS, HttpMethod.POST, "/api/proveedores", cuerpo);
            ra.addFlashAttribute("exito", "Proveedor creado correctamente.");
            return "redirect:/proveedores";
        } catch (ApiException e) {
            return formulario(model, cuerpo, e);
        }
    }

    @GetMapping("/proveedores/{id}/editar")
    @PreAuthorize(BODEGA)
    public String editar(@PathVariable String id, Model model) {
        return formulario(model, api.mapa(Ms.INSUMOS, "/api/proveedores/" + id), null);
    }

    @PostMapping("/proveedores/{id}")
    @PreAuthorize(BODEGA)
    public String actualizar(@PathVariable String id, @RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p);
        try {
            api.enviar(Ms.INSUMOS, HttpMethod.PUT, "/api/proveedores/" + id, cuerpo);
            ra.addFlashAttribute("exito", "Proveedor actualizado correctamente.");
            return "redirect:/proveedores";
        } catch (ApiException e) {
            cuerpo.put("id", id);
            return formulario(model, cuerpo, e);
        }
    }

    @PostMapping("/proveedores/{id}/eliminar")
    @PreAuthorize(BODEGA)
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        return Accion.hacer(ra, "/proveedores", () -> api.accion(Ms.INSUMOS, HttpMethod.DELETE, "/api/proveedores/" + id), "Proveedor eliminado.");
    }

    private String formulario(Model model, Map<String, Object> datos, ApiException e) {
        model.addAttribute("proveedor", datos);
        if (e != null) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("errores", e.getErrores());
        }
        return "proveedor/form";
    }
}
