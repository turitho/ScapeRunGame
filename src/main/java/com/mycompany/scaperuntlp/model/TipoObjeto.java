package com.mycompany.scaperuntlp.model;

/**
 * Dimensões em unidades do mapa (1 unidade = altura de uma parede).
 * A câmera fica a 0.64 unidades do piso: mesas e camas precisam ficar abaixo
 * dela, enquanto armários e estantes ficam próximos da altura de uma parede.
 */
public enum TipoObjeto {
    MESA(0.32, 0.90, 0.72),
    ESTANTE(0.74, 0.86, 0.40),
    ARMARIO(0.84, 0.92, 0.80),
    CAMA(0.22, 0.91, 1.65);

    private final double altura;
    private final double largura;
    private final double profundidade;

    TipoObjeto(double altura, double largura, double profundidade) {
        this.altura = altura;
        this.largura = largura;
        this.profundidade = profundidade;
    }

    public double getAlturaRelativa() { return altura; }
    public double getLargura() { return largura; }
    public double getProfundidade() { return profundidade; }
}
