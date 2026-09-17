///[SinErrores]
// Mezcla de declaraciones locales con asignaciones y control de flujo

class Mezcla {

    int probar(int n) {
        int i = 0;
        int limite, total = 0;
        var paso = 1;
        boolean seguir = true;
        while (seguir) {
            i = i + paso;
            if (i >= limite) {
                seguir = false;
            }
        }
        return total;
    }
}