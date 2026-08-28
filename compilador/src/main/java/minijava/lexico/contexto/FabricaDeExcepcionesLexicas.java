package minijava.lexico.contexto;

import minijava.errores.EscapadorDeNoImprimibles;
import minijava.errores.ExcepcionLexica;
import minijava.errores.PosicionLexica;
import minijava.errores.TipoDeErrorLexico;

// Punto único de construcción de ExcepcionLexica: ningún estado la arma a mano,
// para que lexema y posición sean siempre consistentes entre sí. Lo único que
// varía entre un error y otro es el ancla; el BuilderDeLexema la resuelve.
public final class FabricaDeExcepcionesLexicas {

    private final BuilderDeLexema lexema;

    public FabricaDeExcepcionesLexicas(BuilderDeLexema lexema) {
        this.lexema = lexema;
    }

    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo, AnclaDeError ancla) {
        return crearExcepcionLexica(tipo, ancla, null);
    }

    // El fragmento nombra la parte concretamente ofensiva cuando el lexema armado
    // que exige Pautas es más grande que ella. Se escapa: también se imprime.
    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo, AnclaDeError ancla,
                                                String fragmentoOfensivo) {
        return new ExcepcionLexica(tipo, EscapadorDeNoImprimibles.escapar(lexema.getLexema()),
                lexema.posicionDe(ancla), EscapadorDeNoImprimibles.escapar(fragmentoOfensivo));
    }

    // Única con posición explícita: la apertura de un comentario multilínea, cuyo
    // lexema ya descartó EstadoBarra y que no es reconstruible en el throw.
    public ExcepcionLexica crearExcepcionLexicaEn(TipoDeErrorLexico tipo, String lexemaOfensivo,
                                                  PosicionLexica posicion) {
        return new ExcepcionLexica(tipo, EscapadorDeNoImprimibles.escapar(lexemaOfensivo), posicion);
    }
}
