package model;

import java.util.Objects;

public class Producte {

    // Atributos
    private int codigo = -1;                // 4 bytes
    private String nombre;                  // 52 bytes (2 bytes + (1 byte * carácter))
    private double precio;                  // 8 bytes
    private int stock;                      // 4 bytes
    private boolean descatalogado = false;  // 1 byte
    // En total, un registro ocupa 69 bytes

    // Constructores
    public Producte() {}

    public Producte(String nombre, double precio, int stock, boolean descatalogado) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.descatalogado = descatalogado;
    }

    public Producte(int codigo, String nombre, double precio, int stock, boolean descatalogado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.descatalogado = descatalogado;
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
