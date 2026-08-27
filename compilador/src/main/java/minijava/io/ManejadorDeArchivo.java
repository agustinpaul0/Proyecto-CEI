package minijava.io;


// Extiende la interfaz de la cátedra con lo necesario para el Reporte de Error Elegante.
public interface ManejadorDeArchivo extends SourceManager {

    int getColumnNumber();

    String getCurrentLineText();

    // Responde por POSICIÓN, no comparando contra END_OF_FILE: (char) 26 es un
    // byte legítimo en un fuente y truncaba el análisis en silencio.
    boolean estaEnFinDeArchivo();
}
