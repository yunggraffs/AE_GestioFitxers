package persistencia;

import model.Producte;

import java.util.List;

public class GestioProducte implements Gestionable{

    @Override
    public int afegirProducte(Producte p) throws ProducteNoValidException {
        int codigoGenerado = -1;

        // Validar los datos del producto
        validarDatos(p);

        // Generar codigo para el producto

        return codigoGenerado;
    }

    @Override
    public Producte cercaPerCodi(int codigo) throws ProducteNoValidException, ProducteNoExistentException {
        return null;
    }

    @Override
    public List<Producte> cercaPerNom(String nombre) {
        return List.of();
    }

    @Override
    public List<Producte> cercaSenseStock() {
        return List.of();
    }

    @Override
    public List<Producte> cercaDescatalogats() {
        return List.of();
    }

    @Override
    public void exportarSenseStock() {

    }

    @Override
    public void exportarDescatalogats() {

    }

    @Override
    public void modificarProducte(Producte p) throws ProducteNoValidException, ProducteNoExistentException {

    }

    @Override
    public void modificarStock(int codigo, int cantidad, boolean incrementar) throws ProducteNoExistentException, StockNoValidException {

    }

    @Override
    public void descatalogarProducte(int codigo) throws ProducteNoExistentException {

    }

    @Override
    public void esborrarDescatalogats() {

    }

    private void validarDatos(Producte p) throws ProducteNoValidException {
        StringBuilder nombreBuilder = new StringBuilder();
        
    }

}
