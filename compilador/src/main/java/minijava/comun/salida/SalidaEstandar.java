package minijava.comun.salida;

// Único punto que toca System.out (Pautas: "TODA la Salida debe realizarse con System.out").
public final class SalidaEstandar implements Salida {

    @Override
    public void imprimir(String linea) {
        System.out.println(linea);
    }
}
