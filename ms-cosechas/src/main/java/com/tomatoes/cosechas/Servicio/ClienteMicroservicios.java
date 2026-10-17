package com.tomatoes.cosechas.Servicio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomatoes.cosechas.Excepciones.NegocioException;
import com.tomatoes.cosechas.Excepciones.NoAutorizadoException;
import com.tomatoes.cosechas.Excepciones.ServicioNoDisponibleException;
import com.tomatoes.cosechas.Seguridad.UsuarioAutenticado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Comunicación REST con otros microservicios (patrón database-per-service: nunca se accede a su base de datos).
 * Reenvía el token JWT del usuario que hizo la petición original.
 */
@Component
public class ClienteMicroservicios {

    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {};
    private final RestClient empleados;
    private final RestClient insumos;
    private final ObjectMapper mapper = new ObjectMapper();

    public ClienteMicroservicios(@Value("${servicios.empleados.url}") String urlEmpleados,
                                 @Value("${servicios.insumos.url}") String urlInsumos) {
        this.empleados = construir(urlEmpleados);
        this.insumos = construir(urlInsumos);
    }

    private RestClient construir(String base) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(10_000);
        fabrica.setReadTimeout(60_000); // el plan gratuito de Render tarda en "despertar" un servicio
        return RestClient.builder().baseUrl(base).requestFactory(fabrica).build();
    }

    public Map<String, Object> empleado(String id) {
        return ejecutar("empleados", () -> empleados.get().uri("/api/empleados/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()).retrieve().body(MAPA));
    }

    public Map<String, Object> insumo(String id) {
        return ejecutar("insumos", () -> insumos.get().uri("/api/insumos/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()).retrieve().body(MAPA));
    }

    /** Registra una salida de inventario asociada a la cosecha; ms-insumos descuenta el stock. */
    public Map<String, Object> consumirInsumo(String insumoId, double cantidad, String cosechaId) {
        Map<String, Object> cuerpo = Map.of("insumoId", insumoId, "cantidad", cantidad, "cosechaId", cosechaId,
                "observacion", "Consumo en cosecha " + cosechaId);
        return ejecutar("insumos", () -> insumos.post().uri("/api/movimientos/consumo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token())
                .contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(MAPA));
    }

    private String token() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof UsuarioAutenticado u) return u.token();
        throw new NoAutorizadoException("Sesión no válida.");
    }

    private <T> T ejecutar(String servicio, Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (HttpClientErrorException e) {
            throw new NegocioException(mensajeDe(e));
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new ServicioNoDisponibleException("El servicio de " + servicio
                    + " no está disponible en este momento. Intente de nuevo en unos segundos.");
        }
    }

    private String mensajeDe(HttpClientErrorException e) {
        try {
            String m = mapper.readTree(e.getResponseBodyAsString()).path("mensaje").asText("");
            if (!m.isBlank()) return m;
        } catch (Exception ignorada) {
            // se usa el mensaje genérico
        }
        return "El servicio externo rechazó la operación (" + e.getStatusCode().value() + ").";
    }
}
