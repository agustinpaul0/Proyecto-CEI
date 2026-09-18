package minijava.comun;

import net.jqwik.api.Property;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Los perfiles fase-lexico y fase-sintactico filtran por @Tag, y jqwik NO entiende
// el @Tag de JUnit: una clase de propiedades que use el de JUnit queda sin etiqueta
// para jqwik, y el perfil la SALTEA en silencio —sin fallar y sin contarla—. Lo
// único que lo delata es un aviso por consola, fácil de pasar por alto.
//
// Se verifica por reflexión y no leyendo las fuentes porque es la anotación que
// llega al bytecode la que decide, no cómo se haya escrito el import.
@Tag("comun")
class PropiedadesEtiquetadasTest {

    private static final Path CLASES = Path.of("target", "test-classes");

    @Test
    void todaClaseDePropiedadesLlevaElTagDeJqwikYNoElDeJunit() throws IOException {
        List<String> mal = clasesDePropiedades()
                .filter(clase -> clase.getAnnotation(net.jqwik.api.Tag.class) == null
                        || clase.getAnnotation(Tag.class) != null)
                .map(Class::getName)
                .toList();

        assertEquals(List.of(), mal,
                "usan el @Tag de JUnit, que jqwik ignora: el perfil de fase las saltea");
    }

    @Test
    void hayClasesDePropiedadesQueVerificar() throws IOException {
        assertFalse(clasesDePropiedades().toList().isEmpty());
    }

    private Stream<Class<?>> clasesDePropiedades() throws IOException {
        try (Stream<Path> archivos = Files.walk(CLASES)) {
            return archivos.filter(ruta -> ruta.toString().endsWith(".class"))
                    .map(PropiedadesEtiquetadasTest::cargar)
                    .filter(clase -> clase != null && declaraPropiedades(clase))
                    .toList()
                    .stream();
        }
    }

    // Sin inicializar: cargar una clase de test no tiene por qué correr su
    // inicializador estático sólo para mirarle las anotaciones.
    private static Class<?> cargar(Path archivo) {
        String nombre = CLASES.relativize(archivo).toString()
                .replace(java.io.File.separatorChar, '.')
                .replaceAll("\\.class$", "");
        try {
            return Class.forName(nombre, false, PropiedadesEtiquetadasTest.class.getClassLoader());
        } catch (Throwable noSePudoCargar) {
            return null;
        }
    }

    private static boolean declaraPropiedades(Class<?> clase) {
        return Stream.of(clase.getDeclaredMethods())
                .anyMatch(metodo -> metodo.isAnnotationPresent(Property.class));
    }
}
