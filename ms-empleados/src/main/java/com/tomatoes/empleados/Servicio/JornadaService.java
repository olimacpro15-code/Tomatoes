package com.tomatoes.empleados.Servicio;

import com.tomatoes.empleados.Dto.JornadaRequest;
import com.tomatoes.empleados.Dto.ResumenHoras;
import com.tomatoes.empleados.Entidades.Empleado;
import com.tomatoes.empleados.Entidades.Jornada;
import com.tomatoes.empleados.Repositorio.EmpleadoRepository;
import com.tomatoes.empleados.Repositorio.JornadaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JornadaService {

    private static final LocalDate MINIMA = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAXIMA = LocalDate.of(2100, 12, 31);

    private final JornadaRepository repo;
    private final EmpleadoRepository empleados;

    public JornadaService(JornadaRepository repo, EmpleadoRepository empleados) {
        this.repo = repo;
        this.empleados = empleados;
    }

    public Jornada registrar(String empleadoId, JornadaRequest r) {
        Jornada j = new Jornada();
        j.setEmpleadoId(empleadoId);
        j.setFecha(r.fecha());
        j.setHoras(r.horas());
        j.setLabor(r.labor().trim());
        j.setCosechaId(r.cosechaId() == null || r.cosechaId().isBlank() ? null : r.cosechaId());
        j.setObservaciones(r.observaciones());
        return repo.save(j);
    }

    public List<Jornada> deEmpleado(String empleadoId) {
        return repo.findByEmpleadoIdOrderByFechaDesc(empleadoId);
    }

    public List<Jornada> listar(String empleadoId, LocalDate desde, LocalDate hasta) {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "fecha")).stream()
                .filter(j -> j.getFecha() != null)
                .filter(j -> empleadoId == null || empleadoId.isBlank() || empleadoId.equals(j.getEmpleadoId()))
                .filter(j -> desde == null || !j.getFecha().isBefore(desde))
                .filter(j -> hasta == null || !j.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    /** Total de horas laboradas por empleado en el periodo (RF-18). */
    public List<ResumenHoras> reporteHoras(LocalDate desde, LocalDate hasta) {
        List<Jornada> jornadas = listar(null, desde, hasta);
        Map<String, List<Jornada>> porEmpleado = jornadas.stream().collect(Collectors.groupingBy(Jornada::getEmpleadoId));
        Map<String, String> nombres = new HashMap<>();
        for (Empleado e : empleados.findAllById(porEmpleado.keySet())) {
            nombres.put(e.getId(), e.getNombres() + " " + e.getApellidos());
        }
        return porEmpleado.entrySet().stream()
                .map(en -> new ResumenHoras(en.getKey(), nombres.getOrDefault(en.getKey(), "Empleado eliminado"),
                        en.getValue().stream().mapToDouble(Jornada::getHoras).sum(), en.getValue().size()))
                .sorted(Comparator.comparingDouble(ResumenHoras::totalHoras).reversed())
                .collect(Collectors.toList());
    }
}
