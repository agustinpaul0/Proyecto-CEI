package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeClases;
import minijava.sintactico.recuperacion.ContextoDeSincronizacion;

// Las unidades de compilación: clases, interfaces y sus cabeceras.
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
        match(TipoDeToken.ID_CLASE);
        genericidadOpcional();
        herenciaOpcional();
        match(TipoDeToken.LLAVE_A);
        listaMiembros();
        match(TipoDeToken.LLAVE_C);
    }

    // Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }
    private void interfaz() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_INTERFACE);
        match(TipoDeToken.ID_CLASE);
        genericidadOpcional();
        extensionOpcional();
        match(TipoDeToken.LLAVE_A);
        listaMetodosInterfaz();
        match(TipoDeToken.LLAVE_C);
    }

    // GenericidadOpcional -> < idGen > | EPSILON
    //
    // Una clase o interfaz sigue limitada a UN parámetro de tipo, también con el
    // logro de genericidad avanzada.
    private void genericidadOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.GENERICIDAD_OPCIONAL_MENOR)) {
            match(TipoDeToken.OP_MENOR);
            match(TipoDeToken.ID_GEN);
            match(TipoDeToken.OP_MAYOR);
        } else {
            // GenericidadOpcional -> EPSILON
        }
    }

    // HerenciaOpcional -> extends TipoReferencia | implements TipoReferencia | EPSILON
    private void herenciaOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.HERENCIA_OPCIONAL_EXTENDS)) {
            match(TipoDeToken.PR_EXTENDS);
            sintaxis.tipos().tipoReferencia();
        } else if (estaEn(PrimerosDeClases.HERENCIA_OPCIONAL_IMPLEMENTS)) {
            match(TipoDeToken.PR_IMPLEMENTS);
            sintaxis.tipos().tipoReferencia();
        } else {
            // HerenciaOpcional -> EPSILON
        }
    }

    // ExtensionOpcional -> extends TipoReferencia | EPSILON
    private void extensionOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeClases.EXTENSION_OPCIONAL_EXTENDS)) {
            match(TipoDeToken.PR_EXTENDS);
            sintaxis.tipos().tipoReferencia();
        } else {
            // ExtensionOpcional -> EPSILON
        }
    }

    // ListaMiembros -> Miembro ListaMiembros | EPSILON
    private void listaMiembros() throws ExcepcionLexica, ExcepcionSintactica {
        if (!estaEn(PrimerosDeClases.LISTA_MIEMBROS_MIEMBRO)) {
            return;
        }
        if (!reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_MIEMBROS,
                sintaxis.miembros()::miembro)) {
            return;
        }
        listaMiembros();
    }

    // ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz | EPSILON
    private void listaMetodosInterfaz() throws ExcepcionLexica, ExcepcionSintactica {
        if (!estaEn(PrimerosDeClases.LISTA_METODOS_INTERFAZ_METODO_INTERFAZ)) {
            return;
        }
        if (!reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_MIEMBROS,
                sintaxis.miembros()::metodoInterfaz)) {
            return;
        }
        listaMetodosInterfaz();
    }
}
