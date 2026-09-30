package minijava.semantico.tipos;

import minijava.semantico.entidades.TipoDeclarado;
import minijava.semantico.errores.ExcepcionSemantica;

public sealed interface Tipo permits TipoPrimitivo, TipoVoid, TipoReferencia, VariableDeTipo, TipoArreglo {

    String nombre();

    // `contexto` es la clase o interfaz donde aparece el tipo: es la que decide qué
    // parámetro de tipo es válido. Un método estático no puede usar ese parámetro
    // (JLS §8.1.2), por eso viaja `enContextoEstatico`.
    void verificarEn(TipoDeclarado contexto, boolean enContextoEstatico) throws ExcepcionSemantica;

    Tipo instanciar(Sustitucion sustitucion);

    // Dos tipos son el mismo si se escriben igual: los nombres de clase tienen al
    // menos dos caracteres y los parámetros de tipo uno solo, así que el nombre no
    // confunde un parámetro con una clase.
    default boolean esIgualA(Tipo otro) {
        return nombre().equals(otro.nombre());
    }
}
