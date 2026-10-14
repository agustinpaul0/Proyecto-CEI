package minijava.semantico.tipos;

import java.util.Optional;

// Qué reemplaza al parámetro de tipo de un ancestro. `extends Caja<String>`
// reemplaza la T de Caja por String; `extends Caja` no reemplaza nada, y la T se
// conserva (documento de semántica §5). Sólo se reemplaza la T que declara ese
// ancestro: una T conservada de más arriba es otro tipo (documento §5.1).
@FunctionalInterface
public interface Sustitucion {

    Optional<Tipo> reemplazoDe(VariableDeTipo parametro);

    static Sustitucion identidad() {
        return parametro -> Optional.empty();
    }

    static Sustitucion de(String declarante, String parametro, Tipo argumento) {
        return variable -> variable.declarante().equals(declarante) && variable.nombre().equals(parametro)
                ? Optional.of(argumento) : Optional.empty();
    }
}
