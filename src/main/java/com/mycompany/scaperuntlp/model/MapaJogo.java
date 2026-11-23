package com.mycompany.scaperuntlp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mapa 40x40. Coordena colisões físicas com paredes e móveis. */
public class MapaJogo {
    public static final int LARGURA = 40;
    public static final int ALTURA = 40;

    private final int[][] paredes = new int[ALTURA][LARGURA];
    private final List<ObjetoMapa> objetos = new ArrayList<>();

    public MapaJogo() {
        criarLimites();
        criarDivisorias();
        adicionarMoveis();
    }

    private void criarLimites() {
        for (int y = 0; y < ALTURA; y++) {
            for (int x = 0; x < LARGURA; x++) {
                paredes[y][x] = x == 0 || y == 0 || x == LARGURA - 1 || y == ALTURA - 1 ? 1 : 0;
            }
        }
    }

    private void criarDivisorias() {
        for (int x = 5; x < 35; x++) {
            if (x != 12 && x != 25) paredes[10][x] = 1;
        }
        for (int y = 4; y < 36; y++) {
            if (y != 17 && y != 29) paredes[y][20] = 1;
        }
        for (int x = 2; x < 18; x++) {
            if (x != 8) paredes[25][x] = 1;
        }
        for (int y = 12; y < 25; y++) {
            if (y != 19) paredes[y][8] = 1;
        }
    }

    private void adicionarMoveis() {
        adicionar(TipoObjeto.MESA, 6, 6, 0.32, 0);
        adicionar(TipoObjeto.MESA, 14, 6, 0.36, 90);
        adicionar(TipoObjeto.MESA, 29, 7, 0.31, 0);

        adicionar(TipoObjeto.ESTANTE, 4, 14, 0.72, 90);
        adicionar(TipoObjeto.ESTANTE, 11, 15, 0.78, 0);
        adicionar(TipoObjeto.ESTANTE, 30, 15, 0.75, 90);
        adicionar(TipoObjeto.ESTANTE, 34, 30, 0.68, 0);

        adicionar(TipoObjeto.ARMARIO, 16, 20, 0.84, 0);
        adicionar(TipoObjeto.ARMARIO, 27, 22, 0.87, 90);
        adicionar(TipoObjeto.ARMARIO, 35, 8, 0.82, 0);

        adicionar(TipoObjeto.CAMA, 12, 5, 0.22, 0);
        adicionar(TipoObjeto.CAMA, 31, 27, 0.24, 90);
        adicionar(TipoObjeto.CAMA, 5, 32, 0.21, 0);
    }

    public ObjetoMapa adicionar(TipoObjeto tipo, int x, int y) {
        return adicionar(tipo, x, y, tipo.getAlturaRelativa(), 0);
    }

    public ObjetoMapa adicionar(TipoObjeto tipo, int x, int y, double altura) {
        return adicionar(tipo, x, y, altura, 0);
    }

    public ObjetoMapa adicionar(TipoObjeto tipo, int x, int y, double altura, int rotacao) {
        ObjetoMapa novo = new ObjetoMapa(tipo, x, y, altura);
        novo.setRotacao(rotacao);
        if (x < 1 || y < 1 || x >= LARGURA - 1 || y >= ALTURA - 1) {
            throw new IllegalArgumentException("Móvel fora dos limites internos do mapa.");
        }
        for (int cy = (int) Math.floor(novo.getMinY()); cy <= (int) Math.floor(novo.getMaxY()); cy++) {
            for (int cx = (int) Math.floor(novo.getMinX()); cx <= (int) Math.floor(novo.getMaxX()); cx++) {
                if (celulaParede(cx, cy)) {
                    throw new IllegalArgumentException("Móvel invade parede em " + cx + "," + cy);
                }
            }
        }
        for (ObjetoMapa outro : objetos) {
            if (novo.getMinX() < outro.getMaxX() && novo.getMaxX() > outro.getMinX()
                    && novo.getMinY() < outro.getMaxY() && novo.getMaxY() > outro.getMinY()) {
                throw new IllegalArgumentException("Móveis sobrepostos: " + x + "," + y);
            }
        }
        objetos.add(novo);
        return novo;
    }

    public boolean celulaParede(int x, int y) {
        return x < 0 || y < 0 || x >= LARGURA || y >= ALTURA || paredes[y][x] != 0;
    }

    public boolean parede(double x, double y) {
        return celulaParede((int) Math.floor(x), (int) Math.floor(y));
    }

    /** Mantido para consultas pela célula central do móvel. */
    public ObjetoMapa objetoEm(int x, int y) {
        for (ObjetoMapa objeto : objetos) {
            if (objeto.getX() == x && objeto.getY() == y) return objeto;
        }
        return null;
    }

    /** Consulta pela área física do móvel, inclusive camas maiores que uma célula. */
    public ObjetoMapa objetoEm(double x, double y) {
        for (ObjetoMapa objeto : objetos) {
            if (objeto.contem(x, y)) return objeto;
        }
        return null;
    }

    public boolean bloqueado(double x, double y) {
        return !livreParaCirculo(x, y, 0);
    }

    /** Testa círculos contra os retângulos das paredes e dos móveis. */
    public boolean livreParaCirculo(double x, double y, double raio) {
        if (raio < 0 || !Double.isFinite(x) || !Double.isFinite(y)) return false;
        int minX = (int) Math.floor(x - raio);
        int maxX = (int) Math.floor(x + raio);
        int minY = (int) Math.floor(y - raio);
        int maxY = (int) Math.floor(y + raio);

        for (int cy = minY; cy <= maxY; cy++) {
            for (int cx = minX; cx <= maxX; cx++) {
                if (!celulaParede(cx, cy)) continue;
                double pontoX = Math.max(cx, Math.min(x, cx + 1.0));
                double pontoY = Math.max(cy, Math.min(y, cy + 1.0));
                double dx = x - pontoX;
                double dy = y - pontoY;
                if (dx * dx + dy * dy <= raio * raio) return false;
            }
        }
        for (ObjetoMapa objeto : objetos) {
            if (objeto.isSolido() && objeto.colideComCirculo(x, y, raio)) return false;
        }
        return true;
    }

    /** A linha é bloqueada por paredes, útil para verificação de interação. */
    public boolean linhaSemParede(double x0, double y0, double x1, double y1) {
        double distancia = Math.hypot(x1 - x0, y1 - y0);
        int passos = Math.max(1, (int) Math.ceil(distancia / 0.07));
        for (int i = 0; i <= passos; i++) {
            double t = (double) i / passos;
            if (parede(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t)) return false;
        }
        return true;
    }

    public List<ObjetoMapa> getObjetos() {
        return Collections.unmodifiableList(objetos);
    }
}
