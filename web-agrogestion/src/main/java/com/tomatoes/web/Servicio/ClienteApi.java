package com.tomatoes.web.Servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomatoes.web.Excepciones.ApiException;
import com.tomatoes.web.Excepciones.ServicioNoDisponibleException;
import com.tomatoes.web.Excepciones.SesionExpiradaException;
import com.tomatoes.web.Seguridad.UsuarioSesion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Supplier;

/**
 * Único punto de comunicación REST de la web con los microservicios.
 * Agrega el token JWT de la sesión a cada petición y traduce los errores a excepciones controladas.
 */
@Service
public class ClienteApi {

    public enum Ms { EMPLEADOS, INSUMOS, COSECHAS }

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {};

    private final Map<Ms, RestClient> clientes = new EnumMap<>(Ms.class);
    private final ObjectMapper mapper;

    public ClienteApi(@Value("${servicios.empleados.url}") String empleados,
                      @Value("${servicios.insumos.url}") String insumos,
                      @Value("${servicios.cosechas.url}") String cosechas,
                      ObjectMapper mapper) {
        this.mapper = mapper;
        clientes.put(Ms.EMPLEADOS, construir(empleados));
        clientes.put(Ms.INSUMOS, construir(insumos));
        clientes.put(Ms.COSECHAS, construir(cosechas));
    }

    private RestClient construir(String base) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(10_000);
        fabrica.setReadTimeout(60_000); // el plan gratuito de Render tarda en "despertar" un servicio
        return RestClient.builder()
                .baseUrl(base)
                .requestFactory(fabrica)
                .requestInterceptor((req, cuerpo, ejecucion) -> {
                    Authentication a = SecurityContextHolder.getContext().getAuthentication();
                    if (a != null && a.getPrincipal() instanceof UsuarioSesion u) req.getHeaders().setBearerAuth(u.token());
                    return ejecucion.execute(req, cuerpo);
                })
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> { throw convertir(res); })
                .build();
    }

    // ------------------------------------------------------------------ consultas

    public List<Map<String, Object>> lista(Ms ms, String ruta) {
        return lista(ms, ruta, Map.of());
    }

    public List<Map<String, Object>> lista(Ms ms, String ruta, Map<String, String> filtros) {
        List<Map<String, Object>> r = ejecutar(() -> clientes.get(ms).get()
                .uri(u -> construirUri(u, ruta, filtros)).retrieve().body(LISTA));
        return r == null ? new ArrayList<>() : r;
    }

    public Map<String, Object> mapa(Ms ms, String ruta) {
        return ejecutar(() -> clientes.get(ms).get().uri(ruta).retrieve().body(MAPA));
    }

    // ------------------------------------------------------------------ escritura

    public Map<String, Object> enviar(Ms ms, HttpMethod metodo, String ruta, Object cuerpo) {
        return ejecutar(() -> clientes.get(ms).method(metodo).uri(ruta)
                .contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(MAPA));
    }

    /** Petición sin cuerpo (por ejemplo DELETE o PUT .../activar). */
    public void accion(Ms ms, HttpMethod metodo, String ruta) {
        ejecutar(() -> clientes.get(ms).method(metodo).uri(ruta).retrieve().toBodilessEntity());
    }

    public Map<String, Object> login(String correo, String clave) {
        return enviar(Ms.EMPLEADOS, HttpMethod.POST, "/api/auth/login", Map.of("correo", correo, "password", clave));
    }

    // ------------------------------------------------------------------ utilidades internas

    private URI construirUri(UriBuilder b, String ruta, Map<String, String> filtros) {
        b.path(ruta);
        filtros.forEach((k, v) -> { if (v != null && !v.isBlank()) b.queryParam(k, v); });
        return b.build();
    }

    private <T> T ejecutar(Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("No fue posible comunicarse con el servicio. "
                    + "Si está iniciando (plan gratuito de Render) espere unos segundos e intente de nuevo.");
        }
    }

    private RuntimeException convertir(ClientHttpResponse res) throws IOException {
        int estado = res.getStatusCode().value();
        String mensaje = "El servicio respondió con un error (" + estado + ").";
        Map<String, String> errores = new LinkedHashMap<>();
        try {
            JsonNode n = mapper.readTree(res.getBody());
            if (n.hasNonNull("mensaje")) mensaje = n.get("mensaje").asText();
            JsonNode e = n.get("errores");
            if (e != null && e.isObject()) e.fields().forEachRemaining(en -> errores.put(en.getKey(), en.getValue().asText()));
        } catch (Exception ignorada) {
            // se conserva el mensaje genérico
        }
        if (estado == 401 && haySesion()) return new SesionExpiradaException();
        if (estado == 503) return new ServicioNoDisponibleException(mensaje);
        return new ApiException(estado, mensaje, errores);
    }

    private boolean haySesion() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getPrincipal() instanceof UsuarioSesion;
    }
}
