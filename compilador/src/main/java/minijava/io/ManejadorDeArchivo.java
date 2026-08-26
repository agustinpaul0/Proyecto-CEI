package minijava.io;


// Extiende la interfaz de la cátedra con lo necesario para el Reporte de Error Elegante.
public interface ManejadorDeArchivo extends SourceManager {

    int getColumnNumber();

    String getCurrentLineText();

    // El centinela SourceManager.END_OF_FILE es (char) 26, que también es un
    // byte perfectamente legítimo dentro de un archivo fuente. Comparar el
    // carácter devuelto contra el centinela hacía que un 0x1A embebido
    // truncara el análisis en silencio. Esta consulta responde por POSICIÓN,
    // no por valor: sólo es true si el último getNextChar() se pasó del final
    // real del contenido.
    boolean estaEnFinDeArchivo();
}
