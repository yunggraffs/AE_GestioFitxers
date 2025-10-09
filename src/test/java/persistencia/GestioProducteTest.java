package persistencia;

import model.Producte;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class GestioProducteTest {

    @TempDir
    Path tempDir;

    private File productosFile;
    private File sinStockFile;
    private File descatalogadoFile;
    private GestioProducte gestor;

    @BeforeEach
    public void setUp() {
        productosFile = new File(tempDir.toString(), "productos.bin");
        sinStockFile = new File(tempDir.toString(), "sin-stock.txt");
        descatalogadoFile = new File(tempDir.toString(), "descatalogado.txt");
        gestor = new GestioProducte(productosFile, sinStockFile, descatalogadoFile);
        try {
            productosFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(GestioProducteTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterEach
    public void tearDown() {
        productosFile.delete();
        sinStockFile.delete();
        descatalogadoFile.delete();
    }

    @Test
    public void testAfegirProducte() {
        try {
            Producte p = new Producte("Producto1", 10.0, 5, false);
            int codigo = gestor.afegirProducte(p);
            assertTrue(codigo >= 1);
            Producte result = gestor.cercaPerCodi(codigo);
            assertNotNull(result);
            assertEquals("PRODUCTO1", result.getNombre().strip());
            assertEquals(10.0, result.getPrecio());
            assertEquals(5, result.getStock());
            assertFalse(result.isDescatalogado());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testAfegirProductePreuNegatiu() {
        try {
            Producte p = new Producte("ProductoNeg", -5.0, 5, false);
            gestor.afegirProducte(p);
            fail("Debería lanzar ProducteNoValidException");
        } catch (ProducteNoValidException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }

    @Test
    public void testAfegirProducteStockNegatiu() {
        try {
            Producte p = new Producte("ProductoNegStock", 5.0, -1, false);
            gestor.afegirProducte(p);
            fail("Debería lanzar ProducteNoValidException");
        } catch (ProducteNoValidException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }

    @Test
    public void testCercaPerCodi() {
        try {
            Producte p = new Producte("Producto2", 15.0, 3, false);
            int codigo = gestor.afegirProducte(p);
            Producte result = gestor.cercaPerCodi(codigo);
            assertNotNull(result);
            assertEquals(codigo, result.getCodigo());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testCercaPerCodiNegatiu() {
        try {
            gestor.cercaPerCodi(-1);
            fail("Debería lanzar ProducteNoValidException");
        } catch (ProducteNoValidException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }

    @Test
    public void testCercaPerCodiNoExistent() {
        try {
            gestor.cercaPerCodi(999);
            fail("Debería lanzar ProducteNoExistentException");
        } catch (ProducteNoExistentException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta. " + e.getMessage());
        }
    }

    @Test
    public void testCercaPerNom() {
        try {
            Producte p1 = new Producte("Mouse", 10.0, 5, false);
            Producte p2 = new Producte("Mouse", 12.0, 2, false);
            gestor.afegirProducte(p1);
            gestor.afegirProducte(p2);
            List<Producte> lista = gestor.cercaPerNom("Mouse");
            assertEquals(2, lista.size());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testCercaSenseStock() {
        try {
            Producte p1 = new Producte("Prod1", 5.0, 0, false);
            Producte p2 = new Producte("Prod2", 5.0, 0, true);
            Producte p3 = new Producte("Prod3", 5.0, 2, false);
            gestor.afegirProducte(p1);
            gestor.afegirProducte(p2);
            gestor.afegirProducte(p3);
            List<Producte> sinStock = gestor.cercaSenseStock();
            assertEquals(1, sinStock.size());
            assertEquals(p1.getNombre(), sinStock.get(0).getNombre());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testCercaDescatalogats() {
        try {
            Producte p1 = new Producte("Prod1", 5.0, 5, true);
            Producte p2 = new Producte("Prod2", 5.0, 5, false);
            gestor.afegirProducte(p1);
            gestor.afegirProducte(p2);
            List<Producte> descatalogados = gestor.cercaDescatalogats();
            assertEquals(1, descatalogados.size());
            assertEquals(p1.getNombre(), descatalogados.get(0).getNombre());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testExportarSenseStock() {
        try {
            Producte p1 = new Producte("Prod1", 5.0, 0, false);
            gestor.afegirProducte(p1);
            gestor.exportarSenseStock();
            assertTrue(sinStockFile.exists());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testExportarDescatalogats() {
        try {
            Producte p1 = new Producte("Prod1", 5.0, 0, true);
            gestor.afegirProducte(p1);
            gestor.exportarDescatalogats();
            assertTrue(descatalogadoFile.exists());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testModificarProducte() {
        try {
            Producte p = new Producte("Prod1", 5.0, 5, false);
            int codigo = gestor.afegirProducte(p);
            Producte modificado = new Producte(codigo, "ProdMod", 10.0, 3, true);
            gestor.modificarProducte(modificado);
            Producte result = gestor.cercaPerCodi(codigo);
            assertEquals("PRODMOD", result.getNombre().strip());
            assertEquals(10.0, result.getPrecio());
            assertEquals(3, result.getStock());
            assertTrue(result.isDescatalogado());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }

    @Test
    public void testModificarProductePreuNegativo() {
        try {
            Producte p = new Producte("Prod1", 5.0, 5, false);
            int codigo = gestor.afegirProducte(p);
            Producte modificado = new Producte(codigo, "Prod1", -1.0, 5, false);
            gestor.modificarProducte(modificado);
            fail("Debería lanzar ProducteNoValidException");
        } catch (ProducteNoValidException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }

    @Test
    public void testModificarProducteStockNegativo() {
        try {
            Producte p = new Producte("Prod1", 5.0, 5, false);
            int codigo = gestor.afegirProducte(p);
            Producte modificado = new Producte(codigo, "Prod1", 5.0, -3, false);
            gestor.modificarProducte(modificado);
            fail("Debería lanzar ProducteNoValidException");
        } catch (ProducteNoValidException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }
    
    @Test
    public void testModificarProducteNoExistent() {
        try {
            Producte modificado = new Producte(999, "NoExist", 5.0, 5, false);
            gestor.modificarProducte(modificado);
            fail("Debería lanzar ProducteNoExistentException");
        } catch (ProducteNoExistentException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }
    
    @Test
    public void testModificarStock() {
        try {
            Producte p = new Producte("ProductoStock", 10.0, 5, false);
            int codigo = gestor.afegirProducte(p);

            // Incrementar stock
            gestor.modificarStock(codigo, 3, true);
            Producte result = gestor.cercaPerCodi(codigo);
            assertEquals(8, result.getStock());

            // Decrementar stock
            gestor.modificarStock(codigo, 2, false);
            result = gestor.cercaPerCodi(codigo);
            assertEquals(6, result.getStock());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }
    
    @Test
    public void testModificarStockNegativo() {
        try {
            Producte p = new Producte("ProductoStock", 10.0, 2, false);
            int codigo = gestor.afegirProducte(p);

            gestor.modificarStock(codigo, 5, false);
            fail("Debería lanzar StockNoValidException");
        } catch (StockNoValidException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }
    
    @Test
    public void testModificarStockNoExistent() {
        try {
            gestor.modificarStock(999, 2, true);
            fail("Debería lanzar ProducteNoExistentException");
        } catch (ProducteNoExistentException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }
    
    @Test
    public void testDescatalogarProducte() {
        try {
            Producte p = new Producte("ProductoDesc", 10.0, 3, false);
            int codigo = gestor.afegirProducte(p);

            gestor.descatalogarProducte(codigo);
            Producte result = gestor.cercaPerCodi(codigo);

            assertTrue(result.isDescatalogado());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }
    
    @Test
    public void testDescatalogarProducteNoExistent() {
        try {
            gestor.descatalogarProducte(999);
            fail("Debería lanzar ProducteNoExistentException");
        } catch (ProducteNoExistentException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Se lanzó una excepción incorrecta");
        }
    }
    
    @Test
    public void testEsborrarDescatalogats() {
        try {
            Producte p1 = new Producte("ProductoActivo", 10.0, 5, false);
            Producte p2 = new Producte("ProductoDesc", 15.0, 3, true);

            gestor.afegirProducte(p1);
            gestor.afegirProducte(p2);

            gestor.esborrarDescatalogats();

            List<Producte> descatalogados = gestor.cercaDescatalogats();
            assertEquals(0, descatalogados.size());
        } catch (Exception e) {
            fail("No debería lanzar ninguna excepción");
        }
    }
}