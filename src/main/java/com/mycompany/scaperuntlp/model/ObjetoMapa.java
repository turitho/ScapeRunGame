package com.mycompany.scaperuntlp.model;

/** Representa um móvel ou objeto interativo em uma célula do mapa. */
public class ObjetoMapa {
    private final TipoObjeto tipo;
    private final int x;
    private final int y;
    private boolean aberto;

    public ObjetoMapa(TipoObjeto tipo, int x, int y) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
    }

    public TipoObjeto getTipo() {
        return tipo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isAberto() {
        return aberto;
    }

    public void abrir() {
        if (tipo == TipoObjeto.ARMARIO) {
            aberto = true;
        }
    }

    public void fechar() {
        if (tipo == TipoObjeto.ARMARIO) {
            aberto = false;
        }
    }

    public void alternarAberto() {
        if (tipo == TipoObjeto.ARMARIO) {
            aberto = !aberto;
        }
    }

    /** Armário aberto permite passagem; os demais móveis continuam sólidos. */
    public boolean isSolido() {
        return tipo != TipoObjeto.ARMARIO || !aberto;
    }

    public double getAlturaRelativa() {
        return tipo.getAlturaRelativa();
    }
}
