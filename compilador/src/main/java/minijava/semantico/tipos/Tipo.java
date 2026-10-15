package minijava.semantico.tipos;

import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.errores.ExcepcionSemantica;

public sealed interface Tipo permits TipoPrimitivo, TipoVoid, TipoNull, TipoReferencia, VariableDeTipo, TipoArreglo {

    String nombre();

    // `contexto` es la clase o interfaz donde aparece el tipo: es la que decide qué
    // parámetro de tipo es válido. Un método estático no puede usar ese parámetro
    // (JLS §8.1.2), por eso viaja `enContextoEstatico`.
    void verificarEn(ClaseOInterfaz contexto, boolean enContextoEstatico) throws ExcepcionSemantica;

    Tipo instanciar(Sustitucion sustitucion);

    // Documento §9: si un valor de este tipo puede usarse donde se exige `destino`.
    boolean conformaCon(Tipo destino);

    // El nombre sin ambigüedad. Coincide con `nombre()` salvo para un parámetro de
    // tipo, que suma la clase o interfaz que lo declara (documento §5.1): la `T`
    // que `Derivada extends Base` conserva de `Base` no es la `T` de otra clase.
    default String identidad() {
        return nombre();
    }

    default boolean esIgualA(Tipo otro) {
        return identidad().equals(otro.identidad());
    }
}
