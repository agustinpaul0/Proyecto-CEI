package minijava.lexico.estados;

// Utilidad compartida por los estados de floatLiteral.
final class EstadosFloat {

    private EstadosFloat() {
    }

    static boolean esSufijoFloat(char c) {
        return c == 'f' || c == 'F' || c == 'd' || c == 'D';
    }
}
