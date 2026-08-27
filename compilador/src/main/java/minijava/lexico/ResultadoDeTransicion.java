package minijava.lexico;

// 'sealed' con exactamente dos permits es lo que le permite al driver usar un
// switch exhaustivo sin rama 'default'.
public sealed interface ResultadoDeTransicion permits Continuar, Emitir {
}
