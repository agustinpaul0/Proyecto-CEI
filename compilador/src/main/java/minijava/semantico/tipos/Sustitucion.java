package minijava.semantico.tipos;

import java.util.Optional;

// Qué reemplaza al parámetro de tipo de un ancestro. `extends Caja<String>`
// reemplaza la T de Caja por String; `extends Caja` no reemplaza nada, y la T se
// conserva (documento de semántica §5).
@FunctionalInterface
public interface Sustitucion {

    Optional<Tipo> reemplazoDe(String nombre);

    static Sustitucion identidad() {
        return nombre -> Optional.empty();
    }

    static Sustitucion de(String parametro, Tipo argumento) {
        return nombre -> parametro.equals(nombre) ? Optional.of(argumento) : Optional.empty();
    }
}
