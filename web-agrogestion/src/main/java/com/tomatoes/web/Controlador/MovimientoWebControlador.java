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
@PreAuthorize("hasAnyRole('BODEGA','ADMINISTRADOR')")
public class MovimientoWebControlador {

    private static final String BODEGA = "hasRole('BODEGA')";
    private final ClienteApi api;

    public MovimientoWebControlador(ClienteApi api) {
        this.api = api;
    }

    /** HU-14: historial con filtros. Por defecto muestra los últimos 30 días. */
    @GetMapping("/movimientos")
    public String historial(@RequestParam(required = false) String insumoId, @RequestParam(required = false) String tipo,
                            @RequestParam(required = false) String desde, @RequestParam(required = false) String hasta, Model model) {
        String desdeEfectivo = desde == null ? Datos.hace(30) : desde;
        List<Map<String, Object>> movimientos = api.lista(Ms.INSUMOS, "/api/movimientos",
                Datos.filtro("insumoId", insumoId, "tipo", tipo, "desde", desdeEfectivo, "hasta", hasta));
        List<Map<String, Object>> insumos = api.lista(Ms.INSUMOS, "/api/insumos");
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("insumos", insumos);
        model.addAttribute("nombres", Datos.indexar(insumos, "id", "nombre"));
        model.addAttribute("unidades", Datos.indexar(insumos, "id", "unidadMedida"));
        model.addAttribute("insumoSel", insumoId == null ? "" : insumoId);
        model.addAttribute("tipoSel", tipo == null ? "" : tipo);
        model.addAttribute("desde", desdeEfectivo);
        model.addAttribute("hasta", hasta == null ? "" : hasta);
        return "movimiento/lista";
    }

    @GetMapping("/movimientos/{tipo:entrada|salida}")
    @PreAuthorize(BODEGA)
    public String nuevo(@PathVariable String tipo, @RequestParam(required = false) String insumoId, Model model) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("insumoId", insumoId);
        return formulario(model, tipo, form, null);
    }

    @PostMapping("/movimientos/{tipo:entrada|salida}")
    @PreAuthorize(BODEGA)
    public String registrar(@PathVariable String tipo, @RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "cantidad");
        try {
            api.enviar(Ms.INSUMOS, HttpMethod.POST, "/api/movimientos/" + tipo, cuerpo);
            ra.addFlashAttribute("exito", "Entrada".equalsIgnoreCase(tipo) ? "Entrada registrada: el stock aumentó." : "Salida registrada: el stock disminuyó.");
            return "redirect:/movimientos";
        } catch (ApiException e) {
            return formulario(model, tipo, cuerpo, e);
        }
    }

    private String formulario(Model model, String tipo, Map<String, Object> datos, ApiException e) {
        model.addAttribute("tipo", tipo.toUpperCase());
        model.addAttribute("form", datos);
        model.addAttribute("insumos", api.lista(Ms.INSUMOS, "/api/insumos", Datos.filtro("estado", "ACTIVO")));
        if (e != null) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("errores", e.getErrores());
        }
        return "movimiento/form";
    }
}
