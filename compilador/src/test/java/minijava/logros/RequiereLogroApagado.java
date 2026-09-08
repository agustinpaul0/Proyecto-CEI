package minijava.logros;

import minijava.comun.config.Logro;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// El complemento de RequiereLogro: el test verifica el comportamiento BASE, el
// que la cátedra especifica sin el logro. Se saltea si el logro está prendido.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(CondicionDeLogro.class)
public @interface RequiereLogroApagado {
    Logro[] value();
}
