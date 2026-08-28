package minijava.lexico.recuperacion;

import minijava.lexico.contexto.ContextoLexico;

// Modo pánico ante un escape mal formado en un litString: descarta hasta la
// comilla de cierre, que es el token de sincronización (decisiones.md §4.3).
// Descarta y no avanza: el lexema reportado es el armado hasta el error.
public final class RecuperacionDeLitString {

    private RecuperacionDeLitString() {
    }

    // Un litString no cruza líneas: un salto corta el descarte.
    public static void descartarHastaElCierre(ContextoLexico contexto) {
        while (!contexto.esFinDeArchivo() && contexto.getCaracterActual() != '\n') {
            char actual = contexto.getCaracterActual();
            contexto.descartar();
            if (actual == '"') {
                return;
            }
            // Una comilla escapada no cierra: se descarta con su barra.
            if (actual == '\\' && !contexto.esFinDeArchivo() && contexto.getCaracterActual() != '\n') {
                contexto.descartar();
            }
        }
    }
}
