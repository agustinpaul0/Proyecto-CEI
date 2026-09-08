package minijava.sintactico.recuperacion;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.primeros.ConjuntoDePrimeros;
import minijava.sintactico.primeros.PrimerosDeClases;
import minijava.sintactico.primeros.PrimerosDeSentencias;

import java.util.Set;

// Dónde se puede reanudar el análisis después de un error, según el contexto.
// Pautas Etapa 2, slide 40: «se espera que se sincronice con el siguiente punto
// y coma, llave de cierre, o llave que abre segun el contexto».
//
// Los tres contextos son las tres listas de la gramática que pueden repetir un
// elemento —clases, miembros y sentencias—, que es donde tiene sentido descartar
// un elemento roto y seguir con el siguiente.
//
// A los tokens de la slide se les suman los PRIMEROS del elemento de la lista.
// Sin eso, `int x` al que le falta el `;` se lleva puesto el método que viene
// después: el análisis fallaría en el `void`, que es justo donde empieza el
// miembro siguiente y correcto.
public enum ContextoDeSincronizacion {

    LISTA_DE_CLASES(
            PrimerosDeClases.LISTA_CLASES_CLASE.unido(PrimerosDeClases.LISTA_CLASES_INTERFAZ),
            Set.of(),
            Set.of(TipoDeToken.LLAVE_C)),

    LISTA_DE_MIEMBROS(
            PrimerosDeClases.LISTA_MIEMBROS_MIEMBRO,
            Set.of(TipoDeToken.LLAVE_C),
            Set.of(TipoDeToken.PUNTO_Y_COMA)),

    LISTA_DE_SENTENCIAS(
            PrimerosDeSentencias.LISTA_SENTENCIAS_SENTENCIA,
            Set.of(TipoDeToken.LLAVE_C),
            Set.of(TipoDeToken.PUNTO_Y_COMA));

    private final ConjuntoDePrimeros arrancanOtroElemento;
    private final Set<TipoDeToken> cortanLaLista;
    private final Set<TipoDeToken> cierranElElemento;

    ContextoDeSincronizacion(ConjuntoDePrimeros arrancanOtroElemento,
                             Set<TipoDeToken> cortanLaLista,
                             Set<TipoDeToken> cierranElElemento) {
        this.arrancanOtroElemento = arrancanOtroElemento;
        this.cortanLaLista = cortanLaLista;
        this.cierranElElemento = cierranElElemento;
    }

    // El token cierra el elemento roto: se lo consume y se sigue con el siguiente.
    boolean cierraElElemento(TipoDeToken tipo) {
        return cierranElElemento.contains(tipo);
    }

    // El token le pertenece a quien llamó —la llave que cierra el bloque o la
    // clase, o el fin de archivo—: la lista termina acá.
    boolean cortaLaLista(TipoDeToken tipo) {
        return cortanLaLista.contains(tipo);
    }

    // El token ya arranca otro elemento de la lista: se reanuda sin consumirlo.
    boolean arrancaOtroElemento(Token token) {
        return arrancanOtroElemento.contiene(token);
    }
}
