package minijava.sintactico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TablaDeLexemas;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.ConjuntoDePrimeros;
import minijava.sintactico.primeros.PrimerosDelNucleo;

// Analizador Descendente Recursivo Predictivo LL(1), Esquema Simple de la
// Clase 5 (slides 33-62): un método privado por no terminal, `match` para los
// terminales, y la producción se elige mirando si el token actual está en los
// Primeros del cuerpo de alguna de ellas.
//
// COBERTURA ACTUAL: los no terminales del núcleo hasta ListaMiembros y
// ListaMetodosInterfaz, que hoy sólo derivan a vacío. El compilador acepta
// clases e interfaces sin miembros y rechaza el resto con el error del token
// exacto. Los miembros y las sentencias son la Sesión 3; el pendiente está
// anotado en documentacion/proyecto/plan-de-accion-etapa-2.md.
public final class AnalizadorSintactico {

    private final AnalizadorLexico analizadorLexico;
    private final ConfiguracionDelCompilador configuracion;
    private Token tokenActual;

    public AnalizadorSintactico(AnalizadorLexico analizadorLexico,
                                ConfiguracionDelCompilador configuracion) {
        this.analizadorLexico = analizadorLexico;
        this.configuracion = configuracion;
    }

    public void analizar() throws ExcepcionLexica, ExcepcionSintactica {
        tokenActual = analizadorLexico.proximoToken();
        inicial();
    }

    // El único punto que consume entrada: por eso la posición de cualquier error
    // es siempre la del token actual, sin contabilidad aparte.
    private void match(TipoDeToken esperado) throws ExcepcionLexica, ExcepcionSintactica {
        if (tokenActual.tipo() != esperado) {
            throw new ExcepcionSintactica(tokenActual, nombreDe(esperado));
        }
        tokenActual = analizadorLexico.proximoToken();
    }

    private ExcepcionSintactica errorEsperando(ConjuntoDePrimeros... alternativas) {
        ConjuntoDePrimeros esperados = ConjuntoDePrimeros.de();
        for (ConjuntoDePrimeros alternativa : alternativas) {
            esperados = esperados.unido(alternativa);
        }
        return new ExcepcionSintactica(tokenActual, esperados.habilitadosEn(configuracion).describir());
    }

    private static String nombreDe(TipoDeToken tipo) {
        return TablaDeLexemas.lexemaFijoDe(tipo).orElseGet(tipo::getNombre);
    }

    // <Inicial> ::= <ListaClases> eof
    private void inicial() throws ExcepcionLexica, ExcepcionSintactica {
        listaClases();
        match(TipoDeToken.EOF);
    }

    // <ListaClases> ::= <Clase> <ListaClases> | <Interfaz> <ListaClases> | ε
    private void listaClases() throws ExcepcionLexica, ExcepcionSintactica {
        if (PrimerosDelNucleo.CLASE.contiene(tokenActual)) {
            clase();
            listaClases();
        } else if (PrimerosDelNucleo.INTERFAZ.contiene(tokenActual)) {
            interfaz();
            listaClases();
        }
    }

    // <Clase> ::= class idClase <GenericidadOpcional> <HerenciaOpcional> { <ListaMiembros> }
    private void clase() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_CLASS);
        match(TipoDeToken.ID_CLASE);
        genericidadOpcional();
        herenciaOpcional();
        match(TipoDeToken.LLAVE_A);
        listaMiembros();
        match(TipoDeToken.LLAVE_C);
    }

    // <Interfaz> ::= interface idClase <GenericidadOpcional> <ExtensionOpcional> { <ListaMetodosInterfaz> }
    private void interfaz() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_INTERFACE);
        match(TipoDeToken.ID_CLASE);
        genericidadOpcional();
        extensionOpcional();
        match(TipoDeToken.LLAVE_A);
        listaMetodosInterfaz();
        match(TipoDeToken.LLAVE_C);
    }

    // <GenericidadOpcional> ::= < idGen > | ε
    private void genericidadOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (PrimerosDelNucleo.GENERICIDAD.contiene(tokenActual)) {
            match(TipoDeToken.OP_MENOR);
            match(TipoDeToken.ID_GEN);
            match(TipoDeToken.OP_MAYOR);
        }
    }

    // <HerenciaOpcional> ::= extends <TipoReferencia> | implements <TipoReferencia> | ε
    private void herenciaOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (PrimerosDelNucleo.HERENCIA_POR_EXTENDS.contiene(tokenActual)) {
            match(TipoDeToken.PR_EXTENDS);
            tipoReferencia();
        } else if (PrimerosDelNucleo.HERENCIA_POR_IMPLEMENTS.contiene(tokenActual)) {
            match(TipoDeToken.PR_IMPLEMENTS);
            tipoReferencia();
        }
    }

    // <ExtensionOpcional> ::= extends <TipoReferencia> | ε
    private void extensionOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (PrimerosDelNucleo.HERENCIA_POR_EXTENDS.contiene(tokenActual)) {
            match(TipoDeToken.PR_EXTENDS);
            tipoReferencia();
        }
    }

    // <TipoReferencia> ::= idClase <TipoGenericoOpcional>
    private void tipoReferencia() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.ID_CLASE);
        tipoGenericoOpcional();
    }

    // <TipoGenericoOpcional> ::= < <ArgumentoDeTipo> > | ε
    private void tipoGenericoOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (PrimerosDelNucleo.TIPO_GENERICO.contiene(tokenActual)) {
            match(TipoDeToken.OP_MENOR);
            argumentoDeTipo();
            match(TipoDeToken.OP_MAYOR);
        }
    }

    // <ArgumentoDeTipo> ::= idGen
    //                     | idClase <TipoGenericoOpcional>   [+generico]
    //                     | idClase                          [-generico]
    private void argumentoDeTipo() throws ExcepcionLexica, ExcepcionSintactica {
        if (PrimerosDelNucleo.ARGUMENTO_DE_TIPO_PARAMETRO.contiene(tokenActual)) {
            match(TipoDeToken.ID_GEN);
        } else if (PrimerosDelNucleo.ARGUMENTO_DE_TIPO_CLASE.contiene(tokenActual)) {
            match(TipoDeToken.ID_CLASE);
            if (configuracion.tiene(Logro.GENERICO)) {
                tipoGenericoOpcional();
            }
        } else {
            throw errorEsperando(PrimerosDelNucleo.ARGUMENTO_DE_TIPO);
        }
    }

    // <ListaMiembros> ::= <Miembro> <ListaMiembros> | ε
    // La rama de <Miembro> llega en la Sesión 3.
    private void listaMiembros() {
    }

    // <ListaMetodosInterfaz> ::= <MetodoInterfaz> <ListaMetodosInterfaz> | ε
    // La rama de <MetodoInterfaz> llega en la Sesión 3.
    private void listaMetodosInterfaz() {
    }
}
