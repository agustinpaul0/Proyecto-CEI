package minijava.sintactico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.sintactico.errores.ExcepcionSintactica;

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
    // gramática (Clase 5, slide 61). Que retorne sin excepción y con el eof
    // consumido significa que el programa es sintácticamente válido.
    public void analizar() throws ExcepcionLexica, ExcepcionSintactica {
        entrada.cargarPrimerToken();
        sintaxis.clases().inicial();
    }
}
