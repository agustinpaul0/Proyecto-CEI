package minijava.sentencias;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoNull;
import minijava.semantico.tipos.TipoPrimitivo;
import minijava.semantico.tipos.TipoReferencia;
import minijava.semantico.tipos.TipoVoid;
import minijava.semantico.tipos.VariableDeTipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tipo.conformaCon contra el documento §9, sobre una jerarquía con clases,
// interfaces, genéricos y herencia sin argumento. El corpus generado recorre la
// matriz completa por la salida del compilador; acá se miran las reglas una por una.
@Tag("sentencias")
class ConformidadTest extends BaseDeSentencias {

    @BeforeEach
    void cargarLaJerarquia() throws Exception {
        parsear("""
                interface Ia { }
                interface Ib extends Ia { }
                class Aa implements Ib { }
                class Bb extends Aa { }
                class Caja<T> { }
                interface Contenedor<T> { }
                class Cajon implements Contenedor<String> { }
                class Base<T> { }
                class Intermedia<U> extends Base<U> { }
                class Final extends Intermedia<String> { }
                class Cruda extends Base { }
                """);
        TablaDeSimbolos.chequearDeclaraciones();
        TablaDeSimbolos.consolidar();
    }

    private static TipoReferencia ref(String nombre) {
        return new TipoReferencia(new Token(TipoDeToken.ID_CLASE, nombre, 1));
    }

    private static TipoReferencia ref(String nombre, Tipo argumento) {
        return new TipoReferencia(new Token(TipoDeToken.ID_CLASE, nombre, 1), argumento);
    }

    private static VariableDeTipo t(String declarante) {
        return new VariableDeTipo(new Token(TipoDeToken.ID_GEN, "T", 1), declarante);
    }

    @Test
    void unPrimitivoConformaSoloConsigoMismo() {
        assertTrue(TipoPrimitivo.INT.conformaCon(TipoPrimitivo.INT));
        assertFalse(TipoPrimitivo.CHAR.conformaCon(TipoPrimitivo.INT));
        assertFalse(TipoPrimitivo.INT.conformaCon(TipoPrimitivo.CHAR));
        assertFalse(TipoPrimitivo.BOOLEAN.conformaCon(TipoPrimitivo.INT));
        assertFalse(TipoPrimitivo.INT.conformaCon(ref("Object")));
        assertFalse(TipoPrimitivo.INT.conformaCon(TipoNull.NULL));
    }

    @Test
    void nullNoSeVerificaNiSeInstancia() throws Exception {
        TipoNull.NULL.verificarEn(TablaDeSimbolos.object(), true);
        assertSame(TipoNull.NULL, TipoNull.NULL.instanciar(parametro -> Optional.of(TipoPrimitivo.INT)));
        assertEquals("null", TipoNull.NULL.nombre());
    }

    @Test
    void voidNoConformaConNada() {
        assertFalse(TipoVoid.VOID.conformaCon(TipoVoid.VOID));
        assertFalse(TipoVoid.VOID.conformaCon(ref("Object")));
        assertFalse(TipoNull.NULL.conformaCon(TipoVoid.VOID));
    }

    @Test
    void nullConformaConReferenciasArreglosYParametros() {
        assertTrue(TipoNull.NULL.conformaCon(ref("Aa")));
        assertTrue(TipoNull.NULL.conformaCon(ref("Ia")));
        assertTrue(TipoNull.NULL.conformaCon(TipoArreglo.de(TipoPrimitivo.INT, 1)));
        assertTrue(TipoNull.NULL.conformaCon(t("Caja")));
        assertTrue(TipoNull.NULL.conformaCon(TipoNull.NULL));
        assertFalse(TipoNull.NULL.conformaCon(TipoPrimitivo.BOOLEAN));
        assertFalse(ref("Aa").conformaCon(TipoNull.NULL));
    }

