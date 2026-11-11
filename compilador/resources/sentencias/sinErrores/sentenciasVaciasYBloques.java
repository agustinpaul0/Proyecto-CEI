// ; y los bloques vacios son correctos (§12.1, §12.6)
class Aa {
    void m() {
        ;
        ;;
        { }
        {{ }}
        { ; { ; } }
    }
}
