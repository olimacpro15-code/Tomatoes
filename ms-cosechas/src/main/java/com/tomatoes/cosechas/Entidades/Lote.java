package com.tomatoes.cosechas.Entidades;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lotes")
public class Lote {
    @Id
    private String id;
    @Indexed(unique = true)
    private String nombre;
    private double areaHectareas;
    private String ubicacion;
    private String cultivo;
    private Estado estado;

    public Lote() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getAreaHectareas() { return areaHectareas; }
    public void setAreaHectareas(double areaHectareas) { this.areaHectareas = areaHectareas; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getCultivo() { return cultivo; }
    public void setCultivo(String cultivo) { this.cultivo = cultivo; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
