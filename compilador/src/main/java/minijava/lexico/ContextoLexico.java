package minijava.lexico;

import minijava.config.ConfiguracionDelCompilador;
import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.io.ManejadorDeArchivo;

// Fachada: lo único que ven los estados del autómata. Construye sus dos partes
// internas (el lexema en curso y la fábrica de excepciones) en vez de
// recibirlas: son piezas de la fachada, no dependencias externas — esas dos,
// el manejador y la configuración, sí se inyectan.
public final class ContextoLexico {

    private final ManejadorDeArchivo manejador;
    private final ConfiguracionDelCompilador configuracion;
    private final BuilderDeLexema lexema;
    private final FabricaDeExcepcionesLexicas fabrica;

    public ContextoLexico(ManejadorDeArchivo manejador, ConfiguracionDelCompilador configuracion) {
        this.manejador = manejador;
        this.configuracion = configuracion;
        this.lexema = new BuilderDeLexema(manejador);
        this.fabrica = new FabricaDeExcepcionesLexicas(lexema, manejador);
    }

    // Los estados la consultan para los dos logros que cambian el AF en sí
    // (Floats I y Unicodes); los otros dos flags viven fuera del léxico.
    public ConfiguracionDelCompilador getConfiguracion() {
        return configuracion;
    }

    public char getCaracterActual() {
        return lexema.getCaracterActual();
    }

    // Por posición real, no comparando contra el centinela: un 0x1A embebido
    // en el fuente es un carácter común (y un error léxico), no el fin del
    // archivo. Ver ManejadorDeArchivo.estaEnFinDeArchivo().
    public boolean esFinDeArchivo() {
        return manejador.estaEnFinDeArchivo();
    }

    public int getLinea() {
        return lexema.getLinea();
    }

    public void avanzar() {
        lexema.avanzar();
    }

    public void descartar() {
        lexema.descartar();
    }

    public void iniciarNuevoToken() {
        lexema.iniciarNuevoToken();
    }

    public String getLexema() {
        return lexema.getLexema();
    }

    // La expone sólo EstadoBarra, que necesita fotografiar la apertura de un
    // comentario multilínea antes de que iniciarNuevoToken() borre el lexema
    // (el error se lanza mucho después, desde otro estado).
    public PosicionLexica posicionPrimerConsumido() {
        return lexema.posicionPrimerConsumido();
    }

    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo) {
        return fabrica.crearExcepcionLexica(tipo);
    }

    public ExcepcionLexica crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico tipo) {
        return fabrica.crearExcepcionLexicaSobreElUltimoConsumido(tipo);
    }

    public ExcepcionLexica crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico tipo,
                                                                     String fragmentoOfensivo) {
        return fabrica.crearExcepcionLexicaSobreElUltimoConsumido(tipo, fragmentoOfensivo);
    }

    public ExcepcionLexica crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico tipo) {
        return fabrica.crearExcepcionLexicaSobreElInicioDelLexema(tipo);
    }

    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo, String lexemaOfensivo,
                                                PosicionLexica posicion) {
        return fabrica.crearExcepcionLexica(tipo, lexemaOfensivo, posicion);
    }
}
