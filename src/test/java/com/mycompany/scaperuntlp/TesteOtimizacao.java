package com.mycompany.scaperuntlp;

import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.view.JogoView;
import java.lang.reflect.Field;

/** Testes simples de cache e resolucao interna, sem biblioteca de testes. */
public class TesteOtimizacao {

    private static Object campo(Object alvo, String nome) throws Exception {
        Field campo = alvo.getClass().getDeclaredField(nome);
        campo.setAccessible(true);
        return campo.get(alvo);
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    public static void main(String[] args) throws Exception {
        JogoView view = new JogoView();
        Jogador jogador = new Jogador();
        MapaJogo mapa = new MapaJogo();

        view.desenhar(jogador, mapa);
        Object pecas1 = campo(view, "pecasCache");
        Object paredes1 = campo(view, "facesParedesCache");
        exigir((int) campo(view, "larguraRender") == 768, "Qualidade media padrao");
        exigir((int) campo(view, "alturaRender") == 512, "Proporcao 3:2 mantida");

        view.desenhar(jogador, mapa);
        exigir(pecas1 == campo(view, "pecasCache"), "Geometria reaproveitada sem mudancas");
        exigir(paredes1 == campo(view, "facesParedesCache"), "Paredes reaproveitadas");

        ObjetoMapa armario = mapa.objetoEm(16, 20);
        armario.abrir();
        view.desenhar(jogador, mapa);
        exigir(pecas1 != campo(view, "pecasCache"), "Abertura invalida cache de moveis");
        exigir(paredes1 == campo(view, "facesParedesCache"), "Abrir nao refaz paredes");

        view.alternarQualidade();
        view.desenhar(jogador, mapa);
        exigir((int) campo(view, "larguraRender") == 960, "Qualidade alta");
        exigir(((int[]) campo(view, "pixels")).length == 960 * 640,
                "Buffer de alta resolucao");

        view.alternarQualidade();
        view.desenhar(jogador, mapa);
        exigir((int) campo(view, "larguraRender") == 576, "Qualidade rapida");
        exigir(((int[]) campo(view, "pixels")).length == 576 * 384,
                "Buffer de baixa resolucao");

        view.alternarQualidade();
        exigir((int) campo(view, "larguraRender") == 768, "Retorno a media");
        System.out.println("OK: cache de paredes/moveis e tres niveis de renderizacao.");
    }
}
