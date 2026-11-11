package minijava.generacion;

import java.util.List;

// La función Gen de la Clase 16, slide 7: agrega una instrucción CeIASM a la
// secuencia generada. Se crea uno por compilación y se pasa a cada `generar`, así
// la tabla de símbolos sigue siendo el único estado estático del compilador.
public final class Generador {

    private final StringBuilder texto = new StringBuilder();
    private int ultimaEtiqueta;

    public void seccionDeCodigo() {
        texto.append(".CODE\n");
    }

    public void seccionDeDatos() {
        texto.append(".DATA\n");
    }

    public void gen(String instruccion) {
        texto.append("    ").append(instruccion).append('\n');
    }

    // Una etiqueta va en su propia línea, sobre un NOP: así marca cualquier punto
    // del código, incluido el final de un bloque.
    public void etiqueta(String etiqueta) {
        texto.append(etiqueta).append(": NOP\n");
    }

    public void datos(String etiqueta, List<String> valores) {
        texto.append(etiqueta).append(": DW ").append(String.join(", ", valores)).append('\n');
    }

    // Apunte §16: una cadena terminada en 0, para los mensajes de las rutinas.
    public void cadena(String etiqueta, String contenido) {
        texto.append(etiqueta).append(": DW \"").append(contenido).append("\", 0\n");
    }

    public String etiquetaNueva(String prefijo) {
        ultimaEtiqueta++;
        return prefijo + "@" + ultimaEtiqueta;
    }

    public String texto() {
        return texto.toString();
    }
}
