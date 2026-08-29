package minijava.lexico.contexto;

import minijava.config.ConfiguracionDelCompilador;
import minijava.errores.ExcepcionLexica;
import minijava.errores.PosicionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.io.ManejadorDeArchivo;

// Fachada: lo único que ven los estados. Construye sus dos piezas internas —son
// partes suyas, no dependencias externas.
public final class ContextoLexico {

    private final ManejadorDeArchivo manejador;
    private final ConfiguracionDelCompilador configuracion;
    private final BuilderDeLexema lexema;
    private final FabricaDeExcepcionesLexicas fabrica;

    public ContextoLexico(ManejadorDeArchivo manejador, ConfiguracionDelCompilador configuracion) {
        this.manejador = manejador;
        this.configuracion = configuracion;
        this.lexema = new BuilderDeLexema(manejador);
        this.fabrica = new FabricaDeExcepcionesLexicas(lexema);
    }

    public ConfiguracionDelCompilador getConfiguracion() {
        return configuracion;
    }

    public char getCaracterActual() {
        return lexema.getCaracterActual();
    }

    // Por posición y no contra el centinela: un 0x1A embebido es un carácter común.
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

    // Sólo EstadoBarra, para fotografiar la apertura de un comentario multilínea
    // antes de que iniciarNuevoToken() borre el lexema.
    public PosicionLexica posicionPrimerConsumido() {
        return lexema.posicionPrimerConsumido();
    }

    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo, AnclaDeError ancla) {
        return fabrica.crearExcepcionLexica(tipo, ancla);
    }

    public ExcepcionLexica crearExcepcionLexicaEn(TipoDeErrorLexico tipo, String lexemaOfensivo,
                                                  PosicionLexica posicion) {
        return fabrica.crearExcepcionLexicaEn(tipo, lexemaOfensivo, posicion);
    }
}
