package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;

// Resincronización en modo pánico ante un escape mal formado dentro de un
// litString: se descarta lo que queda del literal hasta su comilla de cierre,
// que es el token de sincronización. Sin esto el análisis se reanudaría en
// mitad del literal y la comilla de cierre abriría uno nuevo, inventando
// errores en cascada (ver documentacion/decisiones.md §4.3).
//
// Descarta y no avanza: el lexema reportado tiene que quedar en el armado
// hasta el momento del error, como exige Pautas Etapa 1 slide 24.
final class RecuperacionDeLitString {

    private RecuperacionDeLitString() {
    }

    // Un litString no cruza líneas: si aparece un salto antes de la comilla,
    // el descarte se detiene ahí y el análisis sigue en la línea siguiente.
    static void descartarHastaElCierre(ContextoLexico contexto) {
        while (!contexto.esFinDeArchivo() && contexto.getCaracterActual() != '\n') {
            char actual = contexto.getCaracterActual();
            contexto.descartar();
            if (actual == '"') {
                return;
            }
            // Una comilla escapada no cierra el literal: se descarta junto con
            // su barra para no confundirla con el cierre real.
            if (actual == '\\' && !contexto.esFinDeArchivo() && contexto.getCaracterActual() != '\n') {
                contexto.descartar();
            }
        }
    }
}
