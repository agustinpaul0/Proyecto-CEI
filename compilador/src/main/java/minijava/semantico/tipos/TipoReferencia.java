package minijava.semantico.tipos;

import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;

import java.util.Optional;

// `Caja<T>` o `Caja<String>`, y `Caja` sólo si no es genérica. La forma raw de
// un tipo genérico vale únicamente en una cláusula `extends` o `implements`
// (documento §5.2).
public final class TipoReferencia implements Tipo {

    private final Token token;
    private final Tipo argumento;

    public TipoReferencia(Token token) {
        this(token, null);
    }

    public TipoReferencia(Token token, Tipo argumento) {
        this.token = token;
        this.argumento = argumento;
    }

    public Token token() {
        return token;
    }

    public Optional<Tipo> argumento() {
        return Optional.ofNullable(argumento);
    }

    @Override
    public String nombre() {
        return argumento().map(a -> token.lexema() + "<" + a.nombre() + ">").orElse(token.lexema());
    }

    @Override
    public String identidad() {
        return argumento().map(a -> token.lexema() + "<" + a.identidad() + ">").orElse(token.lexema());
    }

    public String nombreDelTipo() {
        return token.lexema();
    }

    public boolean esObject() {
        return argumento == null && nombreDelTipo().equals(TablaDeSimbolos.object().nombre());
    }

    @Override
    public void verificarEn(ClaseOInterfaz contexto, boolean enContextoEstatico) throws ExcepcionSemantica {
        verificarComoAncestroEn(contexto, enContextoEstatico);
        if (argumento == null && declaracion().esGenerico()) {
            throw new ExcepcionSemantica(token, "El tipo " + nombreDelTipo()
                    + " es generico y se usa sin argumento de tipo");
        }
    }

    // En una cláusula de herencia el argumento es optativo (documento §5).
    public void verificarComoAncestroEn(ClaseOInterfaz contexto, boolean enContextoEstatico)
            throws ExcepcionSemantica {
        ClaseOInterfaz declarado = declaracion();
        if (argumento == null) {
            return;
        }
        if (!declarado.esGenerico()) {
            throw new ExcepcionSemantica(token, "El tipo " + nombreDelTipo()
                    + " no es generico y no admite argumento de tipo");
        }
        argumento.verificarEn(contexto, enContextoEstatico);
    }

    public ClaseOInterfaz declaracion() throws ExcepcionSemantica {
        Optional<ClaseOInterfaz> declarado = TablaDeSimbolos.tipo(nombreDelTipo());
        if (declarado.isEmpty()) {
            throw new ExcepcionSemantica(token, "La clase o interfaz " + nombreDelTipo() + " no esta declarada");
        }
        return declarado.get();
    }

    @Override
    public TipoReferencia instanciar(Sustitucion sustitucion) {
        return argumento == null ? this : new TipoReferencia(token, argumento.instanciar(sustitucion));
    }

    // Documento §9.2 y §9.3: conforma con Object y con sus ancestros, recorridos
    // con la sustitución de cada relación; los argumentos son invariantes. Se usa
    // sólo después del chequeo de declaraciones, así que el tipo existe.
    @Override
    public boolean conformaCon(Tipo destino) {
        if (!(destino instanceof TipoReferencia referencia)) {
            return false;
        }
        if (esIgualA(referencia) || referencia.esObject()) {
            return true;
        }
        ClaseOInterfaz declarado = TablaDeSimbolos.tipo(nombreDelTipo()).orElseThrow();
        Sustitucion sustitucion = ClaseOInterfaz.sustitucionHacia(this, declarado);
        for (TipoReferencia ancestro : declarado.ancestrosDirectos()) {
            if (ancestro.conParametroConservado().instanciar(sustitucion).conformaCon(destino)) {
                return true;
            }
        }
        return false;
    }

    // Documento §5: heredar sin argumento conserva el parámetro del ancestro, y es
    // el único que tiene el descendiente. `extends Base` se recorre como
    // `extends Base<T>`, con la T que declara Base.
    private TipoReferencia conParametroConservado() {
        ClaseOInterfaz declarado = TablaDeSimbolos.tipo(nombreDelTipo()).orElseThrow();
        if (argumento != null || !declarado.esGenerico()) {
            return this;
        }
        return new TipoReferencia(token, new VariableDeTipo(declarado.parametroDeTipo().orElseThrow(),
                declarado.nombre()));
    }
}
