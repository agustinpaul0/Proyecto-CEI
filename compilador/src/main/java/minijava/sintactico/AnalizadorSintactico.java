package minijava.sintactico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.util.List;

// Analizador Descendente Recursivo Predictivo LL(1), Esquema Simple de la
// Clase 5 (slides 33-62): un método por no terminal, `match` para los terminales,
// y la producción se elige mirando si el token actual está en los Primeros del
// cuerpo de alguna de ellas. Sólo Primeros: los Siguientes no aparecen en
// ninguna línea del compilador.
//
// Las reglas viven en sintactico/partes/, una clase por sección de la gramática;
// acá quedan el arranque y el contrato de uso.
public final class AnalizadorSintactico {

    private final EntradaDeTokens entrada;
    private final Sintaxis sintaxis;

    public AnalizadorSintactico(AnalizadorLexico analizadorLexico,
                                ConfiguracionDelCompilador configuracion) {
        this.entrada = new EntradaDeTokens(analizadorLexico, configuracion);
        this.sintaxis = new Sintaxis(entrada, configuracion);
    }

    // Se carga el primer token y se arranca por el símbolo inicial de la
    // gramática (Clase 5, slide 61). Devuelve los errores encontrados: una lista
    // vacía significa que el programa es sintácticamente válido.
    //
    // Sin Modo Pánico la lista tiene a lo sumo un elemento, porque la primera
    // excepción sube sin que nadie la recupere; con el logro prendido el
    // analizador sigue y los junta todos, en el orden en que aparecen.
    public List<ExcepcionSintactica> analizar() throws ExcepcionLexica {
        entrada.cargarPrimerToken();
        try {
            sintaxis.clases().inicial();
        } catch (ExcepcionSintactica error) {
            if (!sintaxis.recuperador().habilitado()) {
                return List.of(error);
            }
            // Con el logro prendido, un error que nadie recuperó igual se suma a
            // los que ya se habían encontrado: es el último de la lista, no el
            // único.
            sintaxis.recuperador().registrar(error);
        }
        return sintaxis.recuperador().errores();
    }

    // Los errores sintácticos encontrados hasta ahora. Hace falta cuando `analizar`
    // corta por un error LÉXICO: con Modo Pánico puede haber recuperado varios
    // antes de que el léxico se tope con un símbolo que no existe, y perderlos
    // sería reportar de menos.
    public List<ExcepcionSintactica> erroresDetectados() {
        return sintaxis.recuperador().errores();
    }
}
