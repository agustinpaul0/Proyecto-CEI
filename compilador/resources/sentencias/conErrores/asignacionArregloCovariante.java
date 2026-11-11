///[Error:=|5]
// los arreglos son invariantes: String[] no conforma con Object[] (§9.4)
class Aa {
    void m(String[] s, Object[] o) {
        o = s; // <-
    }
}
