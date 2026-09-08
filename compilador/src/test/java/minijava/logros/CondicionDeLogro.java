package minijava.logros;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Decide si un test corre según los logros de la corrida (-Dlogros). Una sola
// condición para las dos anotaciones, porque un mismo elemento puede llevar las
// dos: «con ternario prendido y modo pánico apagado».
//
// Las anotaciones de la CLASE también valen para sus métodos: JUnit evalúa la
// condición en los dos niveles, así que basta con mirar el elemento de cada uno.
public final class CondicionDeLogro implements ExecutionCondition {

    private static final ConditionEvaluationResult CORRE =
            ConditionEvaluationResult.enabled("No hay ninguna restriccion de logros");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext contexto) {
        Optional<AnnotatedElement> elemento = contexto.getElement();
        if (elemento.isEmpty()) {
            return CORRE;
        }

        ConfiguracionDelCompilador configuracion = ConfiguracionDeLaCorrida.actual();
        List<String> faltantes = new ArrayList<>();

        AnnotationSupport.findAnnotation(elemento.get(), RequiereLogro.class)
                .ifPresent(anotacion -> {
                    for (Logro logro : anotacion.value()) {
                        if (!configuracion.tiene(logro)) {
                            faltantes.add(logro.getNombre() + " tiene que estar prendido");
                        }
                    }
                });

        AnnotationSupport.findAnnotation(elemento.get(), RequiereLogroApagado.class)
                .ifPresent(anotacion -> {
                    for (Logro logro : anotacion.value()) {
                        if (configuracion.tiene(logro)) {
                            faltantes.add(logro.getNombre() + " tiene que estar apagado");
                        }
                    }
                });

        if (faltantes.isEmpty()) {
            return CORRE;
        }
        return ConditionEvaluationResult.disabled(
                "No aplica a esta configuracion de logros: " + String.join(", ", faltantes));
    }
}