    @Test
    void unaClaseConformaConSusAncestrosSusInterfacesYObject() {
        assertTrue(ref("Bb").conformaCon(ref("Aa")));
        assertTrue(ref("Bb").conformaCon(ref("Ib")));
        assertTrue(ref("Bb").conformaCon(ref("Ia")));
        assertTrue(ref("Bb").conformaCon(ref("Object")));
        assertTrue(ref("Ib").conformaCon(ref("Ia")));
        assertTrue(ref("Ia").conformaCon(ref("Object")));
        assertTrue(ref("String").conformaCon(ref("Object")));
        assertTrue(ref("Object").conformaCon(ref("Object")));
    }

    @Test
    void elSubtipadoNoSeInvierte() {
        assertFalse(ref("Aa").conformaCon(ref("Bb")));
        assertFalse(ref("Ia").conformaCon(ref("Ib")));
        assertFalse(ref("Ia").conformaCon(ref("Aa")));
        assertFalse(ref("Object").conformaCon(ref("String")));
        assertFalse(ref("String").conformaCon(ref("Aa")));
    }

    @Test
    void losArgumentosGenericosSonInvariantes() {
        assertTrue(ref("Caja", ref("String")).conformaCon(ref("Caja", ref("String"))));
        assertFalse(ref("Caja", ref("String")).conformaCon(ref("Caja", ref("Object"))));
        assertFalse(ref("Caja", ref("Bb")).conformaCon(ref("Caja", ref("Aa"))));
        assertTrue(ref("Caja", ref("String")).conformaCon(ref("Object")));
    }

    @Test
    void laHerenciaSeRecorreConSusSustituciones() {
        assertTrue(ref("Cajon").conformaCon(ref("Contenedor", ref("String"))));
        assertFalse(ref("Cajon").conformaCon(ref("Contenedor", ref("Object"))));
        assertTrue(ref("Final").conformaCon(ref("Intermedia", ref("String"))));
        assertTrue(ref("Final").conformaCon(ref("Base", ref("String"))));
        assertFalse(ref("Final").conformaCon(ref("Base", ref("Object"))));
        assertTrue(ref("Intermedia", ref("Aa")).conformaCon(ref("Base", ref("Aa"))));
        assertFalse(ref("Intermedia", ref("Aa")).conformaCon(ref("Base", ref("Bb"))));
    }

    @Test
    void laHerenciaSinArgumentoNoDaUnaInstanciacion() {
        assertFalse(ref("Cruda").conformaCon(ref("Base", ref("Object"))));
        assertTrue(ref("Cruda").conformaCon(ref("Object")));
    }

    @Test
    void unParametroDeTipoConformaSoloConsigoMismo() {
        assertTrue(t("Caja").conformaCon(t("Caja")));
        assertFalse(t("Caja").conformaCon(ref("Object")));
        assertFalse(t("Caja").conformaCon(t("Base")));
        assertFalse(ref("Object").conformaCon(t("Caja")));
        assertTrue(ref("Caja", t("Caja")).conformaCon(ref("Caja", t("Caja"))));
        assertFalse(ref("Caja", t("Caja")).conformaCon(ref("Caja", t("Base"))));
    }

    @Test
    void losArreglosSonInvariantesYConformanConObject() {
        Tipo strings = TipoArreglo.de(ref("String"), 1);
        assertTrue(strings.conformaCon(TipoArreglo.de(ref("String"), 1)));
        assertFalse(strings.conformaCon(TipoArreglo.de(ref("Object"), 1)));
        assertFalse(TipoArreglo.de(ref("Bb"), 1).conformaCon(TipoArreglo.de(ref("Aa"), 1)));
        assertFalse(TipoArreglo.de(TipoPrimitivo.INT, 2).conformaCon(TipoArreglo.de(TipoPrimitivo.INT, 1)));
        assertFalse(TipoArreglo.de(t("Caja"), 1).conformaCon(TipoArreglo.de(ref("Object"), 1)));
        assertTrue(strings.conformaCon(ref("Object")));
        assertFalse(strings.conformaCon(ref("String")));
        assertFalse(ref("Object").conformaCon(strings));
        assertFalse(strings.conformaCon(ref("Object", ref("String"))));
    }
}
