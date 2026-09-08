package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.Primeros;

// Las unidades de compilación: clases, interfaces y sus cabeceras.
public final class AnalizadorDeClases extends ParteDelAnalizador {

    public AnalizadorDeClases(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Inicial -> ListaClases eof
    public void inicial() throws ExcepcionLexica, ExcepcionSintactica {
        listaClases();
        entrada().match(TipoDeToken.EOF);
    }

    // ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON
    private void listaClases() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(Primeros.LISTA_CLASES_CLASE) || estaEn(Primeros.LISTA_CLASES_INTERFAZ)) {
            if (estaEn(Primeros.LISTA_CLASES_CLASE)) {
                clase();
            } else {
                interfaz();
            }
        }
    }

    // Clase -> class idClase GenericidadOpcional HerenciaOpcional { ListaMiembros }
    private void clase() throws ExcepcionLexica, ExcepcionSintactica {
        entrada().match(TipoDeToken.PR_CLASS);
        entrada().match(TipoDeToken.ID_CLASE);
        genericidadOpcional();
        herenciaOpcional();
        entrada().match(TipoDeToken.LLAVE_A);
        listaMiembros();
        entrada().match(TipoDeToken.LLAVE_C);
    }

    // Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }
    private void interfaz() throws ExcepcionLexica, ExcepcionSintactica {
        entrada().match(TipoDeToken.PR_INTERFACE);
        entrada().match(TipoDeToken.ID_CLASE);
        genericidadOpcional();
        extensionOpcional();
        entrada().match(TipoDeToken.LLAVE_A);
        listaMetodosInterfaz();
        entrada().match(TipoDeToken.LLAVE_C);
    }

    // GenericidadOpcional -> < idGen > | EPSILON
    //
    // Una clase o interfaz sigue limitada a UN parámetro de tipo, también con el
    // logro de genericidad avanzada.
    private void genericidadOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.GENERICIDAD_OPCIONAL_MENOR)) {
            entrada().match(TipoDeToken.OP_MENOR);
            entrada().match(TipoDeToken.ID_GEN);
            entrada().match(TipoDeToken.OP_MAYOR);
        }
    }

    // HerenciaOpcional -> extends TipoReferencia | implements TipoReferencia | EPSILON
    private void herenciaOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.HERENCIA_OPCIONAL_EXTENDS)) {
            entrada().match(TipoDeToken.PR_EXTENDS);
            sintaxis.tipos().tipoReferencia();
        } else if (estaEn(Primeros.HERENCIA_OPCIONAL_IMPLEMENTS)) {
            entrada().match(TipoDeToken.PR_IMPLEMENTS);
            sintaxis.tipos().tipoReferencia();
        }
    }

    // ExtensionOpcional -> extends TipoReferencia | EPSILON
    private void extensionOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.EXTENSION_OPCIONAL_EXTENDS)) {
            entrada().match(TipoDeToken.PR_EXTENDS);
            sintaxis.tipos().tipoReferencia();
        }
    }

    // ListaMiembros -> Miembro ListaMiembros | EPSILON
    private void listaMiembros() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(Primeros.LISTA_MIEMBROS_MIEMBRO)) {
            sintaxis.miembros().miembro();
        }
    }

    // ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz | EPSILON
    private void listaMetodosInterfaz() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(Primeros.LISTA_METODOS_INTERFAZ_METODO_INTERFAZ)) {
            sintaxis.miembros().metodoInterfaz();
        }
    }
}
