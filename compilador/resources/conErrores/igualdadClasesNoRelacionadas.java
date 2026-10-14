///[Error:==|7]
// dos clases sin relacion no se comparan (§11.2)
class Aa { }
class Bb { }
class Uso {
    void m(Aa a, Bb b, boolean r) {
        r = a == b; // <-
    }
}
