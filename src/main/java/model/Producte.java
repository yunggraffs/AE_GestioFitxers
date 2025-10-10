package model;

import java.util.Objects;

/**
 * Clase que representa un producto con sus propiedades principales:
 * código, nombre, precio, stock y estado (descatalogado o no).
 * <p>
 * Cada producto se almacena como un registro de 69 bytes en un fichero binario.
 * </p>
 */
public class Producte {

    /** Código único del producto. */
    private int codigo = -1;
    /** Nombre del producto (máximo 50 caracteres). */
    private String nombre;
    /** Precio del producto. */
    private double precio;
    /** Unidades disponibles en stock. */
    private int stock;
    /** Indica si el producto está descatalogado. */
    private boolean descatalogado = false;

    /**
     * Constructor vacío.
     * Permite crear un producto sin inicializar sus atributos.
     */
    public Producte() {}

    /**
     * Constructor que crea un producto sin código (se genera posteriormente).
     *
     * @param nombre        nombre del producto.
     * @param precio        precio del producto.
     * @param stock         cantidad disponible.
     * @param descatalogado estado de descatalogación.
     */
    public Producte(String nombre, double precio, int stock, boolean descatalogado) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.descatalogado = descatalogado;
    }

    /**
     * Constructor completo (con código asignado).
     *
     * @param codigo        código único del producto.
     * @param nombre        nombre del producto.
     * @param precio        precio del producto.
     * @param stock         cantidad disponible.
     * @param descatalogado estado de descatalogación.
     */
    public Producte(int codigo, String nombre, double precio, int stock, boolean descatalogado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.descatalogado = descatalogado;
    }


    // ---------------- Getters y Setters ----------------

    /** @return código del producto. */
    public int getCodigo() { return codigo; }

    /** @return true si el producto está descatalogado. */
    public boolean isDescatalogado() { return descatalogado; }

    /** @return nombre del producto. */
    public String getNombre() { return nombre; }

    /** @return precio del producto. */
    public double getPrecio() { return precio; }

    /** @return unidades en stock. */
    public int getStock() { return stock; }

    /** @param codigo nuevo código del producto. */
    public void setCodigo(int codigo) { this.codigo = codigo; }

    /** @param nombre nuevo nombre del producto. */
    public void setNombre(String nombre) { this.nombre = nombre; }

    // ---------------- Métodos de utilidad ----------------

    /**
     * Compara productos según su código.
     *
     * @param o objeto a comparar.
     * @return {@code true} si los códigos son iguales.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Producte producto = (Producte) o;
        return codigo == producto.codigo;
    }

    /** @return hash basado en el código del producto. */
    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    /** @return representación textual legible del producto. */
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
