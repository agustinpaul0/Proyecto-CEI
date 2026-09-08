package minijava.lexico.contexto;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.PosicionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.comun.io.ManejadorDeArchivo;

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

    // Recibir el centinela es condición necesaria pero no suficiente —un 0x1A
    // embebido también lo es—, así que el fin de archivo se confirma por posición.
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
