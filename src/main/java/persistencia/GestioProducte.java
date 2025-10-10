package persistencia;

import model.Producte;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Clase que gestiona el almacenamiento, modificación, exportación y búsqueda de productos
 * en ficheros binarios y de texto. Implementa la interfaz {@link Gestionable}.
 * <p>
 * Cada producto se guarda en un fichero binario con registros de longitud fija (69 bytes).
 * La clase utiliza {@link RandomAccessFile} para realizar operaciones de lectura y escritura directa.
 * </p>
 */
public class GestioProducte implements Gestionable {

    /** Ruta del fichero principal de productos (binario). */
    private final File RUTA_PRODUCTOS;
    /** Ruta del fichero de exportación de productos sin stock. */
    private final File RUTA_SIN_STOCK;
    /** Ruta del fichero de exportación de productos descatalogados. */
    private final File RUTA_DESCATALOGADO;
    /** Ruta del fichero temporal utilizado para eliminar descatalogados. */
    private final File RUTA_TEMP = new File("temp/productos-temp.bin");
    /** Tamaño fijo en bytes de cada registro de producto. */
    private final int TAMANO_REGISTRO = 69;

    /**
     * Constructor principal.
     *
     * @param rutaProductos     fichero donde se almacenan los productos.
     * @param rutaSinStock      fichero de exportación de productos sin stock.
     * @param rutaDescatalogado fichero de exportación de productos descatalogados.
     */
    public GestioProducte (File rutaProductos, File rutaSinStock, File rutaDescatalogado) {
        this.RUTA_PRODUCTOS = rutaProductos;
        this.RUTA_SIN_STOCK = rutaSinStock;
        this.RUTA_DESCATALOGADO = rutaDescatalogado;
    }

