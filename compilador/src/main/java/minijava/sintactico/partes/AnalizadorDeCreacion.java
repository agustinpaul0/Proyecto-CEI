package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeCreacion;

// La creación con `new` y los inicializadores de arreglo.
public final class AnalizadorDeCreacion extends ParteDelAnalizador {

    public AnalizadorDeCreacion(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Creacion -> new ArregloOInstancia
    public void creacion() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_NEW);
        arregloOInstancia();
    }

    // ArregloOInstancia -> TipoPrimitivo CreacionDeArreglo
    //                    | idGen CreacionDeArreglo
    //                    | idClase CreacionDesdeIdClase
    private void arregloOInstancia() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.ARREGLO_OINSTANCIA_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            creacionDeArreglo();
        } else if (estaEn(PrimerosDeCreacion.ARREGLO_OINSTANCIA_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
            creacionDeArreglo();
        } else if (estaEn(PrimerosDeCreacion.ARREGLO_OINSTANCIA_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            creacionDesdeIdClase();
        } else {
            throw errorEsperando(PrimerosDeCreacion.ARREGLO_OINSTANCIA_TIPO_PRIMITIVO,
                    PrimerosDeCreacion.ARREGLO_OINSTANCIA_ID_GEN, PrimerosDeCreacion.ARREGLO_OINSTANCIA_ID_CLASE);
        }
    }

    // CreacionDesdeIdClase -> < GenericoDeCreacion | CreacionDeArreglo | ArgsActuales
    private void creacionDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_MENOR)) {
            match(TipoDeToken.OP_MENOR);
            genericoDeCreacion();
        } else if (estaEn(PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_CREACION_DE_ARREGLO)) {
            creacionDeArreglo();
        } else if (estaEn(PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_ARGS_ACTUALES)) {
            sintaxis.argumentos().argsActuales();
        } else {
            throw errorEsperando(PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_MENOR,
                    PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_CREACION_DE_ARREGLO,
                    PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_ARGS_ACTUALES);
        }
    }

    // GenericoDeCreacion -> > ArgsActuales   [+generico]
    // GenericoDeCreacion -> ArgumentoDeTipo > ArregloOInstanciaDeClase
    //
    // El diamante `<>` se admite sólo en la rama que va a ArgsActuales: `new C<>()`
    // es válido y `new C<>[3]` no. La restricción del logro la impone la gramática,
    // no queda delegada al semántico.
    private void genericoDeCreacion() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.GENERICO) && estaEn(PrimerosDeCreacion.GENERICO_DE_CREACION_MAYOR)) {
            match(TipoDeToken.OP_MAYOR);
            sintaxis.argumentos().argsActuales();
            return;
        }
        sintaxis.tipos().argumentoDeTipo();
        match(TipoDeToken.OP_MAYOR);
        arregloOInstanciaDeClase();
    }

    // ArregloOInstanciaDeClase -> CreacionDeArreglo | ArgsActuales
    private void arregloOInstanciaDeClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.ARREGLO_OINSTANCIA_DE_CLASE_CREACION_DE_ARREGLO)) {
            creacionDeArreglo();
        } else if (estaEn(PrimerosDeCreacion.ARREGLO_OINSTANCIA_DE_CLASE_ARGS_ACTUALES)) {
            sintaxis.argumentos().argsActuales();
        } else {
            throw errorEsperando(PrimerosDeCreacion.ARREGLO_OINSTANCIA_DE_CLASE_CREACION_DE_ARREGLO,
                    PrimerosDeCreacion.ARREGLO_OINSTANCIA_DE_CLASE_ARGS_ACTUALES);
        }
    }

    // CreacionDeArreglo -> [ RestoCreacionDeArreglo
    // RestoCreacionDeArreglo -> Expresion ] DimensionesConTamanioOpcional
    // RestoCreacionDeArreglo -> ] DimensionesVaciasOpcional InicializadorDeArreglo   [+arrinit]
    private void creacionDeArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.CORCHETE_A);

        if (estaEn(PrimerosDeCreacion.RESTO_CREACION_DE_ARREGLO_CORCHETE_C)) {
            if (!hayLogro(Logro.ARRINIT)) {
                throw errorEsperando(PrimerosDeCreacion.RESTO_CREACION_DE_ARREGLO_EXPRESION);
            }
            match(TipoDeToken.CORCHETE_C);
            dimensionesVaciasOpcional();
            inicializadorDeArreglo();
            return;
        }

        sintaxis.expresiones().expresion();
        match(TipoDeToken.CORCHETE_C);
        dimensionesConTamanioOpcional();
    }

    // DimensionesVaciasOpcional -> [ ] DimensionesVaciasOpcional | EPSILON   [+arrinit]
    private void dimensionesVaciasOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(PrimerosDeCreacion.DIMENSIONES_VACIAS_OPCIONAL_CORCHETE_A)) {
            match(TipoDeToken.CORCHETE_A);
            match(TipoDeToken.CORCHETE_C);
        }
    }

    // DimensionesConTamanio -> [ Expresion ] DimensionesConTamanioOpcional
    // DimensionesConTamanioOpcional -> DimensionesConTamanio | EPSILON
    //
    // Voraz: `new int[3][4]` consume el segundo `[` como otra dimensión de la
    // creación y no como un acceso al arreglo recién creado. Es la ambigüedad
    // heredada de la gramática de la cátedra, y coincide con Java.
    private void dimensionesConTamanioOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(PrimerosDeCreacion.DIMENSIONES_CON_TAMANIO_CORCHETE_A)) {
            match(TipoDeToken.CORCHETE_A);
            sintaxis.expresiones().expresion();
            match(TipoDeToken.CORCHETE_C);
        }
    }

    // InicializadorDeArreglo -> { ListaElementosOpcional }   [+arrinit]
    // ListaElementosOpcional -> ListaElementos | EPSILON
    // ListaElementos -> ElementoInicializador RestoListaElementos
    // RestoListaElementos -> , ElementoInicializador RestoListaElementos | EPSILON
    private void inicializadorDeArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.LLAVE_A);
        if (estaEn(PrimerosDeCreacion.LISTA_ELEMENTOS_ELEMENTO_INICIALIZADOR)) {
            elementoInicializador();
            while (estaEn(PrimerosDeCreacion.RESTO_LISTA_ELEMENTOS_COMA)) {
                match(TipoDeToken.COMA);
                elementoInicializador();
            }
        } else {
            // ListaElementosOpcional -> EPSILON
        }
        match(TipoDeToken.LLAVE_C);
    }

    // ElementoInicializador -> Expresion | InicializadorDeArreglo   [+arrinit]
    //
    // El logro dice «como en Java» y sólo restringe DÓNDE, no cuántas dimensiones:
    // Java admite `new int[][]{{1,2},{3,4}}`, así que el inicializador es anidable.
    private void elementoInicializador() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.ELEMENTO_INICIALIZADOR_INICIALIZADOR_DE_ARREGLO)) {
            inicializadorDeArreglo();
        } else {
            sintaxis.expresiones().expresion();
        }
    }
}
