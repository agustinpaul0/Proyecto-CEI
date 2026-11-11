package minijava.aplicacion;

import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.comun.salida.Salida;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.FormateadorDeError;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.sintactico.AnalizadorSintactico;
import minijava.sintactico.errores.ExcepcionSintactica;

import minijava.generacion.Generador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Pautas Etapa 3, slides 15-25, Etapa 4, slides 15-27, y Etapa 5, slides 14-26:
// crea la tabla de símbolos, corre el sintáctico que la llena con las
// declaraciones y los AST y, sólo si terminó sin errores, le pide el chequeo de
// declaraciones y la consolidación. En la Etapa 4 sigue con el chequeo de
// sentencias, que corre sobre la tabla consolidada (Clase 10, slide 50). En la
// Etapa 5, con el punto de entrada y la generación de código, que se escribe en el
// archivo de salida sólo si no hubo errores. El primer error corta el análisis.
public final class ModuloPrincipalSemantico implements ModuloPrincipal {

    private static final String MENSAJE_DE_EXITO = "Compilacion Exitosa";

    private final Salida salida;
    private final FormateadorDeError formateadorDeError;
    private final boolean chequeaSentencias;
    private final String rutaSalida;

    // La Etapa 3: sólo las declaraciones.
    public ModuloPrincipalSemantico(Salida salida) {
        this(salida, false, null);
    }

    private ModuloPrincipalSemantico(Salida salida, boolean chequeaSentencias, String rutaSalida) {
        this.salida = salida;
        this.formateadorDeError = new FormateadorDeError();
        this.chequeaSentencias = chequeaSentencias;
        this.rutaSalida = rutaSalida;
    }

    // La Etapa 4: las declaraciones y después las sentencias.
    public static ModuloPrincipalSemantico conChequeoDeSentencias(Salida salida) {
        return new ModuloPrincipalSemantico(salida, true, null);
    }

    // La Etapa 5: además, el código CeIASM en `rutaSalida`.
    public static ModuloPrincipalSemantico conGeneracionDeCodigo(Salida salida, String rutaSalida) {
        return new ModuloPrincipalSemantico(salida, true, rutaSalida);
    }

    @Override
    public void ejecutar(String rutaFuente) throws IOException {
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(rutaFuente);
        TablaDeSimbolos.inicializar();
        var analizador = new AnalizadorSintactico(
                new AnalizadorLexico(new ContextoLexico(manejador)));

        try {
            List<ExcepcionSintactica> errores = analizador.analizar();
            if (errores.isEmpty()) {
                chequearSemantica();
            } else {
                errores.forEach(this::reportar);
            }
        } catch (ExcepcionLexica excepcion) {
            analizador.erroresDetectados().forEach(this::reportar);
            formateadorDeError.formatear(excepcion).forEach(salida::imprimir);
        } finally {
            manejador.close();
        }
    }

    private void chequearSemantica() {
        try {
            TablaDeSimbolos.chequearDeclaraciones();
            TablaDeSimbolos.consolidar();
            if (chequeaSentencias) {
                TablaDeSimbolos.chequearSentencias();
            }
            if (rutaSalida != null) {
                TablaDeSimbolos.chequearPuntoDeEntrada();
                Generador generador = new Generador();
                TablaDeSimbolos.generarCodigo(generador);
                Files.writeString(Path.of(rutaSalida), generador.texto(), StandardCharsets.UTF_8);
            }
            salida.imprimir(MENSAJE_DE_EXITO);
            salida.imprimir("");
            salida.imprimir("[SinErrores]");
        } catch (ExcepcionSemantica excepcion) {
            reportar(excepcion.getMessage(), excepcion.getLexemaOfensivo(), excepcion.getLinea());
        } catch (IOException excepcion) {
            salida.imprimir("No se pudo escribir el archivo de salida: " + rutaSalida);
        }
    }

    private void reportar(ExcepcionSintactica excepcion) {
        reportar(excepcion.getMessage(), excepcion.getLexemaOfensivo(), excepcion.getLinea());
    }

    // El mismo formato que la Etapa 2 (Pautas Etapa 3, slides 28-29).
    private void reportar(String mensaje, String lexema, int linea) {
        salida.imprimir(mensaje);
        salida.imprimir("");
        salida.imprimir("[Error:" + lexema + "|" + linea + "]");
    }
}