    /**
     * Añade un nuevo producto al fichero binario de productos.
     * Genera automáticamente el código del producto (último código + 1).
     *
     * @param p producto a añadir.
     * @return código generado del nuevo producto o -1 si ocurre un error.
     * @throws ProducteNoValidException si los datos del producto son inválidos.
     */
    @Override
    public int afegirProducte(Producte p) throws ProducteNoValidException {
        int codigoGenerado;

        // Validar integridad del fichero productos.bin
        if (!validarFichero(RUTA_PRODUCTOS)) {
            return -1;
        }

        // Validar los datos del producto
        validarDatos(p);

        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "rw")) {
            // Generar código para el producto cogiendo el último código + 1
            int totalRegistros = (int) (RUTA_PRODUCTOS.length() / TAMANO_REGISTRO);

            // En caso de que el total de registros sea 0 se asigna directamente el código 1
            if (totalRegistros == 0) {
                codigoGenerado = 1;

            } else {
                Producte ultimoRegistro = leerProducto(raf, totalRegistros - 1);
                codigoGenerado = ultimoRegistro.getCodigo() + 1;
            }

            // Añadir el nuevo producto a productos.bin
            p.setCodigo(codigoGenerado);
            escribirProducto(raf, p, raf.length());
            System.out.printf("[%d] %s añadido correctamente.\n", p.getCodigo(), p.getNombre().strip());

        } catch (FileNotFoundException e) {
            System.err.println("Error! No se ha podido encontrar el archivo \"" +
                    RUTA_PRODUCTOS.getPath() + "\".");
            return -1;

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            return -1;
        }

        return codigoGenerado;
    }

    /**
     * Busca un producto por su código en el fichero binario.
     *
     * @param codigo código único del producto.
     * @return producto encontrado o {@code null} si no existe.
     * @throws ProducteNoValidException    si el código es menor que 1.
     * @throws ProducteNoExistentException si no existe un producto con ese código.
     */
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
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "r")) {
            int posCodigo = 0;
            int codigoRegistro;

            // Iteramos de código en código
            for (int i = posCodigo; i <= raf.length(); i += TAMANO_REGISTRO) {
                // Posicionar el pointer, recoger el código y comprobar si es el que buscamos
                raf.seek(i);
                codigoRegistro = raf.readInt();

                // Si encontramos el código coincidente, leemos el producto completo
                if (codigoRegistro == codigo) {
                    p = leerProducto(raf, (i / TAMANO_REGISTRO));
                    break;
                }
            }

        // En caso de capturar EOFException significará que no existe ningún producto con ese código registrado
        } catch (EOFException e) {
            throw new ProducteNoExistentException(
                    "No existe ningún producto registrado con el código \'" + codigo + "\'.");

        } catch (FileNotFoundException e) {
            System.err.println("Error! No se ha podido encontrar el archivo " + RUTA_PRODUCTOS);
            
        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            return p;
        }

        return p;
    }

    /**
     * Busca todos los productos cuyo nombre coincide con el indicado (ignorando mayúsculas/minúsculas).
     *
     * @param nombre nombre del producto a buscar.
     * @return lista de productos con ese nombre.
     */
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
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "r")) {
            int posNombre = 4;
            String nombreRegistro;

            // Iteramos de código en código
            for (int i = posNombre; i < raf.length(); i += TAMANO_REGISTRO) {
                // Posicionar el pointer, recoger el nombre y comprobar si es el que buscamos
                raf.seek(i);
                nombreRegistro = raf.readUTF();

                // Si encontramos el nombre coincidente, leemos el producto completo
                if (nombreRegistro.equalsIgnoreCase(nombre)) {
                    productos.add(leerProducto(raf, ((i - posNombre) / TAMANO_REGISTRO)));
                }
            }

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        return productos;
    }

    /**
     * Devuelve una lista de productos que no tienen stock y no están descatalogados.
     *
     * @return lista de productos sin stock.
     */
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
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "r")) {
            int posStock = 64;
            int stock;
            boolean descatalogado;

            // Iteramos de stock en stock
            for (int i = posStock; i < raf.length(); i += TAMANO_REGISTRO) {
                // Posicionar el pointer, recoger el stock y si está descatalogado
                raf.seek(i);
                stock = raf.readInt();
                descatalogado = raf.readBoolean();

                // En el caso en que se cumplan las condiciones, leeremos el producto y lo añadiremos al List
                if (stock == 0 && !descatalogado) {
                    productos.add(leerProducto(raf, ((i - posStock) / TAMANO_REGISTRO)));
                }
            }

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        return productos;
    }

    /**
     * Devuelve una lista de productos descatalogados.
     *
     * @return lista de productos descatalogados.
     */
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
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "r")) {
            int posDescatalogado = 68;
            boolean descatalogado;

            // Iteramos de campo descatalogado en campo descatalogado
            for (int i = posDescatalogado; i < raf.length(); i += TAMANO_REGISTRO) {
                // Posicionar el pointer, recoger el campo descatalogadostock y si está descatalogado
                raf.seek(i);
                descatalogado = raf.readBoolean();

                // En el caso en que esté descatalogado, leeremos el producto y lo añadiremos al List
                if (descatalogado) {
                    productos.add(leerProducto(raf, ((i - posDescatalogado) / TAMANO_REGISTRO)));
                }
            }

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        return productos;
    }

    /**
     * Exporta a un fichero de texto todos los productos sin stock.
     * Cada línea contiene los campos separados por punto y coma.
     */
    @Override
    public void exportarSenseStock() {
        // Validar integridad del fichero sin-stock.bin
        if (!validarFichero(RUTA_SIN_STOCK)) {
            return;
        }

        // Obtener todos los productos registrados con Stock = 0 y Descatalogado = false
        List<Producte> productos = cercaSenseStock();

        // Escribir todos los productos obtenidos en el fichero sin-stock.txt
        try (PrintWriter pw = new PrintWriter(RUTA_SIN_STOCK)) {
            for (Producte p : productos) {
                pw.printf("%d;%s;%.2f;%d;%b\n",
                        p.getCodigo(), p.getNombre().strip(), p.getPrecio(), p.getStock(), p.isDescatalogado());
            }
        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }

    /**
     * Exporta a un fichero de texto todos los productos descatalogados.
     * Cada línea contiene los campos separados por punto y coma.
     */
    @Override
    public void exportarDescatalogats() {
        // Validar integridad del fichero descatalogado.txt
        if (!validarFichero(RUTA_DESCATALOGADO)) {
            return;
        }

        // Obtener todos los productos registrados con Descatalogado = true
        List<Producte> productos = cercaDescatalogats();

        // Escribir todos los productos obtenidos en el fichero descatalogado.txt
        try (PrintWriter pw = new PrintWriter(RUTA_DESCATALOGADO)) {
            for (Producte p : productos) {
                pw.printf("%d;%s;%.2f;%d;%b\n",
                        p.getCodigo(), p.getNombre().strip(), p.getPrecio(), p.getStock(), p.isDescatalogado());
            }
        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }

    /**
     * Modifica un producto existente en el fichero binario.
     *
     * @param p producto con los nuevos valores.
     * @throws ProducteNoValidException    si los datos son inválidos.
     * @throws ProducteNoExistentException si el producto no existe.
     */
    @Override
    public void modificarProducte(Producte p) throws ProducteNoValidException, ProducteNoExistentException {
        // Validamos el producto recibido
        validarDatos(p);

        // Debido a que el campo Código es único lo utilizaremos para encontrar el producto a modificar
        long posicion = -1L;
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "rw")) {
            /*
             Buscamos la posición del producto a modificar, comenzando desde el byte 0 (codigo del registro 1) iterando
             de TAMANO_REGISTRO en TAMANO_REGISTRO.
             */
            for (int i = 0; i <= raf.length(); i += TAMANO_REGISTRO) {
                if (raf.readInt() == p.getCodigo()) {
                    posicion = i;
                    break;
                }
            }

            // Una vez localizado el registro del Producto a modificar lo sobreescribimos con los nuevos valores
            escribirProducto(raf, p, posicion);

        // En caso de capturar EOFException significará que no existe ningún producto con ese código registrado
        } catch (EOFException e) {
            throw new ProducteNoExistentException(
                        "No existe ningún producto registrado con el código \'" + p.getCodigo() + "\'.");
        } catch (FileNotFoundException e) {
            System.err.println("Error! No se ha podido encontrar el archivo \"" +
                    RUTA_PRODUCTOS.getPath() + "\".");

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

    }

    /**
     * Modifica el stock de un producto, sumando o restando una cantidad.
     *
     * @param codigo      código del producto.
     * @param cantidad    cantidad a modificar (> 0).
     * @param incrementar true para sumar stock, false para restar.
     * @throws ProducteNoExistentException si no existe el producto.
     * @throws StockNoValidException       si la cantidad es inválida o el stock resultante sería negativo.
     */
    @Override
    public void modificarStock(int codigo, int cantidad, boolean incrementar) throws ProducteNoExistentException, StockNoValidException {
        // Debido a que el campo Código es único lo utilizaremos para encontrar el producto a modificar
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "rw")) {
            long posicionStock = -1L;
            int stock = 0;

            /*
             Buscamos la posición del producto a modificar, comenzando desde el byte 0 (codigo del registro 1) iterando
             de TAMANO_REGISTRO en TAMANO_REGISTRO.
             */
            for (int i = 0; i <= raf.length(); i += TAMANO_REGISTRO) {
                if (raf.readInt() == codigo) {
                    posicionStock = i + 64;
                    raf.seek(posicionStock);
                    stock = raf.readInt();
                    break;
                }
            }

            // Validar el nuevo valor de Stock
            if (cantidad < 0) {
                throw new StockNoValidException("El valor a incrementar/decrementar tiene que > 0.");
            } else if (!incrementar && stock - cantidad < 0) {
                throw new StockNoValidException("El nuevo valor de Stock no es válido.");
            }

            // Calcular el nuevo stock
            if (incrementar) {
                stock += cantidad;
            } else {
                stock -= cantidad;
            }

            // Modificar el stock del producto
            raf.seek(posicionStock);
            raf.writeInt(stock);

        // En caso de capturar EOFException significará que no existe ningún producto con ese código registrado
        } catch (EOFException e) {
            throw new ProducteNoExistentException(
                        "No existe ningún producto registrado con el código \'" + codigo + "\'.");
        } catch (FileNotFoundException e) {
            System.err.println("Error! No se ha podido encontrar el archivo \"" +
                    RUTA_PRODUCTOS.getPath() + "\".");

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }

    /**
     * Marca un producto como descatalogado.
     *
     * @param codigo código del producto a descatalogar.
     * @throws ProducteNoExistentException si el producto no existe.
     */
    @Override
    public void descatalogarProducte(int codigo) throws ProducteNoExistentException {
        // Debido a que el campo Código es único lo utilizaremos para encontrar el producto a modificar
        long posicion = -1L;
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_PRODUCTOS, "rw")) {
            /*
             Buscamos la posición del producto a modificar, comenzando desde el byte 0 (codigo del registro 1) iterando
             de TAMANO_REGISTRO en TAMANO_REGISTRO.
             */
            for (int i = 0; i <= raf.length(); i += TAMANO_REGISTRO) {
                if (raf.readInt() == codigo) {
                    posicion = i;
                    break;
                }
            }

            // Una vez localizado el registro del Producto a modificar, cambiaremos su valor de Descatalogado
            raf.seek(posicion + 68);
            raf.writeBoolean(true);

            // En caso de capturar EOFException significará que no existe ningún producto con ese código registrado
        } catch (EOFException e) {
            throw new ProducteNoExistentException(
                                "No existe ningún producto registrado con el código \'" + codigo + "\'.");
        } catch (FileNotFoundException e) {
            System.err.println("Error! No se ha podido encontrar el archivo \"" +
                    RUTA_PRODUCTOS.getPath() + "\".");

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }

    /**
     * Elimina físicamente todos los productos descatalogados del fichero principal.
     * Crea un fichero temporal con los productos activos y reemplaza el original.
     */
    @Override
    public void esborrarDescatalogats() {
        // Primero creamos un fichero temporal donde guardaremos todos los productos sin descatalogar
        validarFichero(RUTA_TEMP);

        /*
        Aprovecharemos que el metodo cercaDescatalogats() nos devuelve un List con todos los descatalogados, para
        conocer sus códigos iteraremos sobre todos esos productos guardando el valor de su código en un HashSet para
        poder comprobar de manera rápida y eficiente si un código concreto está en la lista de descatalogados. De
        esta manera, iteraremos de código en código del fichero productos, y en caso de que el HashSet no contenga
        ese código lo añadiremos al fichero temporal.
         */
        try (RandomAccessFile rafTemp = new RandomAccessFile(RUTA_TEMP, "rw");
            RandomAccessFile rafProd = new RandomAccessFile(RUTA_PRODUCTOS, "r")) {
            List<Producte> descatalogados = cercaDescatalogats();

            // Hacemos el HashSet de códigos
            Set<Integer> codigosDesc = new HashSet<>();
            for (Producte p : descatalogados) {
                codigosDesc.add(p.getCodigo());
            }

            // Buscamos todos los productos NO descatalogados
            int codigo;
            long numProductos = rafProd.length() / 69;
            for (int i = 0; i / 69 < numProductos; i += 69) {
                rafProd.seek(i);
                codigo = rafProd.readInt();

                // Si NO está descatalogado, lo leemos entero y lo escribimos en el fichero temporal
                if (!codigosDesc.contains(codigo)) {
                    Producte p = new Producte(
                            codigo,
                            rafProd.readUTF(),
                            rafProd.readDouble(),
                            rafProd.readInt(),
                            rafProd.readBoolean()
                    );
                    escribirProducto(rafTemp, p, rafTemp.length());
                    // Devolvemos el pointer a la posicion inicial del producto
                    rafProd.seek(i);
                }
            }

        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
        }

        /*
        Una vez escritos todos los productos NO descatalogados en el fichero temporal, eliminaremos el fichero
        productos.bin y renombraremos el temporal con ese nombre. De esta manera conseguiremos un fichero productos.bin
        que contendrá todos los productos excepto los descatalogados.
         */
        RUTA_PRODUCTOS.delete();
        RUTA_TEMP.renameTo(RUTA_PRODUCTOS);
    }

    // ------------------------------------------------------------------------
    // MÉTODOS PRIVADOS AUXILIARES
    // ------------------------------------------------------------------------

    /**
     * Lee un producto completo desde una posición concreta del fichero.
     *
     * @param raf       acceso aleatorio al fichero.
     * @param posicion  posición (en número de registro).
     * @return objeto {@link Producte} leído.
     * @throws IOException si ocurre un error de lectura.
     */
    private Producte leerProducto(RandomAccessFile raf, int posicion) throws IOException {
        Producte p = null;
        int codigo, stock;
        String nombre;
        double precio;
        boolean descatalogado;

        // Ubicamos el pointer en la posicion inicial del registro deseado y leemos todos los campos de registro
        raf.seek(posicion * TAMANO_REGISTRO);
        codigo = raf.readInt();
        nombre = raf.readUTF();
        precio = raf.readDouble();
        stock = raf.readInt();
        descatalogado = raf.readBoolean();

        // Una vez recogidos todos los valores del producto, creamos una instancia Producte con esos valores
        p = new Producte(codigo, nombre, precio, stock, descatalogado);

        return p;
    }

    /**
     * Escribe un producto completo en el fichero binario en la posición indicada.
     *
     * @param raf       acceso aleatorio al fichero.
     * @param p         producto a escribir.
     * @param posicion  posición (en bytes) donde escribir.
     * @throws IOException si ocurre un error de escritura.
     */
    private void escribirProducto(RandomAccessFile raf, Producte p, long posicion) throws IOException {
        // Ubicamos el pointer en el final del fichero
        raf.seek(posicion);

        // Escribimos todos los campos de producto
        raf.writeInt(p.getCodigo());
        raf.writeUTF(p.getNombre());
        raf.writeDouble(p.getPrecio());
        raf.writeInt(p.getStock());
        raf.writeBoolean(p.isDescatalogado());
    }

    /**
     * Valida los datos de un producto (nombre, precio y stock).
     *
     * @param p producto a validar.
     * @throws ProducteNoValidException si algún campo no cumple las restricciones.
     */
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

    /**
     * Formatea el nombre del producto a mayúsculas y lo rellena con espacios hasta 50 caracteres.
     *
     * @param nombre nombre original.
     * @return nombre formateado y completado.
     */
    private String formatearNombre(String nombre) {
        StringBuilder nombreBuilder = new StringBuilder(nombre.toUpperCase());

        if (nombre.length() < 50) {
            for (int i = nombre.length(); i < 50; i++) {
                nombreBuilder.append(" ");
            }
        }

        return nombreBuilder.toString();
    }

    /**
     * Comprueba la existencia e integridad de un fichero. Si no existe, lo crea junto a su directorio.
     *
     * @param fichero fichero a validar.
     * @return {@code true} si se ha validado correctamente, {@code false} si ocurre un error.
     */
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
