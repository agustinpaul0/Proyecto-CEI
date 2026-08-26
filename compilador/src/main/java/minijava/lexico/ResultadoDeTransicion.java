package minijava.lexico;

// Lo que devuelve un estado del AF: o el token quedó completo (Emitir), o hay
// que seguir desde otro estado (Continuar). No hay una tercera posibilidad, y
// 'sealed' es lo que se lo garantiza al compilador: por eso el switch del
// driver es exhaustivo sin rama 'default', y si algún día apareciera un tercer
// resultado el driver directamente no compilaría hasta contemplarlo.
public sealed interface ResultadoDeTransicion permits Continuar, Emitir {
}
