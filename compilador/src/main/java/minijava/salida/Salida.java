package minijava.salida;

// Toda la salida pasa por acá en vez de llamar a System.out directo. Hacia
// afuera no cambia nada —SalidaEstandar, la única implementación que usa Main,
// delega en System.out, como exige la cátedra— pero permite testear el Módulo
// Principal completo contra una implementación en memoria, comparando la lista
// de líneas impresas con assertEquals en vez de capturar y restaurar el stream
// global de la JVM. Es la decisión de mayor impacto en la testeabilidad del
// proyecto.
public interface Salida {

    void imprimir(String linea);
}
