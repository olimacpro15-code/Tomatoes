package com.tomatoes.insumos.Entidades;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movimientos")
public class Movimiento {
    @Id
    private String id;
    @Indexed
    private String insumoId;
    private TipoMovimiento tipo;
    private double cantidad;
    private LocalDateTime fecha;
    private String cosechaId;
    private String usuarioId;
    private String usuarioNombre;
    private String observacion;

    public Movimiento() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInsumoId() { return insumoId; }
    public void setInsumoId(String insumoId) { this.insumoId = insumoId; }
    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getCosechaId() { return cosechaId; }
    public void setCosechaId(String cosechaId) { this.cosechaId = cosechaId; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
