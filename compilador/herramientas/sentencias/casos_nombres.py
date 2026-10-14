"""Nombres, ámbitos y variables locales: documento §10.1, §12.3 y §12.6."""
from catalogo import bien, mal

bien("resolucionLocalParametroAtributo", "se busca local, despues parametro, despues atributo (§10.1)", """
class Aa {
    int x;
    boolean y;
    void m(char y) {
        var z = 'a';
        y = z;
        x = 3;
    }
}
""")

bien("localOcultaAtributo", "una local puede ocultar un atributo (§12.3)", """
class Aa {
    int x;
    void m() {
        var x = true;
        x = false;
        this.x = 3;
    }
}
""")

bien("parametroOcultaAtributo", "un parametro oculta un atributo; this.nombre lo selecciona (§10.1)", """
class Aa {
    int x;
    void m(boolean x) {
        x = true;
        this.x = 3;
    }
}
""")

bien("localVisibleEnBloquesAnidados", "una local es visible en los bloques anidados (§12.6)", """
class Aa {
    void m() {
        var x = 1;
        {
            {
                x = 2;
                var y = x;
            }
        }
    }
}
""")

bien("bloquesHermanosRepitenNombre", "bloques hermanos pueden declarar el mismo nombre (§12.3)", """
class Aa {
    void m() {
        {
            var x = 1;
        }
        {
            var x = true;
        }
        if (true) {
            var x = 'c';
        } else {
            var x = "s";
        }
        while (false) {
            var x = 0;
        }
    }
}
""")

bien("nombreReusadoDespuesDelBloque", "al cerrar un bloque sus locales dejan de ser visibles (§12.6)", """
class Aa {
    void m() {
        {
            var x = 1;
        }
        var x = true;
        x = false;
    }
}
""")

bien("varTomaElTipoExactoDelInicializador", "var recibe el tipo del inicializador, con argumentos y dimensiones (§12.3)", """
class Caja<T> { }
class Bb { }
class Aa {
    void m(Caja<String> c, Bb[][] mat) {
        var a = c;
        a = new Caja<String>();
        var b = mat;
        b = new Bb[1][2];
        var s = "x";
        s = "y";
        var i = 1 + 2;
        i = 4;
    }
}
""")

bien("varQueSeNombraAtributoHomonimo", "en el inicializador el nombre sigue siendo el atributo (§12.3)", """
class Aa {
    int x;
    void m() {
        var x = x + 1;
        x = 3;
    }
}
""")

bien("atributoDelAbuelo", "un atributo se busca tambien en los ancestros (§10.1)", """
class Aa { int x; }
class Bb extends Aa { }
class Cc extends Bb {
    void m() {
        x = 1;
    }
}
""")

bien("atributoYMetodoConElMismoNombre", "un atributo y un metodo se llaman igual sin confundirse", """
class Aa {
    int valor;
    int valor() { return valor; }
    void m() {
        valor = valor();
    }
}
""")

bien("localConElNombreDeUnMetodo", "una local puede llamarse como un metodo", """
class Aa {
    int m() { return 1; }
    void n() {
        var m = m();
        m = m + m();
    }
}
""")

bien("localUsadaDespuesDeUnReturn", "las sentencias despues de un return se chequean y ven las locales (§12.6)", """
class Aa {
    int m() {
        var x = 1;
        return x;
        x = 2;
    }
}
""")

bien("parametrosVisiblesEnTodoElCuerpo", "el bloque exterior dispone de los parametros desde el comienzo (§12.6)", """
class Aa {
    void m(int a, int b) {
        {
            {
                a = b;
            }
        }
    }
}
""")

mal("variableNoDeclarada", "zz", "el nombre no es local, ni parametro, ni atributo (§10.1)", """
class Aa {
    void m() {
        zz = 1; // <-
    }
}
""")

mal("variableNoDeclaradaComoValor", "zz", "un nombre sin declarar usado como valor", """
class Aa {
    int m() {
        return zz; // <-
    }
}
""")

mal("localDeOtroMetodo", "x", "una local de otro metodo no es visible", """
class Aa {
    void m() {
        var x = 1;
    }
    void n() {
        x = 2; // <-
    }
}
""")

