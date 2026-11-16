package com.mycompany.scaperuntlp.view;

import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.model.TipoObjeto;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** View responsável pela apresentação 3D simplificada do mapa. */
public class JogoView extends StackPane {
    private static final double LARGURA_TELA = 960;
    private static final double ALTURA_TELA = 640;
    private static final double CAMPO_VISAO = Math.toRadians(60);
    private static final double DISTANCIA_MAXIMA = 35;
    private static final double PASSO_RAYCAST = 0.025;

    private final Canvas canvas = new Canvas(LARGURA_TELA, ALTURA_TELA);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    public JogoView() {
        getChildren().add(canvas);
        setStyle("-fx-background-color: black;");
    }

    public void desenhar(Jogador jogador, MapaJogo mapa) {
        double largura = canvas.getWidth();
        double altura = canvas.getHeight();
        double horizonte = altura / 2;

        if (jogador.isEscondido()) {
            desenharTelaEscondido(largura, altura);
            return;
        }

        desenharCeuEChao(largura, altura, horizonte);

        for (int coluna = 0; coluna < (int) largura; coluna++) {
            desenharColuna(coluna, largura, altura, horizonte, jogador, mapa);
        }

        desenharHud(largura, horizonte);
    }

    private void desenharTelaEscondido(double largura, double altura) {
        gc.setFill(Color.rgb(12, 10, 9));
        gc.fillRect(0, 0, largura, altura);
        gc.setFill(Color.BEIGE);
        gc.setFont(Font.font(22));
        gc.fillText("ESCONDIDO NO ARMÁRIO — pressione E para sair",
                largura / 2 - 230, altura / 2);
    }

    private void desenharCeuEChao(double largura, double altura, double horizonte) {
        gc.setFill(Color.rgb(28, 29, 36));
        gc.fillRect(0, 0, largura, horizonte);
        gc.setFill(Color.rgb(75, 67, 58));
        gc.fillRect(0, horizonte, largura, altura - horizonte);
    }

    private void desenharColuna(int coluna, double largura, double altura,
            double horizonte, Jogador jogador, MapaJogo mapa) {
        double camera = 2.0 * coluna / largura - 1;
        double anguloRaio = jogador.getAngulo()
                + Math.atan(camera * Math.tan(CAMPO_VISAO / 2));
        double raioX = Math.cos(anguloRaio);
        double raioY = Math.sin(anguloRaio);
        double distancia = 0;
        Color cor = Color.GRAY;
        ObjetoMapa objetoEncontrado = null;
        boolean paredeEncontrada = false;

        while (distancia < DISTANCIA_MAXIMA) {
            distancia += PASSO_RAYCAST;
            double x = jogador.getX() + raioX * distancia;
            double y = jogador.getY() + raioY * distancia;

            if (mapa.parede(x, y)) {
                cor = Color.rgb(115, 118, 125);
                paredeEncontrada = true;
                break;
            }

            ObjetoMapa objeto = mapa.objetoEm(x, y);
            // Objetos abertos também precisam ser desenhados: não confundir
            // "não sólido" (permite passagem) com "invisível".
            if (objeto != null) {
                objetoEncontrado = objeto;
                cor = corObjeto(objeto);
                break;
            }
        }

        double distanciaCorrigida = Math.max(0.05,
                distancia * Math.cos(anguloRaio - jogador.getAngulo()));
        double alturaParede = altura / distanciaCorrigida;
        double alturaObjeto = objetoEncontrado == null
                ? alturaParede
                : alturaParede * objetoEncontrado.getAlturaRelativa();
        double brilho = Math.max(0.18, Math.min(1,
                1 / (1 + distanciaCorrigida * 0.10)));

        gc.setFill(cor.deriveColor(0, 1, brilho, 1));
        double topoObjeto = objetoEncontrado == null
                ? horizonte - alturaObjeto / 2
                : horizonte + alturaParede / 2 - alturaObjeto;
        gc.fillRect(coluna, topoObjeto, 1, alturaObjeto);
    }

    private void desenharHud(double largura, double horizonte) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("W/S: mover   A/D ou ←/→: girar   E: abrir, entrar ou sair do armário",
                18, 28);
        gc.setFill(Color.color(1, 1, 1, 0.8));
        gc.fillOval(largura / 2 - 2, horizonte - 2, 4, 4);
    }

    private Color corObjeto(ObjetoMapa objeto) {
        if (objeto.getTipo() == TipoObjeto.MESA) {
            return Color.SADDLEBROWN;
        }
        if (objeto.getTipo() == TipoObjeto.ESTANTE) {
            return Color.DARKGREEN;
        }
        if (objeto.getTipo() == TipoObjeto.ARMARIO) {
            return objeto.isAberto() ? Color.SANDYBROWN : Color.DARKRED;
        }
        if (objeto.getTipo() == TipoObjeto.CAMA) {
            return Color.LIGHTSTEELBLUE;
        }
        return Color.GRAY;
    }
}
