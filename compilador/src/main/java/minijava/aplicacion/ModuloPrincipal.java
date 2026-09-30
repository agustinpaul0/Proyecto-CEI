package minijava.aplicacion;

import java.io.IOException;

// El punto de entrada de una etapa: cada una se corrige por separado y tiene que
// poder correrse sola con `--etapa=N`.
public interface ModuloPrincipal {

    void ejecutar(String rutaFuente) throws IOException;
}
