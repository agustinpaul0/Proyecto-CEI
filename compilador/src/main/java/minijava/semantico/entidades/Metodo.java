package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;

import java.util.ArrayList;
import java.util.List;

public final class Metodo extends Unidad implements Miembro {

    private boolean estatico;
    private Tipo retorno;

    public Metodo(Token nombre) {
        super(nombre);
    }

    private Metodo(Token nombre, List<Parametro> parametros, boolean estatico, Tipo retorno) {
        super(nombre, parametros);
        this.estatico = estatico;
        this.retorno = retorno;
    }

    public void hacerEstatico() {
        estatico = true;
    }

    public void setRetorno(Tipo retorno) {
        this.retorno = retorno;
    }

    public boolean esEstatico() {
        return estatico;
    }

    public Tipo retorno() {
        return retorno;
    }

    public ClaveDeMetodo clave() {
        return new ClaveDeMetodo(nombre(), aridad());
    }

    // La signatura de la redefinición (documento §6.2): los tipos de los
    // parámetros en orden y el retorno. Los nombres de los parámetros no cuentan.
    public boolean mismaSignaturaQue(Metodo otro) {
        if (aridad() != otro.aridad() || !retorno.esIgualA(otro.retorno)) {
            return false;
        }
        for (int i = 0; i < aridad(); i++) {
            if (!parametros().get(i).tipo().esIgualA(otro.parametros().get(i).tipo())) {
                return false;
            }
        }
        return true;
    }

    void estaBienDeclarado(TipoDeclarado contexto) throws ExcepcionSemantica {
        retorno.verificarEn(contexto, estatico);
        verificarParametrosEn(contexto, estatico);
    }

    Metodo instanciado(Sustitucion sustitucion) {
        List<Parametro> instanciados = new ArrayList<>();
        for (Parametro parametro : parametros()) {
            instanciados.add(parametro.instanciado(sustitucion));
        }
        return new Metodo(token(), instanciados, estatico, retorno.instanciar(sustitucion));
    }
}
