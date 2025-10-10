package persistencia;

import model.Producte;

import java.util.List;

public interface Gestionable {

    int afegirProducte(Producte p) throws ProducteNoValidException;

    Producte cercaPerCodi(int codigo)
            throws ProducteNoValidException, ProducteNoExistentException;

    List<Producte> cercaPerNom(String nombre);

    List<Producte> cercaSenseStock();

    List<Producte> cercaDescatalogats();

    void exportarSenseStock();

    void exportarDescatalogats();

    void modificarProducte(Producte p)
            throws ProducteNoValidException, ProducteNoExistentException;

    void modificarStock(
            int codigo, int cantidad, boolean incrementar)
            throws ProducteNoExistentException, StockNoValidException;

    void descatalogarProducte(int codigo)
            throws ProducteNoExistentException;

    void esborrarDescatalogats();

}
