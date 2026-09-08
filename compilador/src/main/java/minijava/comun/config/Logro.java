package minijava.comun.config;

import java.util.Arrays;
import java.util.Optional;

// Un valor por cada logro que cambia el comportamiento OBSERVABLE del compilador
// y por lo tanto se puede prender y apagar. Los logros que no son un interruptor
// —Manejador de Archivos Eficiente, Imbatibilidad, Entrega Anticipada, Good Job,
// Well Done— no están acá: no hay nada que apagar.
//
// El nombre en minúsculas es el mismo flag con el que la gramática marca sus
// producciones (`@+ternario`, `@-visibilidad`) en gramatica/minijava-ll1-logros.g,
// para que el enum y la gramática se lean uno contra el otro. Es también el
// nombre que acepta --logros= en la línea de comandos.
public enum Logro {

    FLOATS("floats", 1),
    UNICODES("unicodes", 1),
    MULTI_DETECCION("multideteccion", 1),
    REPORTE_ELEGANTE("reporteelegante", 1),

    VISIBILIDAD("visibilidad", 2),
    ATRINIT("atrinit", 2),
    VARCLASICA("varclasica", 2),
    FOR("for", 2),
    TERNARIO("ternario", 2),
    POSFIJO("posfijo", 2),
    LAMBDA("lambda", 2),
    ARRINIT("arrinit", 2),
    GENERICO("generico", 2),
    MODO_PANICO("modopanico", 2);

    // El flag de la gramática es `float`, no `floats`: la Etapa 1 lo bautizó en
    // plural y la gramática usa el singular del terminal. Se acepta cualquiera
    // de los dos en --logros para que no haya que recordar cuál va.
    private static final String ALIAS_DE_FLOATS = "float";

    private final String nombre;
    private final int etapa;

    Logro(String nombre, int etapa) {
        this.nombre = nombre;
        this.etapa = etapa;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEtapa() {
        return etapa;
    }

    public static Optional<Logro> porNombre(String nombre) {
        String normalizado = nombre.trim().toLowerCase();
        if (normalizado.equals(ALIAS_DE_FLOATS)) {
            return Optional.of(FLOATS);
        }
        return Arrays.stream(values())
                .filter(logro -> logro.nombre.equals(normalizado))
                .findFirst();
    }
}
