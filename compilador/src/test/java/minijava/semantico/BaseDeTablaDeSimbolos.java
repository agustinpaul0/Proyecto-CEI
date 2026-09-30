package minijava.semantico;

import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.ClaveDeMetodo;
import minijava.semantico.entidades.Interfaz;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.sintactico.AnalizadorSintactico;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Lo común a los tests que miran la tabla de símbolos por dentro: parsear un
// fuente correcto para llenarla y, si hace falta, chequearla y consolidarla.
abstract class BaseDeTablaDeSimbolos {

    protected void parsear(Path dir, String fuente) throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        try {
            TablaDeSimbolos.inicializar();
            assertEquals(0, new AnalizadorSintactico(new AnalizadorLexico(manejador)).analizar().size(),
                    "el fuente de un test de la tabla tiene que ser sintacticamente valido");
        } finally {
            manejador.close();
        }
    }

    protected void compilar(Path dir, String fuente) throws Exception {
        parsear(dir, fuente);
        TablaDeSimbolos.chequearDeclaraciones();
        TablaDeSimbolos.consolidar();
    }

    protected ExcepcionSemantica rechazar(Path dir, String fuente) {
        return assertThrows(ExcepcionSemantica.class, () -> compilar(dir, fuente));
    }

    protected Clase clase(String nombre) {
        return (Clase) TablaDeSimbolos.tipo(nombre).orElseThrow();
    }

    protected Interfaz interfaz(String nombre) {
        return (Interfaz) TablaDeSimbolos.tipo(nombre).orElseThrow();
    }

    protected Metodo metodo(String tipo, String nombre, int aridad) {
        return TablaDeSimbolos.tipo(tipo).orElseThrow()
                .metodo(new ClaveDeMetodo(nombre, aridad)).orElseThrow();
    }
}
