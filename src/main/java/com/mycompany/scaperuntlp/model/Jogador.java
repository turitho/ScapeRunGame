package com.mycompany.scaperuntlp.model;

/** Dados do personagem: posição, direção e esconderijo ativo. */
public class Jogador {

    private double x = 3.5;
    private double y = 3.5;
    private double angulo;
    private double inclinacaoCamera; // pitch: positiva = olhar para cima
    private static final double INCLINACAO_MAXIMA = Math.toRadians(50);

    private final double velocidade = 3.0;

    private ObjetoMapa armarioAtual;
    private double saidaX;
    private double saidaY;
    private double anguloAntesDeEsconder;
    private double inclinacaoAntesDeEsconder;

    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngulo() { return angulo; }
    public double getInclinacaoCamera() { return inclinacaoCamera; }
    public double getVelocidade() { return velocidade; }
    public boolean isEscondido() { return armarioAtual != null; }
    public ObjetoMapa getArmarioAtual() { return armarioAtual; }

    public void mover(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void posicionar(double novoX, double novoY, double novoAngulo) {
        x = novoX;
        y = novoY;
        angulo = novoAngulo;
        inclinacaoCamera = 0;
    }

    /** Altera a direção horizontal da câmera (yaw). */
    public void girar(double diferenca) {
        angulo += diferenca;
    }

    /** Altera a inclinação vertical da câmera, sem virar de cabeça para baixo. */
    public void inclinar(double diferenca) {
        inclinacaoCamera = Math.max(-INCLINACAO_MAXIMA,
                Math.min(INCLINACAO_MAXIMA, inclinacaoCamera + diferenca));
    }

    /**
     * Guarda o local seguro de saída e COLOCA a câmera dentro do armário.
     * O deslocamento é real no Model, não um efeito aplicado sobre a tela.
     */
    public void entrarNoArmario(ObjetoMapa armario, double xSeguro, double ySeguro) {
        if (isEscondido()) {
            return;
        }
        armario.ocupar();

        saidaX = xSeguro;
        saidaY = ySeguro;
        anguloAntesDeEsconder = angulo;
        inclinacaoAntesDeEsconder = inclinacaoCamera;
        armarioAtual = armario;

        // A câmera passa para o centro interno, olhando para a porta.
        x = armario.getCentroX();
        y = armario.getCentroY();
        angulo = Math.atan2(armario.getFrenteY(), armario.getFrenteX());
        inclinacaoCamera = 0;
    }

    /** Retorna exatamente à posição e à orientação salvas na entrada. */
    public void sairDoArmario() {
        if (!isEscondido()) {
            return;
        }
        armarioAtual.desocupar();
        armarioAtual = null;
        x = saidaX;
        y = saidaY;
        angulo = anguloAntesDeEsconder;
        inclinacaoCamera = inclinacaoAntesDeEsconder;
    }

    public double getSaidaX() { return saidaX; }
    public double getSaidaY() { return saidaY; }
}
