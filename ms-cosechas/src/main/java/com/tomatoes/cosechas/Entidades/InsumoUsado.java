package com.tomatoes.cosechas.Entidades;

public class InsumoUsado {
    private String insumoId;
    private String insumoNombre;
    private double cantidad;
    private double costoUnitario;

    public InsumoUsado() {}

    public String getInsumoId() { return insumoId; }
    public void setInsumoId(String insumoId) { this.insumoId = insumoId; }
    public String getInsumoNombre() { return insumoNombre; }
    public void setInsumoNombre(String insumoNombre) { this.insumoNombre = insumoNombre; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }
}
