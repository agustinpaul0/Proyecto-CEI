///[Error:=|5]
// una fila String[] no conforma con Object[] (§9.4)
class Aa {
    void m(String[][] m, Object[] o) {
        o = m[0]; // <-
    }
}
