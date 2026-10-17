package com.tomatoes.web.Util;

import com.tomatoes.web.Excepciones.ServicioNoDisponibleException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;

/** Funciones auxiliares para armar filtros, cuerpos JSON a partir de formularios y datos de las vistas. */
public final class Datos {

    private Datos() {}

    public static LocalDate hoy() {
        return LocalDate.now(ZoneId.of("America/Bogota"));
    }

    public static String hoyTexto() { return hoy().toString(); }

    public static String hace(int dias) { return hoy().minusDays(dias).toString(); }

    /** Filtros de consulta: pares clave, valor (admite valores nulos, que luego se ignoran). */
    public static Map<String, String> filtro(String... pares) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) m.put(pares[i], pares[i + 1]);
        return m;
    }

    /** Convierte los parámetros del formulario en el cuerpo JSON: vacío -> null y las claves indicadas -> número. */
    public static Map<String, Object> cuerpo(Map<String, String> p, String... numericos) {
        Set<String> nums = new HashSet<>(Arrays.asList(numericos));
        Map<String, Object> r = new LinkedHashMap<>();
        p.forEach((k, v) -> {
            if (k.startsWith("_")) return;
            if (v == null || v.isBlank()) { r.put(k, null); return; }
            if (nums.contains(k)) {
                try { r.put(k, Double.valueOf(v.trim().replace(',', '.'))); }
                catch (NumberFormatException e) { r.put(k, v.trim()); }
            } else {
                r.put(k, v.trim());
            }
        });
        return r;
    }

    /** Id -> nombre a partir de una lista de documentos. */
    public static Map<String, String> indexar(List<Map<String, Object>> filas, String claveId, String claveNombre) {
        Map<String, String> m = new LinkedHashMap<>();
        for (Map<String, Object> f : filas) m.put(String.valueOf(f.get(claveId)), String.valueOf(f.get(claveNombre)));
        return m;
    }

    public static double numero(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0;
    }

    /** Suma el costo (cantidad x costo unitario) de los insumos usados en una cosecha. */
    public static double costoInsumos(Object insumosUsados) {
        double total = 0;
        if (insumosUsados instanceof List<?> lista) {
            for (Object o : lista) {
                if (o instanceof Map<?, ?> m) total += numero(m.get("cantidad")) * numero(m.get("costoUnitario"));
            }
        }
        return total;
    }

    /** Agrega a cada fila el porcentaje de su valor respecto al máximo (para dibujar barras). */
    public static void porcentajes(List<Map<String, Object>> filas, String claveValor, String clavePct) {
        double max = filas.stream().mapToDouble(f -> numero(f.get(claveValor))).max().orElse(0);
        for (Map<String, Object> f : filas) f.put(clavePct, max <= 0 ? 0 : Math.round(numero(f.get(claveValor)) / max * 100));
    }

    /** Ejecuta una consulta; si el servicio no responde, devuelve lista vacía y registra un aviso para mostrar en pantalla. */
    public static <T> List<T> segura(Supplier<List<T>> consulta, String servicio, List<String> avisos) {
        try {
            return consulta.get();
        } catch (ServicioNoDisponibleException e) {
            avisos.add("El servicio de " + servicio + " no responde en este momento; algunos datos no se muestran.");
            return new ArrayList<>();
        }
    }

    public static List<String> textos(Object o) {
        List<String> r = new ArrayList<>();
        if (o instanceof List<?> lista) for (Object x : lista) r.add(String.valueOf(x));
        return r;
    }

    public static Map<String, Map<String, Object>> porId(List<Map<String, Object>> filas) {
        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        for (Map<String, Object> f : filas) m.put(String.valueOf(f.get("id")), f);
        return m;
    }

    public static double suma(List<Map<String, Object>> filas, String clave) {
        return filas.stream().mapToDouble(f -> numero(f.get(clave))).sum();
    }
}
