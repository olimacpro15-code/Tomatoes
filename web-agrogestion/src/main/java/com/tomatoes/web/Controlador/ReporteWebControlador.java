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
public class ReporteWebControlador {

    private final ClienteApi api;

    public ReporteWebControlador(ClienteApi api) {
        this.api = api;
    }

    /** HU-17: producción por lote y rango de fechas. */
    @GetMapping("/reportes/produccion")
    @PreAuthorize("hasAnyRole('GERENTE','SUPERVISOR','ADMINISTRADOR')")
    public String produccion(@RequestParam(required = false) String desde, @RequestParam(required = false) String hasta,
                             @RequestParam(required = false) String loteId, Model model) {
        String desdeEfectivo = desde == null ? Datos.hace(90) : desde;
        List<Map<String, Object>> filas = api.lista(Ms.COSECHAS, "/api/reportes/produccion",
                Datos.filtro("desde", desdeEfectivo, "hasta", hasta, "loteId", loteId));
        List<Map<String, Object>> lotes = api.lista(Ms.COSECHAS, "/api/lotes");
        Map<String, Map<String, Object>> porId = Datos.porId(lotes);
        for (Map<String, Object> f : filas) {
            Map<String, Object> lote = porId.get(String.valueOf(f.get("loteId")));
            double area = lote == null ? 0 : Datos.numero(lote.get("areaHectareas"));
            f.put("kgPorHectarea", area > 0 ? Datos.numero(f.get("totalKg")) / area : 0);
        }
        Datos.porcentajes(filas, "totalKg", "pct");
        model.addAttribute("filas", filas);
        model.addAttribute("lotes", lotes);
        model.addAttribute("totalKg", Datos.suma(filas, "totalKg"));
        model.addAttribute("totalCosto", Datos.suma(filas, "costoInsumos"));
        model.addAttribute("totalCosechas", Datos.suma(filas, "cosechas"));
        model.addAttribute("desde", desdeEfectivo);
        model.addAttribute("hasta", hasta == null ? "" : hasta);
        model.addAttribute("loteSel", loteId == null ? "" : loteId);
        return "reporte/produccion";
    }

    /** HU-18: horas laboradas por empleado. */
    @GetMapping("/reportes/jornadas")
    @PreAuthorize("hasAnyRole('GERENTE','ADMINISTRADOR')")
    public String jornadas(@RequestParam(required = false) String desde, @RequestParam(required = false) String hasta, Model model) {
        String desdeEfectivo = desde == null ? Datos.hace(30) : desde;
        List<Map<String, Object>> filas = api.lista(Ms.EMPLEADOS, "/api/jornadas/reporte",
                Datos.filtro("desde", desdeEfectivo, "hasta", hasta));
        Datos.porcentajes(filas, "totalHoras", "pct");
        model.addAttribute("filas", filas);
        model.addAttribute("totalHoras", Datos.suma(filas, "totalHoras"));
        model.addAttribute("desde", desdeEfectivo);
        model.addAttribute("hasta", hasta == null ? "" : hasta);
        return "reporte/jornadas";
    }
}