mal("parametroDeOtroMetodo", "p", "un parametro de otro metodo no es visible", """
class Aa {
    void m(int p) { }
    void n() {
        p = 2; // <-
    }
}
""")

mal("localUsadaAntesDeDeclararse", "x", "una local es visible recien despues de su declaracion (§10.3)", """
class Aa {
    void m() {
        x = 1; // <-
        var x = 2;
    }
}
""")

mal("localFueraDeSuBloque", "x", "al cerrar el bloque la local deja de ser visible (§12.6)", """
class Aa {
    void m() {
        {
            var x = 1;
        }
        x = 2; // <-
    }
}
""")

mal("localDeLaRamaThenEnElElse", "x", "la local de un bloque del then no es visible en el else", """
class Aa {
    void m(boolean b) {
        if (b) {
            var x = 1;
        } else {
            x = 2; // <-
        }
    }
}
""")

mal("varQueSeNombraASiMisma", "x", "el inicializador no ve la variable que declara (§12.3)", """
class Aa {
    void m() {
        var x = x; // <-
    }
}
""")

mal("varRepiteUnParametro", "x", "una local no puede ocultar un parametro (§12.3)", """
class Aa {
    void m(int x) {
        var x = 1; // <-
    }
}
""")

mal("varEnBloqueAnidadoRepiteUnParametro", "x", "tampoco en un bloque anidado (§12.3)", """
class Aa {
    void m(int x) {
        {
            var x = 1; // <-
        }
    }
}
""")

mal("varRepetidaEnElMismoBloque", "x", "dos locales con el mismo nombre en un bloque (§12.3)", """
class Aa {
    void m() {
        var x = 1;
        var x = 2; // <-
    }
}
""")

mal("varRepiteUnaLocalDelBloqueExterior", "x", "una local visible de un contenedor no se oculta (§12.3)", """
class Aa {
    void m() {
        var x = 1;
        if (true) {
            var x = 2; // <-
        }
    }
}
""")

mal("varRepiteUnaLocalDosNivelesAfuera", "x", "la regla vale a cualquier profundidad", """
class Aa {
    void m() {
        var x = 1;
        {
            {
                {
                    var x = 2; // <-
                }
            }
        }
    }
}
""")

mal("varRepetidaConOtroTipo", "x", "la repeticion no depende del tipo", """
class Aa {
    void m() {
        var x = 1;
        var x = true; // <-
    }
}
""")

mal("varConNull", "x", "null no determina un tipo (§12.3)", """
class Aa {
    void m() {
        var x = null; // <-
    }
}
""")

mal("varConLlamadaVoid", "x", "una llamada void no permite inferir un tipo (§12.3)", """
class Aa {
    void v() { }
    void m() {
        var x = v(); // <-
    }
}
""")

mal("varConNullParentizado", "x", "los parentesis conservan el tipo de null", """
class Aa {
    void m() {
        var x = (null); // <-
    }
}
""")

mal("varConAsignacionDeNull", "x", "x = null tiene el tipo del destino, pero var y = (o = null) toma el de o", """
class Aa {
    void m(Aa o) {
        var y = (o = null);
        var x = (null); // <-
    }
}
""")

mal("varConInicializadorErroneo", "+", "el error del inicializador se reporta antes que el de la variable", """
class Aa {
    void m() {
        var x = true + 1; // <-
    }
}
""")

mal("atributoDeOtraClaseSinReceptor", "valor", "un atributo de otra clase no es visible sin receptor", """
class Bb { int valor; }
class Aa {
    void m() {
        valor = 1; // <-
    }
}
""")

mal("atributoDeUnaDescendiente", "valor", "un atributo de una hija no es visible en la madre", """
class Aa {
    void m() {
        valor = 1; // <-
    }
}
class Bb extends Aa { int valor; }
""")

mal("nombreDeMetodoComoVariable", "m", "un metodo no es una variable", """
class Aa {
    int m() { return 1; }
    void n() {
        var x = m; // <-
    }
}
""")

mal("errorEnLaPrimeraSentencia", "zz", "el error esta en la primera sentencia", """
class Aa {
    void m(int a) {
        zz = 1; // <-
        a = 2;
        a = 3;
    }
}
""")
