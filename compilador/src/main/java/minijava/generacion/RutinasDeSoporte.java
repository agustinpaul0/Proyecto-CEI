package minijava.generacion;

import java.util.List;

// Las rutinas que todo programa lleva, escritas a mano en CeIASM. Siguen la
// convención de llamada de cualquier unidad estática (Apunte §8): el llamador
// reserva la celda del resultado con RMEM 1 y apila los argumentos; con n
// parámetros, el i-ésimo está en n+3-i y el resultado en n+3.
public final class RutinasDeSoporte {

    public static final String NUEVO_ARREGLO = Etiquetas.rutina("nuevo_arreglo");
    public static final String NUEVO_ARREGLO_MULTI = Etiquetas.rutina("nuevo_arreglo_multi");
    public static final String COMPONENTE = Etiquetas.rutina("componente");

    private static final String MENSAJE_RANGO = Etiquetas.rutina("mensaje_rango");
    private static final String MENSAJE_TAMANIO = Etiquetas.rutina("mensaje_tamanio");
    private static final String PREFIJO_DE_ERROR = "Error en tiempo de ejecucion: ";

    private RutinasDeSoporte() {
    }

    public static void generar(Generador g) {
        g.seccionDeDatos();
        g.cadena(MENSAJE_RANGO, PREFIJO_DE_ERROR + "indice fuera de rango");
        g.cadena(MENSAJE_TAMANIO, PREFIJO_DE_ERROR + "tamanio de arreglo negativo");
        g.seccionDeCodigo();
        heap(g);
        nuevoArreglo(g);
        nuevoArregloMulti(g);
        componente(g);
    }

    // El String nuevo de n caracteres queda en el tope: VT y longitud cargadas, los
    // caracteres y el terminador en 0 (Apunte §13).
    public static void alojarString(Generador g, int longitud) {
        g.gen("RMEM 1");
        g.gen("PUSH " + (longitud + 3));
        g.gen("PUSH " + Etiquetas.MALLOC);
        g.gen("CALL");
        g.gen("DUP");
        g.gen("PUSH " + Etiquetas.tablaVirtual("String"));
        g.gen("STOREREF 0");
        g.gen("DUP");
        g.gen("PUSH " + longitud);
        g.gen("STOREREF 1");
    }

    // Apunte §17 y CONVENCIONES-CATEDRA §2: un mensaje que empieza con «Error», que
    // es lo que busca la cátedra en la salida, y HALT.
    private static void abortar(Generador g, String mensaje) {
        g.gen("PUSH " + mensaje);
        g.gen("SPRINT");
        g.gen("PRNLN");
        g.gen("HALT");
    }

    private static void prologo(Generador g, String etiqueta) {
        g.etiqueta(etiqueta);
        g.gen("LOADFP");
        g.gen("LOADSP");
        g.gen("STOREFP");
    }

    private static void epilogo(Generador g, int parametros) {
        g.gen("STOREFP");
        g.gen("RET " + parametros);
    }

    // rutinasHeap.txt de la cátedra. El malloc deja el bloque en cero, porque los
    // atributos y las componentes arrancan en 0, false o null (JLS §4.12.5) y la
    // CeIVM no garantiza memoria limpia donde la pila pasó antes.
    private static void heap(Generador g) {
        g.etiqueta(Etiquetas.HEAP_INIT);
        g.gen("RET 0");

        prologo(g, Etiquetas.MALLOC);
        for (String instruccion : List.of("LOADHL", "DUP", "PUSH 1", "ADD", "STORE 4",
                "LOAD 3", "ADD", "STOREHL")) {
            g.gen(instruccion);
        }
        String limpiar = Etiquetas.rutina("malloc_limpiar");
        String fin = Etiquetas.rutina("malloc_fin");
        g.etiqueta(limpiar);
        g.gen("LOAD 3");
        g.gen("BF " + fin);
        g.gen("LOAD 3");
        g.gen("PUSH 1");
        g.gen("SUB");
        g.gen("STORE 3");
        g.gen("LOAD 4");
        g.gen("LOAD 3");
        g.gen("ADD");
        g.gen("PUSH 0");
        g.gen("STOREREF 0");
        g.gen("JUMP " + limpiar);
        g.etiqueta(fin);
        epilogo(g, 1);
    }

    // nuevo_arreglo(tamanio): parámetro en 3, resultado en 4 (Apunte §12).
    private static void nuevoArreglo(Generador g) {
        prologo(g, NUEVO_ARREGLO);
        String valido = Etiquetas.rutina("nuevo_arreglo_valido");
        g.gen("LOAD 3");
        g.gen("PUSH 0");
        g.gen("LT");
        g.gen("BF " + valido);
        abortar(g, MENSAJE_TAMANIO);
        g.etiqueta(valido);
        g.gen("RMEM 1");
        g.gen("LOAD 3");
        g.gen("PUSH 2");
        g.gen("ADD");
        g.gen("PUSH " + Etiquetas.MALLOC);
        g.gen("CALL");
        g.gen("DUP");
        g.gen("PUSH " + Etiquetas.tablaVirtual("Object"));
        g.gen("STOREREF 0");
        g.gen("DUP");
        g.gen("LOAD 3");
        g.gen("STOREREF 1");
        g.gen("STORE 4");
        epilogo(g, 1);
    }

