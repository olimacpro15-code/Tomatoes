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
public class CosechaWebControlador {

    private static final String SUP = "hasRole('SUPERVISOR')";
    private final ClienteApi api;
    private final CatalogoService catalogo;

    public CosechaWebControlador(ClienteApi api, CatalogoService catalogo) {
        this.api = api;
        this.catalogo = catalogo;
    }

    @GetMapping("/cosechas")
    public String lista(@RequestParam(required = false) String estado, @RequestParam(required = false) String loteId, Model model) {
        List<Map<String, Object>> lotes = api.lista(Ms.COSECHAS, "/api/lotes");
        model.addAttribute("cosechas", api.lista(Ms.COSECHAS, "/api/cosechas", Datos.filtro("estado", estado, "loteId", loteId)));
        model.addAttribute("lotes", lotes);
        model.addAttribute("nombresLotes", Datos.indexar(lotes, "id", "nombre"));
        model.addAttribute("estadoSel", estado == null ? "" : estado);
        model.addAttribute("loteSel", loteId == null ? "" : loteId);
        return "cosecha/lista";
    }

    @GetMapping("/cosechas/nueva")
    @PreAuthorize(SUP)
    public String nueva(Model model) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("fechaInicio", Datos.hoyTexto());
        return formulario(model, form, null);
    }

    @PostMapping("/cosechas")
    @PreAuthorize(SUP)
    public String crear(@RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p);
        try {
            Map<String, Object> creada = api.enviar(Ms.COSECHAS, HttpMethod.POST, "/api/cosechas", cuerpo);
            ra.addFlashAttribute("exito", "Cosecha creada. Ahora puede asignar empleados y registrar insumos.");
            return "redirect:/cosechas/" + creada.get("id");
        } catch (ApiException e) {
            return formulario(model, cuerpo, e);
        }
    }

    @GetMapping("/cosechas/{id}")
    public String detalle(@PathVariable String id, Model model) {
        Map<String, Object> cosecha = api.mapa(Ms.COSECHAS, "/api/cosechas/" + id);
        Map<String, Object> lote = api.mapa(Ms.COSECHAS, "/api/lotes/" + cosecha.get("loteId"));
        boolean editable = !"FINALIZADA".equals(cosecha.get("estado"));
        List<String> avisos = new ArrayList<>();
        Set<String> asignados = new HashSet<>(Datos.textos(cosecha.get("empleadosIds")));
        List<Map<String, Object>> disponibles = new ArrayList<>();
        List<Map<String, Object>> insumos = new ArrayList<>();
        if (editable) {
            List<Map<String, Object>> activos = Datos.segura(() -> api.lista(Ms.EMPLEADOS, "/api/empleados", Datos.filtro("estado", "ACTIVO")), "empleados", avisos);
            activos.stream().filter(e -> !asignados.contains(String.valueOf(e.get("id")))).forEach(disponibles::add);
            insumos = Datos.segura(() -> api.lista(Ms.INSUMOS, "/api/insumos", Datos.filtro("estado", "ACTIVO")), "insumos", avisos);
        }
        model.addAttribute("cosecha", cosecha);
        model.addAttribute("lote", lote);
        model.addAttribute("editable", editable);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("insumos", insumos);
        model.addAttribute("nombresEmpleados", catalogo.nombresEmpleados());
        model.addAttribute("costoTotal", Datos.costoInsumos(cosecha.get("insumosUsados")));
        model.addAttribute("avisos", avisos);
        return "cosecha/detalle";
    }

    @PostMapping("/cosechas/{id}/estado")
    @PreAuthorize(SUP)
    public String estado(@PathVariable String id, @RequestParam Map<String, String> p, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "cantidadKg");
        return Accion.hacer(ra, "/cosechas/" + id,
                () -> api.enviar(Ms.COSECHAS, HttpMethod.PUT, "/api/cosechas/" + id + "/estado", cuerpo), "Cosecha actualizada.");
    }

    @PostMapping("/cosechas/{id}/empleados")
    @PreAuthorize(SUP)
    public String asignar(@PathVariable String id, @RequestParam(required = false) List<String> empleadosIds, RedirectAttributes ra) {
        if (empleadosIds == null || empleadosIds.isEmpty()) {
            ra.addFlashAttribute("error", "Seleccione al menos un empleado.");
            return "redirect:/cosechas/" + id;
        }
        return Accion.hacer(ra, "/cosechas/" + id,
                () -> api.enviar(Ms.COSECHAS, HttpMethod.POST, "/api/cosechas/" + id + "/empleados", Map.of("empleadosIds", empleadosIds)),
                "Empleados asignados a la cosecha.");
    }

    @PostMapping("/cosechas/{id}/empleados/{empleadoId}/quitar")
    @PreAuthorize(SUP)
    public String quitar(@PathVariable String id, @PathVariable String empleadoId, RedirectAttributes ra) {
        return Accion.hacer(ra, "/cosechas/" + id,
                () -> api.accion(Ms.COSECHAS, HttpMethod.DELETE, "/api/cosechas/" + id + "/empleados/" + empleadoId), "Empleado retirado de la cosecha.");
    }

    @PostMapping("/cosechas/{id}/insumos")
    @PreAuthorize(SUP)
    public String registrarInsumo(@PathVariable String id, @RequestParam Map<String, String> p, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "cantidad");
        return Accion.hacer(ra, "/cosechas/" + id,
                () -> api.enviar(Ms.COSECHAS, HttpMethod.POST, "/api/cosechas/" + id + "/insumos", cuerpo),
                "Insumo registrado y descontado del inventario.");
    }

    private String formulario(Model model, Map<String, Object> datos, ApiException e) {
        model.addAttribute("cosecha", datos);
        model.addAttribute("lotes", api.lista(Ms.COSECHAS, "/api/lotes", Datos.filtro("estado", "ACTIVO")));
        if (e != null) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("errores", e.getErrores());
        }
        return "cosecha/form";
    }
}
