package minijava.sentencias;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.comun.salida.SalidaEnMemoria;
import minijava.lexico.AnalizadorLexico;
import minijava.semantico.entidades.ClaveDeMetodo;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.sintactico.AnalizadorSintactico;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Lo común a los tests de la Etapa 4 que miran adentro: parsear un fuente para
// llenar la tabla con sus AST, o compilarlo entero y leer la salida.
abstract class BaseDeSentencias {

    @TempDir
    Path directorio;

    protected void parsear(String fuente) throws Exception {
        Path archivo = escribir(fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        try {
            TablaDeSimbolos.inicializar();
            assertEquals(0, new AnalizadorSintactico(new AnalizadorLexico(manejador)).analizar().size(),
                    "el fuente de un test de sentencias tiene que ser sintacticamente valido");
        } finally {
            manejador.close();
        }
    }

    protected List<String> compilar(String fuente) throws Exception {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        ModuloPrincipalSemantico.conChequeoDeSentencias(salida).ejecutar(escribir(fuente).toString());
        return salida.getLineas();
    }

    protected String codigo(String fuente) throws Exception {
        return compilar(fuente).getLast();
    }

    protected Metodo metodo(String tipo, String nombre, int aridad) {
        return TablaDeSimbolos.tipo(tipo).orElseThrow()
                .metodo(new ClaveDeMetodo(nombre, aridad)).orElseThrow();
    }

    private Path escribir(String fuente) throws Exception {
        Path archivo = directorio.resolve("fuente.java");
        Files.writeString(archivo, fuente);
        return archivo;
    }
}
