package com.tomatoes.cosechas.Entidades;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosechas")
public class Cosecha {
    @Id
    private String id;
    @Indexed
    private String loteId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoCosecha estado;
    private Double cantidadKg;
    private Calidad calidad;
    private List<String> empleadosIds = new ArrayList<>();
    private List<InsumoUsado> insumosUsados = new ArrayList<>();

    public Cosecha() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLoteId() { return loteId; }
    public void setLoteId(String loteId) { this.loteId = loteId; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public EstadoCosecha getEstado() { return estado; }
    public void setEstado(EstadoCosecha estado) { this.estado = estado; }
    public Double getCantidadKg() { return cantidadKg; }
    public void setCantidadKg(Double cantidadKg) { this.cantidadKg = cantidadKg; }
    public Calidad getCalidad() { return calidad; }
    public void setCalidad(Calidad calidad) { this.calidad = calidad; }
    public List<String> getEmpleadosIds() { return empleadosIds; }
    public void setEmpleadosIds(List<String> empleadosIds) { this.empleadosIds = empleadosIds; }
    public List<InsumoUsado> getInsumosUsados() { return insumosUsados; }
    public void setInsumosUsados(List<InsumoUsado> insumosUsados) { this.insumosUsados = insumosUsados; }
}
