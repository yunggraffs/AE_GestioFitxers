package persistencia;

import model.Producte;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestioProducte implements Gestionable {

    // Atributos
    private final File rutaProductos;
    private final File rutaSinStock;
    private final File rutaDescatalogado;
    private final int tamanoRegistro = 69;

    // Constructor
    public GestioProducte (File rutaProductos, File rutaSinStock, File rutaDescatalogado) {
        this.rutaProductos = rutaProductos;
        this.rutaSinStock = rutaSinStock;
        this.rutaDescatalogado = rutaDescatalogado;
    }

    @Override
    public int afegirProducte(Producte p) throws ProducteNoValidException {
        int codigoGenerado;

        // Validar integridad del fichero productos.bin
        if (!validarFichero(rutaProductos)) {
            return -1;
        }

        // Validar los datos del producto
        validarDatos(p);

        // Generar código para el producto cogiendo el último código + 1
        int totalRegistros = (int) (rutaProductos.length() / tamanoRegistro);

        // En caso de que el total de registros sea 0 se asigna directamente el código 1
        if (totalRegistros == 0) {
            codigoGenerado = 1;

        } else {
            try (RandomAccessFile raf = new RandomAccessFile(rutaProductos, "r")) {
                Producte ultimoRegistro = leerProducto(raf, totalRegistros - 1);
                codigoGenerado = ultimoRegistro.getCodigo() + 1;

            } catch (IOException e) {
                System.err.println("Error! " + e.getMessage());
                return -1;
            }
        }

        // Añadir el nuevo producto a productos.bin
        p.setCodigo(codigoGenerado);
        try (RandomAccessFile raf = new RandomAccessFile(rutaProductos, "rw")){
            escribirProducto(raf, p);
            System.out.printf("[%d] %s añadido correctamente.\n", p.getCodigo(), p.getNombre().strip());

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
        Producte p = null;

        // Validamos el código
        if (codigo < 1) {
            throw new ProducteNoValidException("Código no válido.");
        }

        /*
        Sabiendo el tamaño en bytes de cada registro, recorremos de código en código hasta que coincida, comenzando
        desde la posición 0 y sumando el tamaño del registro en cada iteración. En el momento que encontremos un
        código coincidente, llamaremos al metodo leerProducto() indicandole la posición inicial, en este caso la misma
        del código. Debido a que el código es un campo con valores únicos, en caso de encontrar un código coincidente
        hacemos un break para dejar de iterar. Como la instancia de RandomAccessFile está en el try-with-resources se
        cerrará de manera automática.
         */
        try (RandomAccessFile raf = new RandomAccessFile(rutaProductos, "r")) {
            int posCodigo = 0;
            int codigoRegistro;

            // Iteramos de código en código
            for (int i = posCodigo; i <= raf.length(); i += tamanoRegistro) {
                // Posicionar el pointer, recoger el código y comprobar si es el que buscamos
                raf.seek(i);
                codigoRegistro = raf.readInt();

                // Si encontramos el código coincidente, leemos el producto completo
                if (codigoRegistro == codigo) {
                    p = leerProducto(raf, (i / tamanoRegistro));
                    break;
                }
            }

        // En caso de capturar EOFException significará que no existe ningún producto con ese código registrado
        } catch (EOFException e) {
            throw new ProducteNoExistentException(
                    "No existe ningún producto registrado con el código \'" + codigo + "\'.");

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            return p;
        }

        return p;
    }

    @Override
    public List<Producte> cercaPerNom(String nombre) {
        List<Producte> productos = new ArrayList<>();

        // Le damos formato al nombre introducido
        nombre = formatearNombre(nombre);

        /*
        Sabiendo el tamaño en bytes de cada registro, recorremos de nombre en nombre hasta que coincida, comenzando
        desde la posición 4 (inicio del campo nombre de primer registro) y sumando el tamaño del registro en cada
        iteración. En el momento que encontremos un nombre coincidente, llamaremos al metodo leerProducto() indicandole
        la posición inicial del producto, en este caso la del nombre - 4. Debido a que el nombre es un campo con valores
        que se pueden repetir, aunque encontremos coincidentes debemos iterar hata el final. Como la instancia de
        RandomAccessFile está en el try-with-resources se cerrará de manera automática.
         */
        try (RandomAccessFile raf = new RandomAccessFile(rutaProductos, "r")) {
            int posNombre = 4;
            String nombreRegistro;

            // Iteramos de código en código
            for (int i = posNombre; i < raf.length(); i += tamanoRegistro) {
                // Posicionar el pointer, recoger el nombre y comprobar si es el que buscamos
                raf.seek(i);
                nombreRegistro = raf.readUTF();

                // Si encontramos el nombre coincidente, leemos el producto completo
                if (nombreRegistro.equalsIgnoreCase(nombre)) {
                    productos.add(leerProducto(raf, ((i - posNombre) / tamanoRegistro)));
                }
            }

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        return productos;
    }

    @Override
    public List<Producte> cercaSenseStock() {
        List<Producte> productos = new ArrayList<>();

        /*
        Sabiendo el tamaño en bytes de cada registro y que el campo Stock se registra justo antes del campo
        Descatalogado, recorremos de Stock en Stock hasta que encontremos uno con Stock = 0, comenzando desde la
        posición 64 (inicio del campo Stock de primer registro) y sumando el tamaño del registro en cada
        iteración. En el momento que encontremos un Stock = 0, comprobaremos si está descatalogado. En caso de que NO lo
        esté llamaremos al metodo leerProducto() indicandole la posición inicial del producto, en este caso la del
        Stock - 64, añadiremos ese producto al List y continuaremos iterando. Como la instancia de RandomAccessFile
        está en el try-with-resources se cerrará de manera automática.
         */
        try (RandomAccessFile raf = new RandomAccessFile(rutaProductos, "r")) {
            int posStock = 64;
            int stock;
            boolean descatalogado;

            // Iteramos de stock en stock
            for (int i = posStock; i < raf.length(); i += tamanoRegistro) {
                // Posicionar el pointer, recoger el stock y si está descatalogado
                raf.seek(i);
                stock = raf.readInt();
                descatalogado = raf.readBoolean();

                // En el caso en que se cumplan las condiciones, leeremos el producto y lo añadiremos al List
                if (stock == 0 && !descatalogado) {
                    productos.add(leerProducto(raf, ((i - posStock) / tamanoRegistro)));
                }
            }

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        return productos;
    }

    @Override
    public List<Producte> cercaDescatalogats() {
        List<Producte> productos = new ArrayList<>();

        /*
        Sabiendo el tamaño en bytes de cada registro, recorremos de campo Descatalogado en campo Descatalogado hasta que
        encontremos uno con Descatalogado = true, comenzando desde la posición 68 (inicio del campo Descatalogado del
        primer registro) y sumando el tamaño del registro en cada iteración. En el momento que encontremos un
        Descatalogado = true llamaremos al metodo leerProducto() indicandole la posición inicial del producto, en este
        caso la del Descatalogado - 68, añadiremos ese producto al List y continuaremos iterando. Como la instancia de
        RandomAccessFile está en el try-with-resources se cerrará de manera automática.
         */
        try (RandomAccessFile raf = new RandomAccessFile(rutaProductos, "r")) {
            int posDescatalogado = 68;
            boolean descatalogado;

            // Iteramos de campo descatalogado en campo descatalogado
            for (int i = posDescatalogado; i < raf.length(); i += tamanoRegistro) {
                // Posicionar el pointer, recoger el campo descatalogadostock y si está descatalogado
                raf.seek(i);
                descatalogado = raf.readBoolean();

                // En el caso en que esté descatalogado, leeremos el producto y lo añadiremos al List
                if (descatalogado) {
                    productos.add(leerProducto(raf, ((i - posDescatalogado) / tamanoRegistro)));
                }
            }

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        return productos;
    }

    @Override
    public void exportarSenseStock() {
        // Validar integridad del fichero sin-stock.bin
        if (!validarFichero(rutaSinStock)) {
            return;
        }

        // Obtener todos los productos registrados con Stock = 0 y Descatalogado = false
        List<Producte> productos = cercaSenseStock();

        // Escribir todos los productos obtenidos en el fichero sin-stock.txt
        try (PrintWriter pw = new PrintWriter(rutaSinStock)) {
            for (Producte p : productos) {
                pw.printf("%d;%s;%.2f;%d;%b\n",
                        p.getCodigo(), p.getNombre().strip(), p.getPrecio(), p.getStock(), p.isDescatalogado());
            }
        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }

    @Override
    public void exportarDescatalogats() {
        // Validar integridad del fichero descatalogado.txt
        if (!validarFichero(rutaDescatalogado)) {
            return;
        }

        // Obtener todos los productos registrados con Descatalogado = true
        List<Producte> productos = cercaDescatalogats();

        // Escribir todos los productos obtenidos en el fichero descatalogado.txt
        try (PrintWriter pw = new PrintWriter(rutaDescatalogado)) {
            for (Producte p : productos) {
                pw.printf("%d;%s;%.2f;%d;%b\n",
                        p.getCodigo(), p.getNombre().strip(), p.getPrecio(), p.getStock(), p.isDescatalogado());
            }
        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }
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



    private Producte leerProducto(RandomAccessFile raf, int posicion) throws IOException {
        Producte p = null;
        int codigo, stock;
        String nombre;
        double precio;
        boolean descatalogado;

        // Ubicamos el pointer en la posicion inicial del registro deseado y leemos todos los campos de registro
        raf.seek(posicion * tamanoRegistro);
        codigo = raf.readInt();
        nombre = raf.readUTF();
        precio = raf.readDouble();
        stock = raf.readInt();
        descatalogado = raf.readBoolean();

        // Una vez recogidos todos los valores del producto, creamos una instancia Producte con esos valores
        p = new Producte(codigo, nombre, precio, stock, descatalogado);

        return p;
    }

    private void escribirProducto(RandomAccessFile raf, Producte p) throws IOException {
        // Ubicamos el pointer en el final del fichero
        raf.seek(rutaProductos.length());

        // Escribimos todos los campos de producto
        raf.writeInt(p.getCodigo());
        raf.writeUTF(p.getNombre());
        raf.writeDouble(p.getPrecio());
        raf.writeInt(p.getStock());
        raf.writeBoolean(p.isDescatalogado());
    }

    private void validarDatos(Producte p) throws ProducteNoValidException {

        // Nombre
        p.setNombre(formatearNombre(p.getNombre()));

        // Precio
        if (p.getPrecio() < 0) {
            throw new ProducteNoValidException("Precio inferior a 0.");
        }

        // Stock
        if (p.getStock() < 0) {
            throw new ProducteNoValidException("Stock inferior a 0.");
        }
    }

    private String formatearNombre(String nombre) {
        StringBuilder nombreBuilder = new StringBuilder(nombre.toUpperCase());

        if (nombre.length() < 50) {
            for (int i = nombre.length(); i < 50; i++) {
                nombreBuilder.append(" ");
            }
        }

        return nombreBuilder.toString();
    }

    private boolean validarFichero(File fichero) {
        boolean ficheroValidado = true;

        // Validar directorio
        File rutaFichero = new File(fichero.getPath().substring(0, fichero.getPath().lastIndexOf("\\")));
        try {
            rutaFichero.mkdirs();
            fichero.createNewFile();
        } catch (IOException e) {
            System.err.printf("Error al crear el fichero %s: %s\n", fichero.getName(), e.getMessage());
            ficheroValidado = false;
        }

        return ficheroValidado;
    }

}
