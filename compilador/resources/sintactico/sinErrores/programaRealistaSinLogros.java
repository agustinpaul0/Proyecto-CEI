class Pila {
  Nodo tope;
  int tamanio;

  public Pila(){
    this.tope = null;
    this.tamanio = 0;
  }

  void apilar(Objeto valor){
    var nodo = new Nodo(valor, this.tope);
    this.tope = nodo;
    this.tamanio = this.tamanio + 1;
  }

  Objeto desapilar(){
    if (this.tamanio == 0) return null;
    var valor = this.tope.valor();
    this.tope = this.tope.siguiente();
    this.tamanio = this.tamanio - 1;
    return valor;
  }

  boolean estaVacia(){ return this.tamanio == 0; }
}

interface Comparable {
  int comparar(Objeto otro);
}
