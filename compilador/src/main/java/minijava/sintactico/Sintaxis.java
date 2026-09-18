package minijava.sintactico;

import minijava.sintactico.partes.AnalizadorDeArgumentos;
import minijava.sintactico.partes.AnalizadorDeClases;
import minijava.sintactico.partes.AnalizadorDeCreacion;
import minijava.sintactico.partes.AnalizadorDeExpresiones;
import minijava.sintactico.partes.AnalizadorDeFors;
import minijava.sintactico.partes.AnalizadorDeMiembros;
import minijava.sintactico.partes.AnalizadorDeReferencias;
import minijava.sintactico.partes.AnalizadorDeSentencias;
import minijava.sintactico.partes.AnalizadorDeTipos;
import minijava.sintactico.recuperacion.RecuperadorDePanico;

// Las nueve secciones de la gramática, cableadas.
//
// La gramática de MiniJava tiene recursión mutua entre secciones —una sentencia
// contiene expresiones, una expresión contiene referencias, y una expresión
// parentizada contiene otra expresión—, así que las partes no se pueden
// inyectar unas en otras por constructor sin un ciclo. Cada parte recibe
// esta fachada y le pide el colaborador EN EL MOMENTO de usarlo, que es cuando
// ya está construido.
public final class Sintaxis {

    private final EntradaDeTokens entrada;
    private final RecuperadorDePanico recuperador;

    private final AnalizadorDeClases clases;
    private final AnalizadorDeMiembros miembros;
    private final AnalizadorDeTipos tipos;
    private final AnalizadorDeSentencias sentencias;
    private final AnalizadorDeFors fors;
    private final AnalizadorDeExpresiones expresiones;
    private final AnalizadorDeReferencias referencias;
    private final AnalizadorDeCreacion creacion;
    private final AnalizadorDeArgumentos argumentos;

    public Sintaxis(EntradaDeTokens entrada) {
        this.entrada = entrada;
        this.recuperador = new RecuperadorDePanico(entrada);

        this.clases = new AnalizadorDeClases(this);
        this.miembros = new AnalizadorDeMiembros(this);
        this.tipos = new AnalizadorDeTipos(this);
        this.sentencias = new AnalizadorDeSentencias(this);
        this.fors = new AnalizadorDeFors(this);
        this.expresiones = new AnalizadorDeExpresiones(this);
        this.referencias = new AnalizadorDeReferencias(this);
        this.creacion = new AnalizadorDeCreacion(this);
        this.argumentos = new AnalizadorDeArgumentos(this);
    }

    public EntradaDeTokens entrada() {
        return entrada;
    }

    public RecuperadorDePanico recuperador() {
        return recuperador;
    }

    public AnalizadorDeClases clases() {
        return clases;
    }

    public AnalizadorDeMiembros miembros() {
        return miembros;
    }

    public AnalizadorDeTipos tipos() {
        return tipos;
    }

    public AnalizadorDeSentencias sentencias() {
        return sentencias;
    }

    public AnalizadorDeFors fors() {
        return fors;
    }

    public AnalizadorDeExpresiones expresiones() {
        return expresiones;
    }

    public AnalizadorDeReferencias referencias() {
        return referencias;
    }

    public AnalizadorDeCreacion creacion() {
        return creacion;
    }

    public AnalizadorDeArgumentos argumentos() {
        return argumentos;
    }
}
