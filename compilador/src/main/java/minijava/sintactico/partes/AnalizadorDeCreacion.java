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

    // GenericoDeCreacion -> > ArgsActuales
    // GenericoDeCreacion -> ArgumentoDeTipo > ArregloOInstanciaDeClase
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

    // CreacionDeArreglo -> DimensionesConTamanio InicializadorDeArregloOpcional
    //
    // Toda dimensión lleva su tamaño, como en MiniJava base; el inicializador se
    // le cuelga al final. `new int[2][2]{{1,2},{3,4}}` es válido y
    // `new int[]{1,2,3}` no: ver el comentario de la producción en el .g.
    private void creacionDeArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        dimensionesConTamanio();
        inicializadorDeArregloOpcional();
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

    // InicializadorDeArregloOpcional -> InicializadorDeArreglo | EPSILON
    private void inicializadorDeArregloOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.INICIALIZADOR_DE_ARREGLO_OPCIONAL_INICIALIZADOR_DE_ARREGLO)) {
            inicializadorDeArreglo();
        } else {
            // InicializadorDeArregloOpcional -> EPSILON
        }
    }

    // InicializadorDeArreglo -> { ListaElementosOpcional }
    // ListaElementosOpcional -> ListaElementos | EPSILON
    // ListaElementos -> ElementoInicializador RestoListaElementos
    private void inicializadorDeArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.LLAVE_A);
        if (estaEn(PrimerosDeCreacion.LISTA_ELEMENTOS_ELEMENTO_INICIALIZADOR)) {
            elementoInicializador();
            restoListaElementos();
        } else {
            // ListaElementosOpcional -> EPSILON
        }
        match(TipoDeToken.LLAVE_C);
    }

    // RestoListaElementos -> , ElementoInicializador RestoListaElementos | EPSILON
    private void restoListaElementos() throws ExcepcionLexica, ExcepcionSintactica {
        if (!estaEn(PrimerosDeCreacion.RESTO_LISTA_ELEMENTOS_COMA)) {
            return;
        }
        match(TipoDeToken.COMA);
        elementoInicializador();
        restoListaElementos();
    }

    // ElementoInicializador -> Expresion | InicializadorDeArreglo
    //
    // El inicializador es anidable: `new int[2][2]{{1,2},{3,4}}`.
    private void elementoInicializador() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeCreacion.ELEMENTO_INICIALIZADOR_INICIALIZADOR_DE_ARREGLO)) {
            inicializadorDeArreglo();
        } else {
            sintaxis.expresiones().expresion();
        }
    }
}
