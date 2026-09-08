package minijava.lexico;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.tokens.Token;
import minijava.lexico.tokens.TipoDeToken;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

// Fuzzing con semilla fija: el único contrato es que el compilador termine y que
// todo fallo sea ExcepcionLexica, nunca un crash sin controlar.
@Tag("lexico")
class AnalizadorLexicoFuzzTest {

    private static final long SEMILLA = 20260824L;
    private static final int CANTIDAD_DE_CORRIDAS = 500;
    private static final int LARGO_MAXIMO = 200;
    private static final int LIMITE_DE_SEGURIDAD = 50_000;

    @Test
    void bytesUtf8AleatoriosNuncaProducenUnFalloNoControlado() throws IOException {
        Random random = new Random(SEMILLA);
        Path archivo = Files.createTempFile("minijava-fuzz-", ".mjava");
        archivo.toFile().deleteOnExit();

        for (int corrida = 0; corrida < CANTIDAD_DE_CORRIDAS; corrida++) {
            String fuente = generarFuenteAleatoria(random);
            Files.write(archivo, fuente.getBytes(StandardCharsets.UTF_8));

            try {
                tokenizarConRecuperacion(archivo);
            } catch (AssertionError | RuntimeException fallo) {
                Path caso = Path.of("resources", "lexico", "regresion",
                        "fuzz-semilla-" + SEMILLA + "-corrida-" + corrida + ".mjava");
                Files.createDirectories(caso.getParent());
                Files.write(caso, fuente.getBytes(StandardCharsets.UTF_8));
                fail("Falló con semilla " + SEMILLA + ", corrida " + corrida
                        + " — caso guardado en " + caso, fallo);
            }
        }
    }

    private void tokenizarConRecuperacion(Path archivo) throws IOException {
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        AnalizadorLexico analizador = new AnalizadorLexico(manejador);

        for (int i = 0; i < LIMITE_DE_SEGURIDAD; i++) {
            try {
                Token token = analizador.proximoToken();
                if (token.tipo() == TipoDeToken.EOF) {
                    return;
                }
            } catch (ExcepcionLexica excepcionControlada) {
                // Sin paso de recuperación: el bucle avanza solo.
            }
        }
        throw new AssertionError("no llegó a EOF tras " + LIMITE_DE_SEGURIDAD + " pasos");
    }

    // Sobre BYTES CRUDOS: getBytes(UTF_8) siempre produce bytes bien formados, así
    // que el camino de decodificación quedaba sin fuzzear — y ahí se escondía el
    // crash con archivos binarios (ver DefectosDeRobustezTest).
    @Test
    void bytesCrudosArbitrariosNuncaProducenUnFalloNoControlado() throws IOException {
        Random random = new Random(SEMILLA);
        Path archivo = Files.createTempFile("minijava-fuzz-bytes-", ".mjava");
        archivo.toFile().deleteOnExit();

        for (int corrida = 0; corrida < CANTIDAD_DE_CORRIDAS; corrida++) {
            byte[] bytes = new byte[random.nextInt(LARGO_MAXIMO)];
            random.nextBytes(bytes);
            Files.write(archivo, bytes);

            try {
                tokenizarConRecuperacion(archivo);
            } catch (AssertionError | RuntimeException fallo) {
                Path caso = Path.of("resources", "lexico", "regresion",
                        "fuzz-bytes-semilla-" + SEMILLA + "-corrida-" + corrida + ".mjava");
                Files.createDirectories(caso.getParent());
                Files.write(caso, bytes);
                fail("Fallo con semilla " + SEMILLA + ", corrida " + corrida
                        + " — caso guardado en " + caso, fallo);
            }
        }
    }

    // Sesgada a construcciones sin cerrar, donde vive la mayor parte de la lógica
    // de error: el alfabeto general las produce demasiado poco seguido.
    @Test
    void construccionesSinCerrarNuncaProducenUnFalloNoControlado() throws IOException {
        Random random = new Random(SEMILLA + 1);
        Path archivo = Files.createTempFile("minijava-fuzz-abiertos-", ".mjava");
        archivo.toFile().deleteOnExit();
        String[] piezas = {"'", "\"", "\\", "/*", "*/", "//", "\n", "u", "0", "a", " ", "'\\", "\"\\"};

        for (int corrida = 0; corrida < CANTIDAD_DE_CORRIDAS; corrida++) {
            StringBuilder fuente = new StringBuilder();
            int cantidad = random.nextInt(30);
            for (int i = 0; i < cantidad; i++) {
                fuente.append(piezas[random.nextInt(piezas.length)]);
            }
            Files.write(archivo, fuente.toString().getBytes(StandardCharsets.UTF_8));

            try {
                tokenizarConRecuperacion(archivo);
            } catch (AssertionError | RuntimeException fallo) {
                Path caso = Path.of("resources", "lexico", "regresion",
                        "fuzz-abiertos-semilla-" + SEMILLA + "-corrida-" + corrida + ".mjava");
                Files.createDirectories(caso.getParent());
                Files.write(caso, fuente.toString().getBytes(StandardCharsets.UTF_8));
                fail("Fallo con semilla " + SEMILLA + ", corrida " + corrida
                        + " — caso guardado en " + caso, fallo);
            }
        }
    }

    // Sesga hacia caracteres interesantes (comillas, barras, escapes) para no
    // desperdiciar corridas en texto trivialmente válido o inválido.
    private String generarFuenteAleatoria(Random random) {
        String alfabeto = "abcXYZ019 \t\n{}()\"'\\/*.+-<>=!&|#%;,:_ ";
        int largo = random.nextInt(LARGO_MAXIMO);
        StringBuilder sb = new StringBuilder(largo);
        for (int i = 0; i < largo; i++) {
            if (random.nextInt(20) == 0) {
                sb.appendCodePoint(0x20AC + random.nextInt(200)); // ruido Unicode
            } else {
                sb.append(alfabeto.charAt(random.nextInt(alfabeto.length())));
            }
        }
        return sb.toString();
    }
}
