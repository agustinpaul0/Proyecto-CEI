package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;

public final class Constructor extends Unidad implements Miembro {

    public Constructor(Token nombre) {
        super(nombre);
    }

    void estaBienDeclarado(Clase clase) throws ExcepcionSemantica {
        if (!nombre().equals(clase.nombre())) {
            throw new ExcepcionSemantica(token(), "El constructor " + nombre()
                    + " no tiene el nombre de su clase, " + clase.nombre());
        }
        verificarParametrosEn(clase, false);
    }
}
