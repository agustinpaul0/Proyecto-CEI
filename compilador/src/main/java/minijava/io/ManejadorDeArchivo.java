package minijava.io;


// Extiende la interfaz de la cátedra con lo necesario para el Reporte de Error Elegante.
public interface ManejadorDeArchivo extends SourceManager {

    int getColumnNumber();

    String getCurrentLineText();
}
