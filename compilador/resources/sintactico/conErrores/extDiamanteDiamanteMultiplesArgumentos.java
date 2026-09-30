///[Error:,|8]
// Esta gramatica admite a lo sumo UN argumento de tipo en la instanciacion
// (new Caja<T>() o new Caja<>()); varios argumentos (Caja<Uno, Dos>) quedan
// fuera, aunque Java si los permite.
//
class Prueba {
    void main() {
        var a = new Caja<Uno, Dos>();
    }
}

class Caja<T> {
}

class Uno {
}

class Dos {
}