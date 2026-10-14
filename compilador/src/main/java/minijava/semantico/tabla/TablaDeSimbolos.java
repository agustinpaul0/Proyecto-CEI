package minijava.semantico.tabla;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.Interfaz;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.entidades.Unidad;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.TipoReferencia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Estática y de acceso global, sin singleton: es la recomendación del profesor
// para la Etapa 3. Es el único estado estático del compilador, y `inicializar()`
// la deja limpia al empezar cada compilación, porque los testers de la cátedra
// llaman a `main()` muchas veces en la misma JVM.
//
// Los nombres repetidos se detectan mientras se parsea (la primera pasada del
// documento §8), pero se lanzan recién al empezar el chequeo: el semántico sólo
// corre si el sintáctico terminó sin errores (Pautas slide 20).
public final class TablaDeSimbolos {

    private static Map<String, ClaseOInterfaz> tipos;
    private static Clase claseActual;
    private static Interfaz interfazActual;
    private static ClaseOInterfaz tipoActual;
    private static Unidad unidadActual;
    private static ExcepcionSemantica primerRepetido;

    static {
        inicializar();
    }

    private TablaDeSimbolos() {
    }

    public static void inicializar() {
        tipos = new LinkedHashMap<>();
        claseActual = null;
        interfazActual = null;
        tipoActual = null;
        unidadActual = null;
        primerRepetido = null;
        EntidadesPredefinidas.cargar();
    }

    public static void declararClase(Clase clase) {
        claseActual = clase;
        declarar(clase, "La clase ");
    }

    public static void declararInterfaz(Interfaz interfaz) {
        interfazActual = interfaz;
        declarar(interfaz, "La interfaz ");
    }

    private static void declarar(ClaseOInterfaz tipo, String descripcion) {
        tipoActual = tipo;
        if (tipos.containsKey(tipo.nombre())) {
            registrarRepetido(new ExcepcionSemantica(tipo.token(),
                    descripcion + tipo.nombre() + " ya esta declarada"));
        } else {
            tipos.put(tipo.nombre(), tipo);
        }
    }

    public static Clase claseActual() {
        return claseActual;
    }

    public static Interfaz interfazActual() {
        return interfazActual;
    }

    // La clase o interfaz que se está parseando: la que declara cada `T` que aparece.
    public static ClaseOInterfaz tipoActual() {
        return tipoActual;
    }

    public static Unidad unidadActual() {
        return unidadActual;
    }

    public static void setUnidadActual(Unidad unidad) {
        unidadActual = unidad;
    }

    public static Optional<ClaseOInterfaz> tipo(String nombre) {
        return Optional.ofNullable(tipos.get(nombre));
    }

    public static ClaseOInterfaz object() {
        return tipos.get(EntidadesPredefinidas.OBJECT);
    }

    // La herencia implícita de una clase sin `extends` (documento §4).
    public static TipoReferencia referenciaAObject(int linea) {
        return referenciaA(EntidadesPredefinidas.OBJECT, linea);
    }

    // El tipo de los literales de cadena (documento §11.1).
    public static TipoReferencia referenciaAString(int linea) {
        return referenciaA(EntidadesPredefinidas.STRING, linea);
    }

    private static TipoReferencia referenciaA(String nombre, int linea) {
        return new TipoReferencia(new Token(TipoDeToken.ID_CLASE, nombre, linea));
    }

    public static List<ClaseOInterfaz> tipos() {
        return List.copyOf(tipos.values());
    }

    public static void registrarRepetido(ExcepcionSemantica repetido) {
        if (primerRepetido == null) {
            primerRepetido = repetido;
        }
    }

    public static void chequearDeclaraciones() throws ExcepcionSemantica {
        if (primerRepetido != null) {
            throw primerRepetido;
        }
        for (ClaseOInterfaz tipo : tipos.values()) {
            tipo.estaBienDeclarado();
        }
    }

    // Todos los tipos se chequean antes de consolidar cualquiera: la consolidación
    // supone que no hay ciclos y que todo ancestro existe y es del tipo correcto.
    public static void consolidar() throws ExcepcionSemantica {
        for (ClaseOInterfaz tipo : tipos.values()) {
            tipo.consolidar();
        }
    }

    // Pautas Etapa 4, slide 22: corre sólo sobre la tabla consolidada. Las
    // interfaces no tienen cuerpos.
    public static void chequearSentencias() throws ExcepcionSemantica {
        for (ClaseOInterfaz tipo : tipos.values()) {
            if (tipo instanceof Clase clase) {
                clase.chequearSentencias();
            }
        }
    }
}
