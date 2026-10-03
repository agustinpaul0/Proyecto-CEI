package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.Interfaz;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeClases;
import minijava.sintactico.recuperacion.ContextoDeSincronizacion;

import java.util.Optional;

// Las unidades de compilación: clases, interfaces y sus cabeceras. Cada clase o
// interfaz se crea apenas se lee su nombre y queda como la actual de la tabla de
// símbolos: sus miembros y su herencia se le agregan a ella.
public final class AnalizadorDeClases extends ParteDelAnalizador {

    public AnalizadorDeClases(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Inicial -> ListaClases eof
    public void inicial() throws ExcepcionLexica, ExcepcionSintactica {
        listaClases();
        match(TipoDeToken.EOF);
    }

    // ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON
    private void listaClases() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.LISTA_CLASES_CLASE)) {
            if (reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_CLASES, this::clase)) {
                listaClases();
            }
        } else if (estaEn(PrimerosDeClases.LISTA_CLASES_INTERFAZ)) {
            if (reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_CLASES, this::interfaz)) {
                listaClases();
            }
        } else {
            // ListaClases -> EPSILON
        }
    }

    // Clase -> class idClase GenericidadOpcional HerenciaOpcional { ListaMiembros }
    private void clase() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_CLASS);
        Token nombre = actual();
        match(TipoDeToken.ID_CLASE);
        Clase clase = new Clase(nombre);
        TablaDeSimbolos.declararClase(clase);
        genericidadOpcional().ifPresent(clase::setParametroDeTipo);
        herenciaOpcional();
        match(TipoDeToken.LLAVE_A);
        listaMiembros();
        match(TipoDeToken.LLAVE_C);
        clase.cerrar();
    }

    // Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }
    private void interfaz() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_INTERFACE);
        Token nombre = actual();
        match(TipoDeToken.ID_CLASE);
        Interfaz interfaz = new Interfaz(nombre);
        TablaDeSimbolos.declararInterfaz(interfaz);
        genericidadOpcional().ifPresent(interfaz::setParametroDeTipo);
        extensionOpcional();
        match(TipoDeToken.LLAVE_A);
        listaMetodosInterfaz();
        match(TipoDeToken.LLAVE_C);
    }

    // GenericidadOpcional -> < idGen > | EPSILON
    //
    // Una clase o interfaz está limitada a UN parámetro de tipo: el logro de
    // genericidad avanzada agrega el anidamiento, no la cantidad.
    private Optional<Token> genericidadOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.GENERICIDAD_OPCIONAL_MENOR)) {
            match(TipoDeToken.OP_MENOR);
            Token parametro = actual();
            match(TipoDeToken.ID_GEN);
            match(TipoDeToken.OP_MAYOR);
            return Optional.of(parametro);
        } else {
            // GenericidadOpcional -> EPSILON
            return Optional.empty();
        }
    }

    // HerenciaOpcional -> extends TipoReferencia | implements TipoReferencia | EPSILON
    //
    // Sin `extends`, la clase extiende Object (documento de semántica §4).
    private void herenciaOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        Clase clase = TablaDeSimbolos.claseActual();
        if (estaEn(PrimerosDeClases.HERENCIA_OPCIONAL_EXTENDS)) {
            match(TipoDeToken.PR_EXTENDS);
            clase.setPadre(sintaxis.tipos().tipoReferencia());
        } else if (estaEn(PrimerosDeClases.HERENCIA_OPCIONAL_IMPLEMENTS)) {
            match(TipoDeToken.PR_IMPLEMENTS);
            clase.setPadre(TablaDeSimbolos.referenciaAObject(clase.token().linea()));
            clase.setInterfaz(sintaxis.tipos().tipoReferencia());
        } else {
            // HerenciaOpcional -> EPSILON
            clase.setPadre(TablaDeSimbolos.referenciaAObject(clase.token().linea()));
        }
    }

    // ExtensionOpcional -> extends TipoReferencia | EPSILON
    private void extensionOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.EXTENSION_OPCIONAL_EXTENDS)) {
            match(TipoDeToken.PR_EXTENDS);
            TablaDeSimbolos.interfazActual().setExtendida(sintaxis.tipos().tipoReferencia());
        } else {
            // ExtensionOpcional -> EPSILON
        }
    }

    // ListaMiembros -> Miembro ListaMiembros | EPSILON
    private void listaMiembros() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.LISTA_MIEMBROS_MIEMBRO)) {
            if (reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_MIEMBROS,
                    sintaxis.miembros()::miembro)) {
                listaMiembros();
            }
        } else {
            // ListaMiembros -> EPSILON
        }
    }

    // ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz | EPSILON
    //
    // Sincroniza con el contexto de los miembros: los dos son listas adentro de
    // una llave y reanudan en los mismos tokens.
    private void listaMetodosInterfaz() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.LISTA_METODOS_INTERFAZ_METODO_INTERFAZ)) {
            if (reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_MIEMBROS,
                    sintaxis.miembros()::metodoInterfaz)) {
                listaMetodosInterfaz();
            }
        } else {
            // ListaMetodosInterfaz -> EPSILON
        }
    }
}
