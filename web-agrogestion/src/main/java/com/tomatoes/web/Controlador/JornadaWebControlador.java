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

/** Módulo del Operario: sus labores asignadas (HU-04) y el registro de su jornada. */
@Controller
@PreAuthorize("hasRole('OPERARIO')")
public class JornadaWebControlador {

    private final ClienteApi api;
    private final CatalogoService catalogo;

    public JornadaWebControlador(ClienteApi api, CatalogoService catalogo) {
        this.api = api;
        this.catalogo = catalogo;
    }

    @GetMapping("/jornadas")
    public String lista(@RequestParam(required = false) String cosechaId, Model model) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("fecha", Datos.hoyTexto());
        form.put("cosechaId", cosechaId);
        return vista(model, form, null);
    }

    @PostMapping("/jornadas")
    public String registrar(@RequestParam Map<String, String> p, Model model, RedirectAttributes ra) {
        Map<String, Object> cuerpo = Datos.cuerpo(p, "horas");
        try {
            api.enviar(Ms.EMPLEADOS, HttpMethod.POST, "/api/jornadas", cuerpo);
            ra.addFlashAttribute("exito", "Jornada registrada correctamente.");
            return "redirect:/jornadas";
        } catch (ApiException e) {
            return vista(model, cuerpo, e);
        }
    }

    @GetMapping("/mis-labores")
    public String misLabores(Model model) {
        List<String> avisos = new ArrayList<>();
        List<Map<String, Object>> labores = Datos.segura(() -> api.lista(Ms.COSECHAS, "/api/cosechas/mis-labores"), "cosechas", avisos);
        List<Map<String, Object>> lotes = Datos.segura(() -> api.lista(Ms.COSECHAS, "/api/lotes"), "cosechas", avisos);
        model.addAttribute("labores", labores);
        model.addAttribute("lotes", Datos.porId(lotes));
        model.addAttribute("avisos", avisos);
        return "cosecha/mis-labores";
    }

    private String vista(Model model, Map<String, Object> form, ApiException e) {
        List<String> avisos = new ArrayList<>();
        List<Map<String, Object>> jornadas = Datos.segura(() -> api.lista(Ms.EMPLEADOS, "/api/jornadas/mias"), "empleados", avisos);
        List<Map<String, Object>> labores = Datos.segura(() -> api.lista(Ms.COSECHAS, "/api/cosechas/mis-labores"), "cosechas", avisos);
        Map<String, String> lotes = catalogo.nombresLotes();
        Map<String, String> etiquetas = new LinkedHashMap<>();
        List<Map<String, Object>> activas = new ArrayList<>();
        for (Map<String, Object> c : labores) {
            String nombre = lotes.getOrDefault(String.valueOf(c.get("loteId")), "Lote") + " (inicio " + c.get("fechaInicio") + ")";
            etiquetas.put(String.valueOf(c.get("id")), nombre);
            if (!"FINALIZADA".equals(c.get("estado"))) activas.add(c);
        }
        double total = Datos.suma(jornadas, "horas");
        model.addAttribute("jornadas", jornadas);
        model.addAttribute("labores", activas);
        model.addAttribute("etiquetas", etiquetas);
        model.addAttribute("totalHoras", total);
        model.addAttribute("promedio", jornadas.isEmpty() ? 0 : total / jornadas.size());
        model.addAttribute("form", form);
        model.addAttribute("avisos", avisos);
        if (e != null) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("errores", e.getErrores());
        }
        return "jornada/lista";
    }
}