    // nuevo_arreglo_multi(tamanios, nivel): tamanios en 4, nivel en 3, resultado en
    // 5. Crea la dimensión `nivel` y, si no es la última, cada componente con una
    // llamada recursiva (Apunte §12). En el nivel 0 controla primero todos los
    // tamaños, como javac 25: `new int[0][-1]` falla aunque no aloje la segunda.
    // Locales: el arreglo en 0, y el índice del recorrido en -1.
    private static void nuevoArregloMulti(Generador g) {
        prologo(g, NUEVO_ARREGLO_MULTI);
        String crear = Etiquetas.rutina("multi_crear");
        String chequeo = Etiquetas.rutina("multi_chequeo");
        String siguiente = Etiquetas.rutina("multi_siguiente");
        String finChequeo = Etiquetas.rutina("multi_fin_chequeo");
        String llenar = Etiquetas.rutina("multi_llenar");
        String finLlenar = Etiquetas.rutina("multi_fin_llenar");
        String fin = Etiquetas.rutina("multi_fin");

        g.gen("LOAD 3");
        g.gen("BT " + crear);
        g.gen("PUSH 0");
        g.etiqueta(chequeo);
        g.gen("LOAD 0");
        g.gen("LOAD 4");
        g.gen("LOADREF 1");
        g.gen("LT");
        g.gen("BF " + finChequeo);
        g.gen("LOAD 4");
        g.gen("LOAD 0");
        g.gen("ADD");
        g.gen("LOADREF 2");
        g.gen("PUSH 0");
        g.gen("LT");
        g.gen("BF " + siguiente);
        abortar(g, MENSAJE_TAMANIO);
        g.etiqueta(siguiente);
        g.gen("LOAD 0");
        g.gen("PUSH 1");
        g.gen("ADD");
        g.gen("STORE 0");
        g.gen("JUMP " + chequeo);
        g.etiqueta(finChequeo);
        g.gen("FMEM 1");

        g.etiqueta(crear);
        g.gen("RMEM 1");
        g.gen("LOAD 4");
        g.gen("LOAD 3");
        g.gen("ADD");
        g.gen("LOADREF 2");
        g.gen("PUSH " + NUEVO_ARREGLO);
        g.gen("CALL");
        g.gen("LOAD 3");
        g.gen("LOAD 4");
        g.gen("LOADREF 1");
        g.gen("PUSH 1");
        g.gen("SUB");
        g.gen("LT");
        g.gen("BF " + fin);
        g.gen("PUSH 0");
        g.etiqueta(llenar);
        g.gen("LOAD -1");
        g.gen("LOAD 0");
        g.gen("LOADREF 1");
        g.gen("LT");
        g.gen("BF " + finLlenar);
        g.gen("LOAD 0");
        g.gen("LOAD -1");
        g.gen("ADD");
        g.gen("RMEM 1");
        g.gen("LOAD 4");
        g.gen("LOAD 3");
        g.gen("PUSH 1");
        g.gen("ADD");
        g.gen("PUSH " + NUEVO_ARREGLO_MULTI);
        g.gen("CALL");
        g.gen("STOREREF 2");
        g.gen("LOAD -1");
        g.gen("PUSH 1");
        g.gen("ADD");
        g.gen("STORE -1");
        g.gen("JUMP " + llenar);
        g.etiqueta(finLlenar);
        g.gen("FMEM 1");
        g.etiqueta(fin);
        g.gen("LOAD 0");
        g.gen("STORE 5");
        g.gen("FMEM 1");
        epilogo(g, 2);
    }

    // componente(arreglo, indice): arreglo en 4, índice en 3, resultado en 5. Deja
    // la dirección arreglo+indice, a la que se le suma el 2 de la VT y la longitud
    // con LOADREF 2 o STOREREF 2 (Apunte §12). LOADREF 1 sobre la referencia
    // original detecta el arreglo nulo.
    private static void componente(Generador g) {
        prologo(g, COMPONENTE);
        String valido = Etiquetas.rutina("componente_valido");
        g.gen("LOAD 4");
        g.gen("LOADREF 1");
        g.gen("LOAD 3");
        g.gen("LE");
        g.gen("LOAD 3");
        g.gen("PUSH 0");
        g.gen("LT");
        g.gen("OR");
        g.gen("BF " + valido);
        abortar(g, MENSAJE_RANGO);
        g.etiqueta(valido);
        g.gen("LOAD 4");
        g.gen("LOAD 3");
        g.gen("ADD");
        g.gen("STORE 5");
        epilogo(g, 2);
    }
}
