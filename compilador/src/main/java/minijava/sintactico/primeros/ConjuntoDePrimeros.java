package minijava.sintactico.primeros;

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

    // Lo que va después de «se esperaba» en el reporte. Un lexema fijo se nombra
    // por su lexema —`{`, `class`—, porque es lo que el programador escribiría;
    // los de lexema variable, por su nombre de token —idClase, intLiteral—.
    public List<String> describir() {
        return tokens.stream()
                .map(tipo -> TablaDeLexemas.lexemaFijoDe(tipo).orElseGet(tipo::getNombre))
                .toList();
    }
}
