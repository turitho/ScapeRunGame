package com.mycompany.scaperuntlp.controller;

import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.model.TipoObjeto;
import javafx.scene.input.KeyCode;

/** Movimentação, colisão circular e interações sem energia/inventário. */
public class JogadorController {
    public static final double RAIO_JOGADOR = 0.20;
    private static final double DISTANCIA_INTERACAO = 1.55;

    private final Jogador jogador;
    private final MapaJogo mapa;
    private final InputController input;
    private String mensagem = "";

    public JogadorController(Jogador jogador, MapaJogo mapa, InputController input) {
        this.jogador = jogador;
        this.mapa = mapa;
        this.input = input;
    }

    public String consumirMensagem() {
        String resultado = mensagem;
        mensagem = "";
        return resultado;
    }

    /**
     * Movimentação relativa ao olhar do jogador. O teclado NUNCA gira a
     * câmera: W/↑ avança, S/↓ recua, A/← vai à esquerda, D/→ à direita.
     * As diagonais têm a mesma velocidade do movimento em linha reta.
     */
    public void atualizar(double dt) {
        if (jogador.isEscondido()) {
            return;
        }

        int frente = (input.pressionada(KeyCode.W) || input.pressionada(KeyCode.UP) ? 1 : 0)
                - (input.pressionada(KeyCode.S) || input.pressionada(KeyCode.DOWN) ? 1 : 0);
        int lado = (input.pressionada(KeyCode.D) || input.pressionada(KeyCode.RIGHT) ? 1 : 0)
                - (input.pressionada(KeyCode.A) || input.pressionada(KeyCode.LEFT) ? 1 : 0);

        if (frente == 0 && lado == 0) {
            return;
        }

        double angulo = jogador.getAngulo();
        double cos = Math.cos(angulo);
        double sin = Math.sin(angulo);
        double normalizacao = Math.hypot(frente, lado);
        double distancia = jogador.getVelocidade() * dt / normalizacao;

        // Vetor "frente": (cos, sin); vetor "direita": (-sin, cos).
        double dx = (frente * cos - lado * sin) * distancia;
        double dy = (frente * sin + lado * cos) * distancia;
        moverComColisao(dx, dy);
    }

    /** Divide passos grandes para impedir atravessar paredes e móveis. */
    public void moverComColisao(double dx, double dy) {
        if (jogador.isEscondido()) return;
        int passos = Math.max(1, (int) Math.ceil(Math.hypot(dx, dy) / 0.07));
        for (int i = 0; i < passos; i++) {
            double passoX = dx / passos;
            double passoY = dy / passos;
            if (mapa.livreParaCirculo(jogador.getX() + passoX,
                    jogador.getY(), RAIO_JOGADOR)) {
                jogador.mover(passoX, 0);
            }
            if (mapa.livreParaCirculo(jogador.getX(),
                    jogador.getY() + passoY, RAIO_JOGADOR)) {
                jogador.mover(0, passoY);
            }
        }
    }

    /** E abre, entra ou sai do armário; em outros móveis, apenas examina. */
    public void interagir() {
        if (jogador.isEscondido()) {
            sairDoArmario();
            return;
        }
        ObjetoMapa alvo = getAlvoInteracao();
        if (alvo == null) return;

        switch (alvo.getTipo()) {
            case ARMARIO:
                if (!alvo.isAberto()) {
                    alvo.abrir();
                    mensagem = "Armário aberto. E entrar / F fechar";
                } else if (!alvo.isOcupado()) {
                    entrarNoArmario(alvo);
                }
                break;
            case MESA:
                mensagem = "Você examinou a mesa.";
                break;
            case ESTANTE:
                mensagem = "Você examinou a estante.";
                break;
            case CAMA:
                mensagem = "Você examinou a cama.";
                break;
            default:
                break;
        }
    }

