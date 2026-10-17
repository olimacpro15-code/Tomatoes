package com.tomatoes.empleados.Entidades;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jornadas")
public class Jornada {
    @Id
    private String id;
    @Indexed
    private String empleadoId;
    private LocalDate fecha;
    private double horas;
    private String labor;
    private String cosechaId;
    private String observaciones;

    public Jornada() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(String empleadoId) { this.empleadoId = empleadoId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public double getHoras() { return horas; }
    public void setHoras(double horas) { this.horas = horas; }
    public String getLabor() { return labor; }
    public void setLabor(String labor) { this.labor = labor; }
    public String getCosechaId() { return cosechaId; }
    public void setCosechaId(String cosechaId) { this.cosechaId = cosechaId; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
