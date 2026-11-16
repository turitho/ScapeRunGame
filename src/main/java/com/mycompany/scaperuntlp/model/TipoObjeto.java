package com.mycompany.scaperuntlp.model;

/** Tipos de móveis presentes no mapa e suas alturas relativas à parede. */
public enum TipoObjeto {
    MESA(0.42),
    ESTANTE(0.90),
    ARMARIO(0.95),
    CAMA(0.30);

    private final double alturaRelativa;

    TipoObjeto(double alturaRelativa) {
        this.alturaRelativa = alturaRelativa;
    }

    public double getAlturaRelativa() {
        return alturaRelativa;
    }
}