    /** F fecha apenas armários que estejam abertos e vazios. */
    public void fecharArmario() {
        if (jogador.isEscondido()) {
            mensagem = "Saia do armário para fechar a porta.";
            return;
        }
        ObjetoMapa alvo = getAlvoInteracao();
        if (alvo != null && alvo.getTipo() == TipoObjeto.ARMARIO
                && alvo.isAberto() && !alvo.isOcupado()) {
            alvo.fechar();
            mensagem = "Armário fechado.";
        }
    }

    /** Localiza o móvel à frente sem atravessar paredes. */
    public ObjetoMapa getAlvoInteracao() {
        if (jogador.isEscondido()) return jogador.getArmarioAtual();

        double x = jogador.getX();
        double y = jogador.getY();
        double dx = Math.cos(jogador.getAngulo());
        double dy = Math.sin(jogador.getAngulo());
        ObjetoMapa alvo = null;
        double melhorDistancia = DISTANCIA_INTERACAO + 1;

        for (ObjetoMapa objeto : mapa.getObjetos()) {
            // Armários só são operados pelo lado da porta, não por trás.
            if (objeto.getTipo() == TipoObjeto.ARMARIO) {
                double ladoFrente = (x - objeto.getCentroX()) * objeto.getFrenteX()
                        + (y - objeto.getCentroY()) * objeto.getFrenteY();
                if (ladoFrente <= objeto.getProfundidade() * 0.32) continue;
            }

            double distancia = distanciaAoRetangulo(x, y, dx, dy,
                    objeto.getMinX(), objeto.getMinY(), objeto.getMaxX(), objeto.getMaxY());
            if (distancia < 0 || distancia > DISTANCIA_INTERACAO
                    || distancia >= melhorDistancia) continue;

            double pontoX = x + dx * Math.max(0, distancia - 0.02);
            double pontoY = y + dy * Math.max(0, distancia - 0.02);
            if (!mapa.linhaSemParede(x, y, pontoX, pontoY)) continue;
            melhorDistancia = distancia;
            alvo = objeto;
        }
        return alvo;
    }

    private static double distanciaAoRetangulo(double x, double y, double dx, double dy,
            double minX, double minY, double maxX, double maxY) {
        double entrada = Double.NEGATIVE_INFINITY;
        double saida = Double.POSITIVE_INFINITY;
        if (Math.abs(dx) < 1e-8) {
            if (x < minX || x > maxX) return -1;
        } else {
            double t1 = (minX - x) / dx;
            double t2 = (maxX - x) / dx;
            entrada = Math.max(entrada, Math.min(t1, t2));
            saida = Math.min(saida, Math.max(t1, t2));
        }
        if (Math.abs(dy) < 1e-8) {
            if (y < minY || y > maxY) return -1;
        } else {
            double t1 = (minY - y) / dy;
            double t2 = (maxY - y) / dy;
            entrada = Math.max(entrada, Math.min(t1, t2));
            saida = Math.min(saida, Math.max(t1, t2));
        }
        if (entrada > saida || saida < 0) return -1;
        return Math.max(0, entrada);
    }

    private void entrarNoArmario(ObjetoMapa armario) {
        // Verifica a posição de saída *antes* de entrar.
        if (!mapa.livreParaCirculo(jogador.getX(), jogador.getY(), RAIO_JOGADOR)) {
            mensagem = "Não há um local seguro para entrar.";
            return;
        }
        jogador.entrarNoArmario(armario, jogador.getX(), jogador.getY());
        mensagem = "Você está dentro do armário. E para sair.";
    }

    private void sairDoArmario() {
        // Dentro do móvel a câmera colide com sua estrutura. A saída precisa
        // ser validada na posição guardada, não na posição interna atual.
        if (!mapa.livreParaCirculo(jogador.getSaidaX(),
                jogador.getSaidaY(), RAIO_JOGADOR)) {
            mensagem = "Saída bloqueada. Não é possível sair agora.";
            return;
        }
        jogador.sairDoArmario();
        mensagem = "Você saiu do armário.";
    }
}
