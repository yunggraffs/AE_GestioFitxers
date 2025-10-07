package model;

import java.util.Objects;

public class Producte {

    // Atributos
    private int codigo = -1;
    private boolean descatalogado = false;
    private String nombre;
    private double precio;
    private int stock;

    // Constructores
    public Producte() {}

    public Producte(String nombre, double precio, int stock,
                    boolean descatalogado) {
        this.descatalogado = descatalogado;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y Setters
    public int getCodigo() {
        return codigo;
    }

    public boolean isDescatalogado() {
        return descatalogado;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public void setDescatalogado(boolean descatalogado) {
        this.descatalogado = descatalogado;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Hashcode y Equals
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Producte producto = (Producte) o;
        return codigo == producto.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    // ToString
    @Override
    public String toString() {
        return "Producto{" +
                "codigo=" + codigo +
                ", descatalogado=" + descatalogado +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                '}';
    }
}
