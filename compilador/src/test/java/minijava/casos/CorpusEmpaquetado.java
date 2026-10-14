package minijava.casos;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

// Los corpus generados tienen decenas de miles de fuentes chicos. Se versionan
// empaquetados porque, sueltos, las cabeceras del zip de entrega pesan tanto
// como los fuentes. Acá se descomprimen en target/corpus/ y se recorren como
// cualquier carpeta. Los arma herramientas/empaquetar_corpus.py.
public final class CorpusEmpaquetado {

    public static final Path CORPUS_EXTERNO =
            Path.of("resources", "corpus-externo", "corpus-externo.tar.gz");
    public static final Path SINTACTICO_GENERADO =
            Path.of("resources", "sintactico", "sintactico-generado.tar.gz");
    public static final Path SENTENCIAS_GENERADO =
            Path.of("resources", "sentencias-generado.tar.gz");

    private static final Path DESTINO = Path.of("target", "corpus");
    private static final String EXTENSION = ".tar.gz";
    private static final String MARCA = ".extraido";

    private CorpusEmpaquetado() {
    }

    // La marca guarda el tamaño y la fecha del archivo: si el corpus se
    // regenera, la carpeta vieja se descarta. Se descomprime en una carpeta
    // aparte y se mueve entera, así nadie recorre una a medio llenar.
    public static synchronized Path extraido(Path archivo) throws IOException {
        String nombre = archivo.getFileName().toString();
        Path raiz = DESTINO.resolve(nombre.substring(0, nombre.length() - EXTENSION.length()));
        String version = Files.size(archivo) + " " + Files.getLastModifiedTime(archivo).toMillis();
        Path marca = raiz.resolve(MARCA);
        if (Files.exists(marca) && Files.readString(marca).equals(version)) {
            return raiz;
        }
        Path provisoria = Files.createDirectories(DESTINO).resolve(raiz.getFileName() + ".provisoria");
        borrar(provisoria);
        descomprimir(archivo, provisoria);
        Files.writeString(provisoria.resolve(MARCA), version);
        borrar(raiz);
        Files.move(provisoria, raiz, StandardCopyOption.ATOMIC_MOVE);
        return raiz;
    }

    private static void descomprimir(Path archivo, Path destino) throws IOException {
        try (InputStream crudo = new BufferedInputStream(Files.newInputStream(archivo));
             TarArchiveInputStream tar = new TarArchiveInputStream(new GZIPInputStream(crudo))) {
            for (TarArchiveEntry entrada = tar.getNextEntry(); entrada != null; entrada = tar.getNextEntry()) {
                Path fuente = destino.resolve(entrada.getName());
                if (entrada.isDirectory()) {
                    Files.createDirectories(fuente);
                } else {
                    Files.createDirectories(fuente.getParent());
                    Files.copy(tar, fuente);
                }
            }
        }
    }

    private static void borrar(Path carpeta) throws IOException {
        if (!Files.exists(carpeta)) {
            return;
        }
        try (Stream<Path> contenido = Files.walk(carpeta)) {
            for (Path ruta : contenido.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(ruta);
            }
        }
    }
}
