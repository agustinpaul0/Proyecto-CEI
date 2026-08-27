package minijava.salida;

// Interfaz y no System.out directo: permite testear el Módulo Principal contra
// una implementación en memoria, sin capturar el stream global de la JVM.
public interface Salida {

    void imprimir(String linea);
}
