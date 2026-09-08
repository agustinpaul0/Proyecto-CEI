package minijava.sintactico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.sintactico.partes.AnalizadorDeClases;
import minijava.sintactico.partes.AnalizadorDeCreacion;
import minijava.sintactico.partes.AnalizadorDeExpresiones;
import minijava.sintactico.partes.AnalizadorDeFors;
import minijava.sintactico.partes.AnalizadorDeLambdas;
import minijava.sintactico.partes.AnalizadorDeMiembros;
import minijava.sintactico.partes.AnalizadorDeReferencias;
import minijava.sintactico.partes.AnalizadorDeSentencias;
import minijava.sintactico.partes.AnalizadorDeTipos;

// Las nueve secciones de la gramática, cableadas.
//
// La gramática de MiniJava tiene recursión mutua entre secciones —una sentencia
// contiene expresiones, una expresión contiene lambdas, y el cuerpo de una
// lambda vuelve a ser una expresión—, así que las partes no se pueden inyectar
// unas en otras por constructor sin un ciclo. Cada parte recibe esta fachada y
// le pide el colaborador EN EL MOMENTO de usarlo, que es cuando ya está
// construido.
public final class Sintaxis {

    private final ConfiguracionDelCompilador configuracion;
    private final EntradaDeTokens entrada;
    private final RestriccionesDeLambda restriccionesDeLambda;

    private final AnalizadorDeClases clases;
    private final AnalizadorDeMiembros miembros;
    private final AnalizadorDeTipos tipos;
    private final AnalizadorDeSentencias sentencias;
    private final AnalizadorDeFors fors;
    private final AnalizadorDeExpresiones expresiones;
    private final AnalizadorDeReferencias referencias;
    private final AnalizadorDeLambdas lambdas;
    private final AnalizadorDeCreacion creacion;

    public Sintaxis(EntradaDeTokens entrada, ConfiguracionDelCompilador configuracion) {
        this.entrada = entrada;
        this.configuracion = configuracion;
        this.restriccionesDeLambda = new RestriccionesDeLambda();

        this.clases = new AnalizadorDeClases(this);
        this.miembros = new AnalizadorDeMiembros(this);
        this.tipos = new AnalizadorDeTipos(this);
        this.sentencias = new AnalizadorDeSentencias(this);
        this.fors = new AnalizadorDeFors(this);
        this.expresiones = new AnalizadorDeExpresiones(this);
        this.referencias = new AnalizadorDeReferencias(this);
        this.lambdas = new AnalizadorDeLambdas(this);
        this.creacion = new AnalizadorDeCreacion(this);
    }

    public EntradaDeTokens entrada() {
        return entrada;
    }

    public ConfiguracionDelCompilador configuracion() {
        return configuracion;
    }

    public RestriccionesDeLambda restriccionesDeLambda() {
        return restriccionesDeLambda;
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

    public AnalizadorDeLambdas lambdas() {
        return lambdas;
    }

    public AnalizadorDeCreacion creacion() {
        return creacion;
    }
}
