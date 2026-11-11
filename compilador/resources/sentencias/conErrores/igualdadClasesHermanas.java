///[Error:!=|8]
// dos hijas de una misma clase no conforman entre si
class Aa { }
class Bb extends Aa { }
class Cc extends Aa { }
class Uso {
    void m(Bb b, Cc c, boolean r) {
        r = b != c; // <-
    }
}
