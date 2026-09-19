// [Error:[|6]
// Dimensiones tras el nombre (int a[]): valido en Java, pero esta gramatica solo las admite delante (int[] a).
//
class Foo {
	void main() {
		int a[];
	}
}