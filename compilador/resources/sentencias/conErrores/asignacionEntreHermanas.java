///[Error:=|8]
// dos hijas de la misma clase no conforman
class Aa { }
class Bb extends Aa { }
class Cc extends Aa { }
class Uso {
    void m(Bb b, Cc c) {
        b = c; // <-
    }
}
