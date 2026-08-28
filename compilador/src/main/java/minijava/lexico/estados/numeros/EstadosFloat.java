package minijava.lexico.estados.numeros;

final class EstadosFloat {

    private EstadosFloat() {
    }

    static boolean esSufijoFloat(char c) {
        return c == 'f' || c == 'F' || c == 'd' || c == 'D';
    }
}
