package com.example.mislugares;

public class Lugar {
    private String id;
    private String nombre;
    private String direccion;
    private String tipo;
    private float valoracion;

    public Lugar(String id, String nombre, String direccion, String tipo, float valoracion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.tipo = tipo;
        this.valoracion = valoracion;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTipo() { return tipo; }
    public float getValoracion() { return valoracion; }
}
