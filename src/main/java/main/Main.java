package main;

import model.Producte;
import persistencia.GestioProducte;
import persistencia.ProducteNoExistentException;
import persistencia.ProducteNoValidException;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        GestioProducte gp = new GestioProducte(
                new File("ficheros/productos.bin"),
                new File("ficheros/sin-stock.bin"),
                new File("ficheros/descatalogado.bin"));

        // Probar afegirProducte()
//        try {
//            int codigo;
//            codigo = gp.afegirProducte(new Producte("Producto1", 23.99, 100, false));
//            System.out.printf("Código retornado: %d\n", codigo);
//
//            codigo = gp.afegirProducte(new Producte("Producto1", 23.99, 100, false));
//            System.out.printf("Código retornado: %d\n", codigo);
//
//            codigo = gp.afegirProducte(new Producte("Producto2", 23.99, 100, false));
//            System.out.printf("Código retornado: %d\n", codigo);
//
//            codigo = gp.afegirProducte(new Producte("Producto3", -100, 100, false));
//            System.out.printf("Código retornado: %d\n", codigo);
//
//        } catch (ProducteNoValidException e) {
//            System.err.println("Error! " + e.getMessage());
//        }

        // Probar cercaPerCodi()
//        try {
//            System.out.println(gp.cercaPerCodi(1));
////            System.out.println(gp.cercaPerCodi(-2));
//            System.out.println(gp.cercaPerCodi(10));
//        } catch (ProducteNoValidException | ProducteNoExistentException e) {
//            System.err.println("Error! " + e.getMessage());
//        }

        // Probar cercaPerNom()
//        for (Producte p : gp.cercaPerNom("producto1")) {
//            System.out.println(p);
//        }
    }
}
