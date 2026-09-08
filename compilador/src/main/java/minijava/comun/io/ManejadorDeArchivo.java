package minijava.comun.io;


// Extiende la interfaz de la cátedra con lo necesario para el Reporte de Error Elegante.
public interface ManejadorDeArchivo extends SourceManager {

    int getColumnNumber();

    String getCurrentLineText();

    // La confirmación del fin de archivo. El centinela END_OF_FILE sigue siendo la
    // señal, pero solo no alcanza: (char) 26 es un byte legítimo dentro de un
    // fuente, así que recibirlo es condición necesaria y no suficiente. Esto
    // responde la otra mitad —si la posición de lectura se pasó del contenido—, y
    // el fin de archivo es la conjunción de las dos.
    //
    // El AND queda implícito en los call sites porque este método ya implica al
    // otro conjunto: el manejador devuelve el centinela SOLO en la misma rama en
    // que lo prende. Un (char) 26 embebido llega con esto en false y cae donde
    // corresponde, en el carácter desconocido.
    boolean estaEnFinDeArchivo();
}
