package main;

import model.Producte;
import persistencia.GestioProducte;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        GestioProducte gp = new GestioProducte(
                new File("ficheros/productos.bin"),
                new File("ficheros/sin-stock.bin"),
                new File("ficheros/descatalogado.bin"));

//        gp.leerFicheroCompleto();

        int codigo = gp.afegirProducte(new Producte("Producto1", 23.99, 100, false));
        System.out.println(codigo);

        Producte p = gp.cercaPerCodi(1);
        System.out.println(p);

        gp.afegirProducte(new Producte("Producto2", 23.99, 100, false));
        for (Producte producte : gp.cercaPerNom("producto1")) {
            
        }

    }
}
