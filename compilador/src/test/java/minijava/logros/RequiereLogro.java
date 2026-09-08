package minijava.logros;

import minijava.comun.config.Logro;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// El test sólo tiene sentido con estos logros PRENDIDOS en la corrida. Si alguno
// está apagado el test se SALTEA, no falla: que un logro esté apagado es una
// configuración legítima del compilador, no un defecto.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(CondicionDeLogro.class)
public @interface RequiereLogro {
    Logro[] value();
}
