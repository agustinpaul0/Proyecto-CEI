package minijava.sintactico.partes;

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

    // CreacionDesdeIdClase -> < GenericoDeCreacion | ArregloOInstanciaDeClase
    private void creacionDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_MENOR)) {
            match(TipoDeToken.OP_MENOR);
            genericoDeCreacion();
        } else if (estaEn(PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_ARREGLO_OINSTANCIA_DE_CLASE)) {
            arregloOInstanciaDeClase();
        } else {
            throw errorEsperando(PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_MENOR,
                    PrimerosDeCreacion.CREACION_DESDE_ID_CLASE_ARREGLO_OINSTANCIA_DE_CLASE);
        }
    }

    // GenericoDeCreacion -> > ArgsActuales
    // GenericoDeCreacion -> InstanciadoOParametrico > ArregloOInstanciaDeClase
    //
    // El diamante `<>` se admite sólo en la rama que va a ArgsActuales: `new C<>()`
    // es válido y `new C<>[3]` no. La restricción del logro la impone la gramática,
    // no queda delegada al semántico.
    private void genericoDeCreacion() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.GENERICO_DE_CREACION_MAYOR)) {
            match(TipoDeToken.OP_MAYOR);
            sintaxis.argumentos().argsActuales();
            return;
        }
        sintaxis.tipos().instanciadoOParametrico();
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
    private void creacionDeArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.CORCHETE_A);
        restoCreacionDeArreglo();
    }

    // RestoCreacionDeArreglo -> Expresion ] DimensionesConTamanioOpcional
    // RestoCreacionDeArreglo -> ] DimensionesOpcionales InicializadorDeArreglo
    //
    // Con tamaño es la creación de MiniJava base; sin tamaño, la de Java con
    // inicializador, donde ninguna dimensión lleva tamaño (JLS 15.10.1).
    private void restoCreacionDeArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.RESTO_CREACION_DE_ARREGLO_EXPRESION)) {
            sintaxis.expresiones().expresion();
            match(TipoDeToken.CORCHETE_C);
            dimensionesConTamanioOpcional();
        } else if (estaEn(PrimerosDeCreacion.RESTO_CREACION_DE_ARREGLO_CORCHETE_C)) {
            match(TipoDeToken.CORCHETE_C);
            sintaxis.tipos().dimensionesOpcionales();
            inicializadorDeArreglo();
        } else {
            throw errorEsperando(PrimerosDeCreacion.RESTO_CREACION_DE_ARREGLO_EXPRESION,
                    PrimerosDeCreacion.RESTO_CREACION_DE_ARREGLO_CORCHETE_C);
        }
    }

    // DimensionesConTamanio -> [ Expresion ] DimensionesConTamanioOpcional
    //
    // Voraz: `new int[3][4]` consume el segundo `[` como otra dimensión de la
    // creación y no como un acceso al arreglo recién creado. Es la ambigüedad
    // heredada de la gramática de la cátedra, y coincide con Java.
    private void dimensionesConTamanio() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.CORCHETE_A);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.CORCHETE_C);
        dimensionesConTamanioOpcional();
    }

    // DimensionesConTamanioOpcional -> DimensionesConTamanio | EPSILON
    private void dimensionesConTamanioOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.DIMENSIONES_CON_TAMANIO_OPCIONAL_DIMENSIONES_CON_TAMANIO)) {
            dimensionesConTamanio();
        } else {
            // DimensionesConTamanioOpcional -> EPSILON
        }
    }

    // InicializadorDeArreglo -> { ListaElementosOpcional }
    private void inicializadorDeArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.LLAVE_A);
        listaElementosOpcional();
        match(TipoDeToken.LLAVE_C);
    }

    // ListaElementosOpcional -> ListaElementos | , | EPSILON
    // ListaElementos -> ElementoInicializador RestoListaElementos
    private void listaElementosOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.LISTA_ELEMENTOS_OPCIONAL_LISTA_ELEMENTOS)) {
            elementoInicializador();
            restoListaElementos();
        } else if (estaEn(PrimerosDeCreacion.LISTA_ELEMENTOS_OPCIONAL_COMA)) {
            match(TipoDeToken.COMA);
        } else {
            // ListaElementosOpcional -> EPSILON
        }
    }

    // RestoListaElementos -> , ElementosTrasComa | EPSILON
    private void restoListaElementos() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.RESTO_LISTA_ELEMENTOS_COMA)) {
            match(TipoDeToken.COMA);
            elementosTrasComa();
        } else {
            // RestoListaElementos -> EPSILON
        }
    }

    // ElementosTrasComa -> ElementoInicializador RestoListaElementos | EPSILON
    //
    // La coma final es opcional, como en Java (JLS 10.6): `{1, 2,}` es válido.
    private void elementosTrasComa() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.ELEMENTOS_TRAS_COMA_ELEMENTO_INICIALIZADOR)) {
            elementoInicializador();
            restoListaElementos();
        } else {
            // ElementosTrasComa -> EPSILON
        }
    }

    // ElementoInicializador -> Expresion | InicializadorDeArreglo
    //
    // El inicializador es anidable: `new int[][]{{1,2},{3,4}}`.
    private void elementoInicializador() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.ELEMENTO_INICIALIZADOR_INICIALIZADOR_DE_ARREGLO)) {
            inicializadorDeArreglo();
        } else {
            sintaxis.expresiones().expresion();
        }
    }
}
