package minijava.semantico.tipos;

import java.util.Optional;

// Qué reemplaza al parámetro de tipo de un ancestro. `extends Caja<String>`
// reemplaza la T de Caja por String; `extends Caja` no reemplaza nada, y la T se
// conserva (documento de semántica §5).
public record Sustitucion(Optional<String> parametro, Optional<Tipo> argumento) {

    public static Sustitucion identidad() {
        return new Sustitucion(Optional.empty(), Optional.empty());
    }

    public static Sustitucion de(String parametro, Tipo argumento) {
        return new Sustitucion(Optional.of(parametro), Optional.of(argumento));
    }

    public Optional<Tipo> reemplazoDe(String nombre) {
        return parametro.filter(nombre::equals).flatMap(p -> argumento);
    }
}
