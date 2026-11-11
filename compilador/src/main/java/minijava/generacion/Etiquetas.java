package minijava.generacion;

// Las etiquetas del código generado. CeIASM no distingue mayúsculas y sólo admite
// letras, dígitos, `_`, `$` y `@` (Apunte §2), mientras que un identificador de
// MiniJava distingue mayúsculas y admite cualquier letra. Por eso cada nombre se
// codifica: lo que no es una minúscula ASCII, un dígito o `_` va como `$hex$`, y
// `Aa` y `AA` no chocan. `@` separa las partes: no aparece en ningún identificador.
public final class Etiquetas {

    public static final String HEAP_INIT = "simple_heap_init";
    public static final String MALLOC = "simple_malloc";

    private Etiquetas() {
    }

    public static String metodo(String clase, String nombre, int aridad) {
        return "metodo@" + codificar(clase) + "@" + codificar(nombre) + "@" + aridad;
    }

    public static String constructor(String clase, int aridad) {
        return "constructor@" + codificar(clase) + "@" + aridad;
    }

    public static String tablaVirtual(String clase) {
        return "vt@" + codificar(clase);
    }

    public static String rutina(String nombre) {
        return nombre + "@runtime";
    }

    static String codificar(String nombre) {
        StringBuilder codificado = new StringBuilder();
        nombre.codePoints().forEach(c -> {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
                codificado.appendCodePoint(c);
            } else {
                codificado.append('$').append(Integer.toHexString(c)).append('$');
            }
        });
        return codificado.toString();
    }
}
