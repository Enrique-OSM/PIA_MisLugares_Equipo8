package com.example.mislugares;

public class Lugar {
    private String id;
    private String nombre;
    private String direccion;
    private Double distancia;
    private String tipo;
    private float valoracion;

    public Lugar(String id, String nombre, String direccion, String tipo, float valoracion, Double distancia) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.distancia = distancia;
        this.tipo = tipo;
        this.valoracion = valoracion;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public Double getDistancia() { return distancia; }
    public String getTipo() { return tipo; }
    public float getValoracion() { return valoracion; }
}
