package minijava.casos;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// A qué configuración de logros aplica un caso del corpus, según el manifiesto
// de su fase, que documenta el formato y el motivo de estar afuera de los casos.
//
// Hay un manifiesto por fase y no uno solo porque la clave es
// <directorio>/<archivo>: `sinErrores/x.java` del léxico y del sintáctico
// chocarían en un mapa único.
//
// Es la misma semántica que @RequiereLogro sobre un test: un caso cuyos
// requisitos no se cumplen se SALTEA, no falla.
public record RequisitosDeLogrosDelCaso(Set<Logro> prendidos, Set<Logro> apagados) {

    public enum Fase {
        // El corpus de la etapa ACTIVA vive en la raíz de resources/, que es donde
        // los testers de la cátedra lo buscan sin subcarpeta. El de la etapa
        // anterior queda archivado en su propia carpeta.
        LEXICO(Path.of("resources", "lexico")),
        SINTACTICO(Path.of("resources"));

        private final Path raiz;

        Fase(Path raiz) {
            this.raiz = raiz;
        }

        public Path raiz() {
            return raiz;
        }

        Path manifiesto() {
            return raiz.resolve("requisitos-de-logros.txt");
        }
    }

    private static final RequisitosDeLogrosDelCaso SIN_REQUISITOS =
            new RequisitosDeLogrosDelCaso(Set.of(), Set.of());

    private static final Map<Fase, Map<String, RequisitosDeLogrosDelCaso>> POR_FASE =
            new EnumMap<>(Fase.class);

    public static RequisitosDeLogrosDelCaso de(Fase fase, Path caso) {
        return manifiestoDe(fase).getOrDefault(claveDe(caso), SIN_REQUISITOS);
    }

    public static Map<String, RequisitosDeLogrosDelCaso> manifiestoDe(Fase fase) {
        return POR_FASE.computeIfAbsent(fase, RequisitosDeLogrosDelCaso::leerManifiesto);
    }

    public boolean seCumplenEn(ConfiguracionDelCompilador configuracion) {
        return incumplidos(configuracion).isEmpty();
    }

    public String describirIncumplidos(ConfiguracionDelCompilador configuracion) {
        return String.join(", ", incumplidos(configuracion));
    }

    private List<String> incumplidos(ConfiguracionDelCompilador configuracion) {
        List<String> faltantes = new ArrayList<>();
        for (Logro logro : prendidos) {
            if (!configuracion.tiene(logro)) {
                faltantes.add(logro.getNombre() + " tiene que estar prendido");
            }
        }
        for (Logro logro : apagados) {
            if (configuracion.tiene(logro)) {
                faltantes.add(logro.getNombre() + " tiene que estar apagado");
            }
        }
        return faltantes;
    }

    // Los testers nombran el caso de formas distintas: unos con la ruta completa,
    // otros con el nombre suelto. La clave es siempre <directorio>/<archivo>.
    private static String claveDe(Path caso) {
        Path normalizado = caso.normalize();
        Path directorio = normalizado.getParent();
        return directorio == null
                ? normalizado.getFileName().toString()
                : directorio.getFileName() + "/" + normalizado.getFileName();
    }

    private static Map<String, RequisitosDeLogrosDelCaso> leerManifiesto(Fase fase) {
        Path manifiesto = fase.manifiesto();
        Map<String, RequisitosDeLogrosDelCaso> requisitos = new HashMap<>();
        List<String> lineas;
        try {
            lineas = Files.readAllLines(manifiesto, StandardCharsets.UTF_8);
        } catch (IOException excepcion) {
            throw new UncheckedIOException("No se pudo leer " + manifiesto, excepcion);
        }

        for (String linea : lineas) {
            String limpia = linea.strip();
            if (limpia.isEmpty() || limpia.startsWith("#")) {
                continue;
            }
            String[] campos = limpia.split("\\|", -1);
            if (campos.length != 3) {
                throw new IllegalStateException(
                        manifiesto + ": se esperaban tres campos separados por | en: " + limpia);
            }
            String caso = campos[0].strip();
            var anterior = requisitos.put(caso,
                    new RequisitosDeLogrosDelCaso(logros(manifiesto, campos[1]),
                            logros(manifiesto, campos[2])));
            if (anterior != null) {
                throw new IllegalStateException(manifiesto + ": caso declarado dos veces: " + caso);
            }
        }
        return Map.copyOf(requisitos);
    }

    private static Set<Logro> logros(Path manifiesto, String campo) {
        EnumSet<Logro> logros = EnumSet.noneOf(Logro.class);
        for (String nombre : campo.split(",")) {
            if (nombre.isBlank()) {
                continue;
            }
            logros.add(Logro.porNombre(nombre).orElseThrow(() -> new IllegalStateException(
                    manifiesto + ": logro inexistente: " + nombre.strip())));
        }
        return logros;
    }
}
