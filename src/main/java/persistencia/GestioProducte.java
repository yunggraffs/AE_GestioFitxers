package persistencia;

import model.Producte;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestioProducte implements Gestionable {

    // Atributos
    private final File rutaProductos;
    private final File rutaSinStock;
    private final File rutaDescatalogado;
    private final int tamanoRegistro = 67;

    // Constructor
    public GestioProducte (File rutaProductos, File rutaSinStock, File rutaDescatalogado) {
        this.rutaProductos = rutaProductos;
        this.rutaSinStock = rutaSinStock;
        this.rutaDescatalogado = rutaDescatalogado;
    }

    @Override
    public int afegirProducte(Producte p) throws ProducteNoValidException {
        int codigoGenerado;

        // Validar los datos del producto
        validarDatos(p);

        // Generar código para el producto cogiendo el último código + 1
        int totalRegistros = (int) (rutaProductos.length() / tamanoRegistro);
        Producte ultimoRegistro;
        try {
            ultimoRegistro = leerProducto(new RandomAccessFile(rutaProductos, "r"), totalRegistros);
            codigoGenerado = ultimoRegistro.getCodigo() + 1;

        } catch (EOFException e) {
            // Únicamente atrapará EOFException cuando el fichero esté vacío
            codigoGenerado = 1;

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            return -1;
        }

        // Añadir el nuevo producto a productos.bin
        p.setCodigo(codigoGenerado);
        try {
            escribirProducto(new RandomAccessFile(rutaProductos, "rw"), p, totalRegistros);

        } catch (FileNotFoundException e) {
            System.err.println("Error! No se ha podido encontrar el archivo \"" +
                    rutaProductos.getPath() + "\".");
            return -1;

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            return -1;
        }

        return codigoGenerado;
    }

    @Override
    public Producte cercaPerCodi(int codigo) throws ProducteNoValidException, ProducteNoExistentException {
        Producte p;

        // Validamos el código
        if (codigo < 1) {
            throw new ProducteNoValidException("Código no válido.");
        }

        // Aprovechamos que el atributo codigo es único y secuencial para posicionarnos directamente en ese registro
        try {
            p = leerProducto(new RandomAccessFile(rutaProductos, "r"), codigo - 1);

        } catch (EOFException e) {
            throw new ProducteNoExistentException(
                    "No existe ningún producto registrado con el código \'" + codigo + "\'.");

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            return null;
        }

        return p;
    }

    @Override
    public List<Producte> cercaPerNom(String nombre) {
        int numRegistros = (int)(rutaProductos.length() / tamanoRegistro);

        try {
            for (int i = 0; i < numRegistros; i++) {
                Producte p = leerProducto(new RandomAccessFile(rutaProductos, "r"), i);
                System.out.println(p);
            }

        } catch (EOFException e) {

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        return new ArrayList<>();
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

        // Nombre
        String nombre = p.getNombre().toUpperCase();
        StringBuilder nombreBuilder = new StringBuilder(nombre);
        if (nombre.length() < 50) {
            for (int i = nombre.length(); i < 50; i++) {
                nombreBuilder.append(" ");
            }
        }
        p.setNombre(nombreBuilder.toString());

        // Precio
        if (p.getPrecio() < 0) {
            throw new ProducteNoValidException("Precio inferior a 0.");
        }

        // Stock
        if (p.getStock() < 0) {
            throw new ProducteNoValidException("Stock inferior a 0.");
        }
    }

    private Producte leerProducto(RandomAccessFile file, int posicion) throws IOException {
        // Posicionarnos en el registro deseado
        file.seek(posicion * tamanoRegistro);

        // Extraer todos los atributos del registro
        int codigo = file.readInt();

        char[] nombreChars = new char[50];
        for (int i = 0; i < nombreChars.length; i++) {
            nombreChars[i] = file.readChar();
        }
        String nombre = new String(nombreChars);
        double precio = file.readDouble();
        int stock = file.readInt();
        boolean descatalogado = file.readBoolean();
        file.close();

        return new Producte(codigo, nombre, precio, stock, descatalogado);
    }

    private void escribirProducto(RandomAccessFile file, Producte p, int posicion) throws IOException {
        // Se posiciona en la posición deseada
        file.seek(posicion * tamanoRegistro);

        // Se escribe el nuevo registro
        file.writeInt(p.getCodigo());

        char[] nombreChar = p.getNombre().toCharArray();
        for (char letra : nombreChar) {
            file.writeChar(letra);
        }

        file.writeDouble(p.getPrecio());
        file.writeInt(p.getStock());
        file.writeBoolean(p.isDescatalogado());

        file.close();
        System.out.println("Producto añadido con éxito.");
    }

    //==============================================
    public void leerFicheroCompleto() {
        try (FileInputStream fis = new FileInputStream(rutaProductos)) {
            int b;
            while ((b = fis.read()) != -1) {
                System.out.print(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
