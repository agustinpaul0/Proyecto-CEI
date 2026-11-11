///0&5&exitosamente
// sin-contraste: javac rechaza un metodo con retorno que puede terminar sin return; MiniJava no lo controla (documento §12.5)
class Init {
    static int quizas(boolean b) {
        if (b) {
            return 5;
        }
    }
    static void main() {
        debugPrint(quizas(false));
        debugPrint(quizas(true));
    }
}
