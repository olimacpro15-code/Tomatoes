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
@PreAuthorize("hasAnyRole('BODEGA','SUPERVISOR','ADMINISTRADOR')")
public class InsumoWebControlador {

    private static final String BODEGA = "hasRole('BODEGA')";
    private static final List<String> CATEGORIAS = List.of("SEMILLA", "FERTILIZANTE", "AGROQUIMICO", "HERRAMIENTA", "COMBUSTIBLE");
    private static final List<String> UNIDADES = List.of("kg", "g", "L", "mL", "bulto", "unidad", "galón");

    private final ClienteApi api;
    private final CatalogoService catalogo;

    public InsumoWebControlador(ClienteApi api, CatalogoService catalogo) {
        this.api = api;
        this.catalogo = catalogo;
    }

    @GetMapping("/insumos")
    public String lista(@RequestParam(required = false) String estado, Model model) {
        List<Map<String, Object>> insumos = api.lista(Ms.INSUMOS, "/api/insumos", Datos.filtro("estado", estado));
        model.addAttribute("insumos", insumos);
        model.addAttribute("proveedores", catalogo.nombresProveedores());
        model.addAttribute("estadoSel", estado == null ? "" : estado);
        return "insumo/lista";
    }

    /** HU-13: alerta de stock bajo. */
    @GetMapping("/insumos/stock-bajo")
    @PreAuthorize("hasAnyRole('BODEGA','ADMINISTRADOR')")
    public String stockBajo(Model model) {
        model.addAttribute("insumos", api.lista(Ms.INSUMOS, "/api/insumos/stock-bajo"));
        model.addAttribute("proveedores", catalogo.nombresProveedores());
        return "insumo/stock-bajo";
    }

    @GetMapping("/insumos/nuevo")
    @PreAuthorize(BODEGA)
    public String nuevo(Model model) {
        return formulario(model, new LinkedHashMap<>(), null);
    }

    @PostMapping("/insumos")
    @PreAuthorize(BODEGA)
    public String crear(@RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "stockActual", "stockMinimo", "costoUnitario");
        try {
            api.enviar(Ms.INSUMOS, HttpMethod.POST, "/api/insumos", cuerpo);
            ra.addFlashAttribute("exito", "Insumo creado correctamente.");
            return "redirect:/insumos";
        } catch (ApiException e) {
            return formulario(model, cuerpo, e);
        }
    }

    @GetMapping("/insumos/{id}/editar")
    @PreAuthorize(BODEGA)
    public String editar(@PathVariable String id, Model model) {
        return formulario(model, api.mapa(Ms.INSUMOS, "/api/insumos/" + id), null);
    }

    @PostMapping("/insumos/{id}")
    @PreAuthorize(BODEGA)
    public String actualizar(@PathVariable String id, @RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "stockMinimo", "costoUnitario");
        try {
            api.enviar(Ms.INSUMOS, HttpMethod.PUT, "/api/insumos/" + id, cuerpo);
            ra.addFlashAttribute("exito", "Insumo actualizado correctamente.");
            return "redirect:/insumos";
        } catch (ApiException e) {
            cuerpo.put("id", id);
            return formulario(model, cuerpo, e);
        }
    }

    @PostMapping("/insumos/{id}/desactivar")
    @PreAuthorize(BODEGA)
    public String desactivar(@PathVariable String id, RedirectAttributes ra) {
        return Accion.hacer(ra, "/insumos", () -> api.accion(Ms.INSUMOS, HttpMethod.DELETE, "/api/insumos/" + id), "Insumo desactivado.");
    }

    @PostMapping("/insumos/{id}/activar")
    @PreAuthorize(BODEGA)
    public String activar(@PathVariable String id, RedirectAttributes ra) {
        return Accion.hacer(ra, "/insumos", () -> api.accion(Ms.INSUMOS, HttpMethod.PUT, "/api/insumos/" + id + "/activar"), "Insumo reactivado.");
    }

    private String formulario(Model model, Map<String, Object> datos, ApiException e) {
        model.addAttribute("insumo", datos);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("unidades", UNIDADES);
        model.addAttribute("proveedores", api.lista(Ms.INSUMOS, "/api/proveedores"));
        if (e != null) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("errores", e.getErrores());
        }
        return "insumo/form";
    }
}
