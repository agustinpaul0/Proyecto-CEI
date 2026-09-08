package minijava.sintactico.primeros;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.lexico.tokens.TablaDeLexemas;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// Los Primeros del cuerpo de UNA producción. Es lo único que el Esquema Simple
// necesita para elegir qué producción aplicar (Clase 5, slide 34): el analizador
// decide sólo con Primeros, nunca con Siguientes.
//
// Va como constante con nombre y no escrito dentro de cada `if` para que el
// código se pueda leer contra la gramática del informe renglón por renglón, y
// para que un cambio en la gramática tenga un único lugar donde reflejarse.
public final class ConjuntoDePrimeros {

    private final Set<TipoDeToken> tokens;

    private ConjuntoDePrimeros(EnumSet<TipoDeToken> tokens) {
        this.tokens = Collections.unmodifiableSet(tokens);
    }

    public static ConjuntoDePrimeros de(TipoDeToken... tokens) {
        return new ConjuntoDePrimeros(tokens.length == 0
                ? EnumSet.noneOf(TipoDeToken.class)
                : EnumSet.copyOf(List.of(tokens)));
    }

    public ConjuntoDePrimeros unido(ConjuntoDePrimeros otro) {
        EnumSet<TipoDeToken> union = EnumSet.noneOf(TipoDeToken.class);
        union.addAll(tokens);
        union.addAll(otro.tokens);
        return new ConjuntoDePrimeros(union);
    }

    public boolean contiene(Token token) {
        return tokens.contains(token.tipo());
    }

    public Set<TipoDeToken> getTokens() {
        return tokens;
    }

    // Los tokens que un logro apagado saca del lenguaje no pueden aparecer en un
    // mensaje de error: prometerían una entrada que el compilador rechazaría.
    public ConjuntoDePrimeros habilitadosEn(ConfiguracionDelCompilador configuracion) {
        EnumSet<TipoDeToken> habilitados = EnumSet.noneOf(TipoDeToken.class);
        for (TipoDeToken token : tokens) {
            if (TablaDeLexemas.estaHabilitado(token, configuracion)) {
                habilitados.add(token);
            }
        }
        return new ConjuntoDePrimeros(habilitados);
    }

    // Lo que va después de «se esperaba» en el reporte. Un lexema fijo se nombra
    // por su lexema —`{`, `class`—, porque es lo que el programador escribiría;
    // los de lexema variable, por su nombre de token —idClase, intLiteral—.
    public List<String> describir() {
        return tokens.stream()
                .map(tipo -> TablaDeLexemas.lexemaFijoDe(tipo).orElseGet(tipo::getNombre))
                .toList();
    }
}
