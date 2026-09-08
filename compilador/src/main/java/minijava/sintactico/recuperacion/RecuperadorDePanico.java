package minijava.sintactico.recuperacion;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.EntradaDeTokens;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.util.ArrayList;
import java.util.List;

// El logro Recuperación de Errores Modo Pánico. Guarda los errores encontrados y
// descarta entrada hasta un punto donde se pueda reanudar.
//
// Apagado el logro, `habilitado()` es falso y el analizador deja subir la primera
// excepción, que es la convención base de Pautas slide 25: reportar y terminar.
// Prendido, el PRIMER error reportado sigue siendo exactamente el mismo, así que
// un caso del corpus vale para las dos configuraciones.
public final class RecuperadorDePanico {

    private final EntradaDeTokens entrada;
    private final ConfiguracionDelCompilador configuracion;
    private final List<ExcepcionSintactica> errores = new ArrayList<>();

    public RecuperadorDePanico(EntradaDeTokens entrada, ConfiguracionDelCompilador configuracion) {
        this.entrada = entrada;
        this.configuracion = configuracion;
    }

    public boolean habilitado() {
        return configuracion.tiene(Logro.MODO_PANICO);
    }

    public void registrar(ExcepcionSintactica error) {
        errores.add(error);
    }

    public List<ExcepcionSintactica> errores() {
        return List.copyOf(errores);
    }

    // Descarta tokens hasta uno donde el contexto pueda reanudar. Devuelve si la
    // lista puede seguir con otro elemento; false significa que lo que sigue le
    // corresponde a quien llamó: la llave de cierre, o el fin de archivo.
    //
    // Las llaves se cuentan: un `{` que no arranca un elemento de la lista abre
    // algo anidado —el cuerpo del método cuya firma está rota— y se saltea
    // entero, para no leer su contenido como si fuera otro miembro.
    public boolean sincronizar(ContextoDeSincronizacion contexto) throws ExcepcionLexica {
        int anidados = 0;
        while (true) {
            TipoDeToken tipo = entrada.actual().tipo();
            if (tipo == TipoDeToken.EOF) {
                return false;
            }
            if (anidados == 0) {
                if (contexto.cortaLaLista(tipo)) {
                    return false;
                }
                if (contexto.cierraElElemento(tipo)) {
                    entrada.descartar();
                    return true;
                }
                if (contexto.arrancaOtroElemento(entrada.actual())) {
                    return true;
                }
            }
            if (tipo == TipoDeToken.LLAVE_A) {
                anidados++;
            } else if (tipo == TipoDeToken.LLAVE_C) {
                anidados--;
            }
            entrada.descartar();
        }
    }

    // Descartar un token cuando la vuelta entera del bucle no consumió ninguno.
    //
    // Pasa cuando el error cae justo en un token que arranca otro elemento y el
    // elemento vuelve a fallar ahí mismo: `class C { int int; }` falla en el
    // segundo `int`, que también es el comienzo válido de un miembro. Sin esto el
    // analizador quedaría dando vueltas sobre el mismo token.
    public void forzarProgreso() throws ExcepcionLexica {
        if (entrada.actual().tipo() != TipoDeToken.EOF) {
            entrada.descartar();
        }
    }
}
