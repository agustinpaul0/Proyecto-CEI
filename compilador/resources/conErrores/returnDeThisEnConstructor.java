///[Error:this|5]
// tampoco this: new produce el objeto, no el constructor (§12.5)
class Aa {
    Aa() {
        return this; // <-
    }
}
