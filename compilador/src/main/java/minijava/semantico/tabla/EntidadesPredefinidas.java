package minijava.semantico.tabla;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoPrimitivo;
import minijava.semantico.tipos.TipoReferencia;
import minijava.semantico.tipos.TipoVoid;

// Documento de semántica §7. Tienen que estar en la tabla antes de parsear
// (Clase 8, slide 17). No vienen del fuente, así que sus tokens son sintéticos,
// de la línea 0.
final class EntidadesPredefinidas {

    static final String OBJECT = "Object";
    static final String STRING = "String";
    private static final String SYSTEM = "System";
    private static final int LINEA_PREDEFINIDA = 0;

    private EntidadesPredefinidas() {
    }

    static void cargar() {
        cargarObject();
        cargarString();
        cargarSystem();
    }

    private static void cargarObject() {
        Clase object = new Clase(idClase(OBJECT));
        TablaDeSimbolos.declararClase(object);
        object.agregarMetodo(estatico(metodo("debugPrint", TipoVoid.VOID, "i", TipoPrimitivo.INT)));
        object.agregarMetodo(metodo("toString", referencia(STRING)));
        object.cerrar();
    }

    private static void cargarString() {
        Clase string = new Clase(idClase(STRING));
        TablaDeSimbolos.declararClase(string);
        string.setPadre(TablaDeSimbolos.referenciaAObject(LINEA_PREDEFINIDA));
        string.cerrar();
    }

    private static void cargarSystem() {
        Clase system = new Clase(idClase(SYSTEM));
        TablaDeSimbolos.declararClase(system);
        system.setPadre(TablaDeSimbolos.referenciaAObject(LINEA_PREDEFINIDA));
        system.agregarMetodo(estatico(metodo("read", TipoPrimitivo.INT)));
        system.agregarMetodo(estatico(metodo("printB", TipoVoid.VOID, "b", TipoPrimitivo.BOOLEAN)));
        system.agregarMetodo(estatico(metodo("printC", TipoVoid.VOID, "c", TipoPrimitivo.CHAR)));
        system.agregarMetodo(estatico(metodo("printI", TipoVoid.VOID, "i", TipoPrimitivo.INT)));
        system.agregarMetodo(estatico(metodo("printS", TipoVoid.VOID, "s", referencia(STRING))));
        system.agregarMetodo(estatico(metodo("println", TipoVoid.VOID)));
        system.agregarMetodo(estatico(metodo("printBln", TipoVoid.VOID, "b", TipoPrimitivo.BOOLEAN)));
        system.agregarMetodo(estatico(metodo("printCln", TipoVoid.VOID, "c", TipoPrimitivo.CHAR)));
        system.agregarMetodo(estatico(metodo("printIln", TipoVoid.VOID, "i", TipoPrimitivo.INT)));
        system.agregarMetodo(estatico(metodo("printSln", TipoVoid.VOID, "s", referencia(STRING))));
        system.cerrar();
    }

    private static Metodo metodo(String nombre, Tipo retorno) {
        Metodo metodo = new Metodo(new Token(TipoDeToken.ID_MV, nombre, LINEA_PREDEFINIDA));
        metodo.setRetorno(retorno);
        return metodo;
    }

    private static Metodo metodo(String nombre, Tipo retorno, String parametro, Tipo tipoDelParametro) {
        Metodo metodo = metodo(nombre, retorno);
        metodo.agregarParametro(new Token(TipoDeToken.ID_MV, parametro, LINEA_PREDEFINIDA), tipoDelParametro);
        return metodo;
    }

    private static Metodo estatico(Metodo metodo) {
        metodo.hacerEstatico();
        return metodo;
    }

    private static TipoReferencia referencia(String nombre) {
        return new TipoReferencia(idClase(nombre));
    }

    private static Token idClase(String nombre) {
        return new Token(TipoDeToken.ID_CLASE, nombre, LINEA_PREDEFINIDA);
    }
}
