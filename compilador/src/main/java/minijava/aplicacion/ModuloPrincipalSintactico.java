package minijava.aplicacion;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.comun.salida.Salida;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.FormateadorDeError;
import minijava.sintactico.AnalizadorSintactico;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.io.IOException;
import java.util.List;

public final class ModuloPrincipalSintactico {

    private static final String MENSAJE_DE_EXITO = "Compilacion Exitosa";

    private final Salida salida;
    private final ConfiguracionDelCompilador configuracion;
    private final FormateadorDeError formateadorDeError;

    public ModuloPrincipalSintactico(Salida salida, ConfiguracionDelCompilador configuracion) {
        this.salida = salida;
        this.configuracion = configuracion;
        this.formateadorDeError = new FormateadorDeError(configuracion);
    }

    public void ejecutar(String rutaFuente) throws IOException {
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(rutaFuente);
        var analizador = new AnalizadorSintactico(
                new AnalizadorLexico(new ContextoLexico(manejador, configuracion)), configuracion);

        try {
            List<ExcepcionSintactica> errores = analizador.analizar();
            if (errores.isEmpty()) {
                salida.imprimir(MENSAJE_DE_EXITO);
                salida.imprimir("");
                salida.imprimir("[SinErrores]");
            } else {
                errores.forEach(this::reportar);
            }
        } catch (ExcepcionLexica excepcion) {
            // Con Modo Pánico el análisis puede haber recuperado varios errores
            // sintácticos antes de que el léxico se topara con un símbolo que no
            // existe. Van primero: son anteriores en el fuente.
            analizador.erroresDetectados().forEach(this::reportar);
            formateadorDeError.formatear(excepcion).forEach(salida::imprimir);
        } finally {
            manejador.close();
        }
    }

    // El renglón en blanco entre el mensaje y el código es el del ejemplo de
    // Pautas Etapa 2, slide 25. Con Modo Pánico se repite el reporte entero por
    // cada error, que es el mismo formato con el que la Etapa 1 reporta varios
    // errores léxicos.
    private void reportar(ExcepcionSintactica excepcion) {
        salida.imprimir(excepcion.getMessage());
        salida.imprimir("");
        salida.imprimir("[Error:" + excepcion.getLexemaOfensivo() + "|" + excepcion.getLinea() + "]");
    }
}
