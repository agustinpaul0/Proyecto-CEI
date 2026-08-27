package minijava.salida;

import java.util.ArrayList;
import java.util.List;

// Doble de test: asserts directos sobre la salida, sin tocar el stream de la JVM.
public final class SalidaEnMemoria implements Salida {

    private final List<String> lineas = new ArrayList<>();

    @Override
    public void imprimir(String linea) {
        lineas.add(linea);
    }

    public List<String> getLineas() {
        return lineas;
    }
}
