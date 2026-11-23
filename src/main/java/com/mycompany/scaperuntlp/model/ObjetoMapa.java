package com.mycompany.scaperuntlp.model;

/** Móvel com dimensões, orientação e estados próprios. */
public class ObjetoMapa {

    public enum EstadoArmario { FECHADO, ABERTO, OCUPADO }

    private final TipoObjeto tipo;
    private final int x;
    private final int y;
    private double alturaRelativa;
    private double largura;
    private double profundidade;
    private int rotacao; // 0, 90, 180 ou 270 graus.
    private EstadoArmario estadoArmario = EstadoArmario.FECHADO;

    public ObjetoMapa(TipoObjeto tipo, int x, int y) {
        this(tipo, x, y, tipo.getAlturaRelativa());
    }

    public ObjetoMapa(TipoObjeto tipo, int x, int y, double altura) {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo não pode ser nulo.");
        }
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.largura = tipo.getLargura();
        this.profundidade = tipo.getProfundidade();
        setAlturaRelativa(altura);
    }

    public TipoObjeto getTipo() { return tipo; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getCentroX() { return x + 0.5; }
    public double getCentroY() { return y + 0.5; }
    /** Direção para fora da frente do armário, considerando sua rotação. */
    public double getFrenteX() {
        switch (rotacao) {
            case 90: return -1;
            case 270: return 1;
            default: return 0;
        }
    }

    public double getFrenteY() {
        switch (rotacao) {
            case 0: return 1;
            case 180: return -1;
            default: return 0;
        }
    }
    public double getAlturaRelativa() { return alturaRelativa; }
    public double getLargura() { return largura; }
    public double getProfundidade() { return profundidade; }
    public int getRotacao() { return rotacao; }

    public void setAlturaRelativa(double altura) {
        validarDimensao(altura, 0.08, 2.0);
        this.alturaRelativa = altura;
    }

    public void setDimensoes(double largura, double profundidade) {
        validarDimensao(largura, 0.15, 2.5);
        validarDimensao(profundidade, 0.15, 2.5);
        this.largura = largura;
        this.profundidade = profundidade;
    }

    public void setRotacao(int graus) {
        if (graus % 90 != 0) {
            throw new IllegalArgumentException("Rotação deve ser múltipla de 90.");
        }
        this.rotacao = ((graus % 360) + 360) % 360;
    }

    private static void validarDimensao(double valor, double minimo, double maximo) {
        if (!Double.isFinite(valor) || valor < minimo || valor > maximo) {
            throw new IllegalArgumentException("Dimensão fora do intervalo permitido.");
        }
    }

    public double getLarguraNoMundo() {
        return rotacao % 180 == 0 ? largura : profundidade;
    }

    public double getProfundidadeNoMundo() {
        return rotacao % 180 == 0 ? profundidade : largura;
    }

    public double getMinX() { return getCentroX() - getLarguraNoMundo() / 2; }
    public double getMaxX() { return getCentroX() + getLarguraNoMundo() / 2; }
    public double getMinY() { return getCentroY() - getProfundidadeNoMundo() / 2; }
    public double getMaxY() { return getCentroY() + getProfundidadeNoMundo() / 2; }

    public boolean contem(double px, double py) {
        return px >= getMinX() && px <= getMaxX()
                && py >= getMinY() && py <= getMaxY();
    }

    /** Colisão entre um círculo (jogador) e o retângulo do móvel. */
    public boolean colideComCirculo(double px, double py, double raio) {
        if (raio == 0) return contem(px, py);
        double proximoX = Math.max(getMinX(), Math.min(px, getMaxX()));
        double proximoY = Math.max(getMinY(), Math.min(py, getMaxY()));
        double dx = px - proximoX;
        double dy = py - proximoY;
        return dx * dx + dy * dy < raio * raio;
    }

    public EstadoArmario getEstadoArmario() { return estadoArmario; }
    public boolean isAberto() { return tipo == TipoObjeto.ARMARIO && estadoArmario != EstadoArmario.FECHADO; }
    public boolean isOcupado() { return tipo == TipoObjeto.ARMARIO && estadoArmario == EstadoArmario.OCUPADO; }

    public void abrir() {
        if (tipo == TipoObjeto.ARMARIO && estadoArmario == EstadoArmario.FECHADO) {
            estadoArmario = EstadoArmario.ABERTO;
        }
    }

    public void fechar() {
        if (tipo == TipoObjeto.ARMARIO && estadoArmario == EstadoArmario.ABERTO) {
            estadoArmario = EstadoArmario.FECHADO;
        }
    }

    public void alternarAberto() {
        if (estadoArmario == EstadoArmario.FECHADO) abrir(); else fechar();
    }

    public void ocupar() {
        if (tipo != TipoObjeto.ARMARIO || estadoArmario != EstadoArmario.ABERTO) {
            throw new IllegalStateException("O armário precisa estar aberto e vazio.");
        }
        estadoArmario = EstadoArmario.OCUPADO;
    }

    public void desocupar() {
        if (estadoArmario == EstadoArmario.OCUPADO) {
            estadoArmario = EstadoArmario.ABERTO;
        }
    }

    /** Mesmo aberto, o armário físico continua bloqueando o movimento. */
    public boolean isSolido() { return true; }
}
