package minijava.salida;

// Toda la salida del compilador pasa por acá — único punto que toca System.out
// (Pautas Etapa 1: "TODA la Salida debe realizarse con System.out").
public final class SalidaEstandar implements Salida {

    @Override
    public void imprimir(String linea) {
        System.out.println(linea);
    }
}
