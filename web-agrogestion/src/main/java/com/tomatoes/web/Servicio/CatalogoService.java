package com.tomatoes.web.Servicio;

import com.tomatoes.web.Excepciones.ApiException;
import com.tomatoes.web.Excepciones.ServicioNoDisponibleException;
import com.tomatoes.web.Servicio.ClienteApi.Ms;
import com.tomatoes.web.Util.Datos;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tablas id -> nombre para mostrar nombres en lugar de identificadores. Si un servicio falla, devuelve un mapa vacío. */
@Service
public class CatalogoService {

    private final ClienteApi api;

    public CatalogoService(ClienteApi api) {
        this.api = api;
    }

    public Map<String, String> nombresLotes() {
        return indexar(Ms.COSECHAS, "/api/lotes", "nombre", null);
    }

    public Map<String, String> nombresInsumos() {
        return indexar(Ms.INSUMOS, "/api/insumos", "nombre", null);
    }

    public Map<String, String> nombresProveedores() {
        return indexar(Ms.INSUMOS, "/api/proveedores", "nombre", null);
    }

    public Map<String, String> nombresEmpleados() {
        return indexar(Ms.EMPLEADOS, "/api/empleados", "nombres", "apellidos");
    }

    private Map<String, String> indexar(Ms ms, String ruta, String clave1, String clave2) {
        Map<String, String> m = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> filas = api.lista(ms, ruta);
            for (Map<String, Object> f : filas) {
                String nombre = String.valueOf(f.get(clave1)) + (clave2 != null ? " " + f.get(clave2) : "");
                m.put(String.valueOf(f.get("id")), nombre);
            }
        } catch (ApiException | ServicioNoDisponibleException e) {
            // el rol actual no tiene acceso o el servicio no responde: las vistas muestran el identificador
        }
        return m;
    }
}
