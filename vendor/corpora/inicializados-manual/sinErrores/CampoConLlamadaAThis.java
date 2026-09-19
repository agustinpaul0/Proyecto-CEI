// [SinErrores]
// Un inicializador de campo puede ser una llamada encadenada con this
// (private int k = this.m();): la expresion de inicializacion admite cualquier
// expresion sin asignacion, incluidas las llamadas a metodos.
//
class Prueba {
	private int k = this.m();

	int m() {
		return 0;
	}
}