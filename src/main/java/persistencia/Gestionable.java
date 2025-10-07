package persistencia;

import model.Producte;

import java.util.List;

public interface Gestionable {

    public int afegirProducte(Producte p) throws ProducteNoValidException;

    public Producte cercaPerCodi(int codigo)
            throws ProducteNoValidException, ProducteNoExistentException;

    public List<Producte> cercaPerNom(String nombre);

    public List<Producte> cercaSenseStock();

    public List<Producte> cercaDescatalogats();

    public void exportarSenseStock();

    public void exportarDescatalogats();

    public void modificarProducte(Producte p)
            throws ProducteNoValidException, ProducteNoExistentException;

    public void modificarStock(
            int codigo, int cantidad, boolean incrementar)
            throws ProducteNoExistentException, StockNoValidException;

    public void descatalogarProducte(int codigo)
            throws ProducteNoExistentException;

    public void esborrarDescatalogats();

}
