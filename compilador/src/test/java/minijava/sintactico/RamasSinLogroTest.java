package minijava.sintactico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import minijava.logros.ConfiguracionDeLaCorrida;
import minijava.sintactico.errores.ExcepcionSintactica;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Las ramas del analizador que sólo existen con un logro APAGADO —las que la
// gramática marca con `@-`— corriendo con una configuración explícita en vez de
// con la de la corrida. Sin esto, una corrida con todos los logros prendidos no
// las ejecuta nunca, y son justamente las que reproducen la especificación base
// de la cátedra.
@Tag("sintactico")
class RamasSinLogroTest extends BaseDeAnalisis {

    private ConfiguracionDelCompilador sin(Logro... logros) {
        var configuracion = ConfiguracionDeLaCorrida.actual();
        for (Logro logro : logros) {
            configuracion = configuracion.sin(logro);
        }
        return configuracion;
    }

    private void aceptaSin(Path dir, String fuente, Logro... logros) {
        assertDoesNotThrow(() -> analizar(dir, fuente, sin(logros)), fuente);
    }

    private String rechazaSin(Path dir, String fuente, Logro... logros) {
        return assertThrows(ExcepcionSintactica.class,
                () -> analizar(dir, fuente, sin(logros)), fuente).getLexemaOfensivo();
    }

    // Miembro -> Constructor  [-visibilidad]
    // Constructor -> public idClase CuerpoDeMetodo
    @Test
    void sinVisibilidadElConstructorEsSuPropiaProduccion(@TempDir Path dir) {
        aceptaSin(dir, "class C1 { public C1(){} }", Logro.VISIBILIDAD);
        aceptaSin(dir, "class C1 { public C1(int a, char b){} }", Logro.VISIBILIDAD);
        aceptaSin(dir, "class C1 { public C1(){} int x; void m(){} }", Logro.VISIBILIDAD);
    }

    @Test
    void sinVisibilidadElPublicSoloValeEnElConstructor(@TempDir Path dir) {
        assertEquals("int", rechazaSin(dir, "class C1 { public int x; }", Logro.VISIBILIDAD));
        assertEquals("void", rechazaSin(dir, "class C1 { public void m(){} }", Logro.VISIBILIDAD));
        assertEquals("private", rechazaSin(dir, "class C1 { private int x; }", Logro.VISIBILIDAD));
    }

    @Test
    void sinVisibilidadUnMetodoDeInterfazNoLlevaPublic(@TempDir Path dir) {
        aceptaSin(dir, "interface I1 { int m1(); }", Logro.VISIBILIDAD);
        // El `public` no está en los Primeros de <MetodoInterfaz> sin el logro, así
        // que la lista termina ahí y el error lo da la llave de cierre que falta.
        assertEquals("public", rechazaSin(dir, "interface I1 { public int m1(); }",
                Logro.VISIBILIDAD));
    }

    // Sentencia -> SentenciaExpresion ;  [-varclasica]
    @Test
    void sinVariablesClasicasLaSentenciaExpresionEsSuPropiaProduccion(@TempDir Path dir) {
        aceptaSin(dir, envolverEnMetodo("x = 1;"), Logro.VARCLASICA);
        aceptaSin(dir, envolverEnMetodo("Clase.metodo();"), Logro.VARCLASICA);
        aceptaSin(dir, envolverEnMetodo("this.campo = a[0] + b.c();"), Logro.VARCLASICA);
        assertEquals("int", rechazaSin(dir, envolverEnMetodo("int x;"), Logro.VARCLASICA));
    }

    // ArgumentoDeTipo -> idClase  [-generico]
    @Test
    void sinGenericidadAvanzadaElArgumentoDeTipoEsUnSoloNivel(@TempDir Path dir) {
        aceptaSin(dir, "class C1 { Lista<Persona> x; }", Logro.GENERICO);
        assertEquals("<", rechazaSin(dir, "class C1 { Lista<Lista<Persona>> x; }", Logro.GENERICO));
        assertEquals(">", rechazaSin(dir, envolverEnMetodo("var x = new Lista<>();"),
                Logro.GENERICO));
    }

    // TipoPrimitivo -> float  y  Primitivo -> floatLiteral  [+float]
    @Test
    void sinFloatsElTipoYElLiteralDejanDeExistir(@TempDir Path dir) {
        aceptaSin(dir, "class C1 { int x; }", Logro.FLOAT);
        // Sin el logro `float` no es palabra clave: es un idMetVar, y como tal no
        // puede abrir un miembro.
        assertEquals("float", rechazaSin(dir, "class C1 { float x; }", Logro.FLOAT));
    }

    @Test
    void sinTernarioNiPosfijosNiLambdasLasCapasDesaparecen(@TempDir Path dir) {
        aceptaSin(dir, envolverEnMetodo("var x = a + b * c;"),
                Logro.TERNARIO, Logro.POSFIJO, Logro.LAMBDA);
        assertEquals("++", rechazaSin(dir, envolverEnMetodo("x++;"), Logro.POSFIJO));
    }
}
