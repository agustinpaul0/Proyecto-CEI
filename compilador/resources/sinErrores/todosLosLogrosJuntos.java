class Registro<T> extends Base<T> {
  private Lista<Lista<T>> datos = new Lista<>();
  public int cuenta = 0;
  private float promedio = 0.5f;

  public Registro(Lista<T> iniciales){
    int i, j, total = 0;
    for(int k = 0; k < 10; k++) {
      total = k > 5 ? k : total;
    }
    for(T elemento : iniciales) {
      this.cuenta++;
    }
    var matriz = new int[][]{{1,2},{3,4}};
  }

  private Lista<T> filtrar(Lista<T> origen, int limite){
    return origen;
  }
}
