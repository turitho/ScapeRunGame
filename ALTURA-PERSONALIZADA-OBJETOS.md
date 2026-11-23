# Tamanhos dos móveis

As medidas padrão estão em `model/TipoObjeto.java`, e o mapa pode personalizá-las ao adicionar cada objeto em `model/MapaJogo.java`.

A câmera está a **0.64 unidade** do piso; paredes têm altura **1.00**. Mesas têm cerca de **0.32**, camas **0.22**, estantes **0.74** e armários **0.84** unidade. A cabeceira da cama ultrapassa levemente a altura principal.

```java
// Inserir móvel no mapa (em uma posição livre):
mapa.adicionar(TipoObjeto.MESA, 9, 7, 0.32, 0);

// Alterar individualmente um móvel existente:
ObjetoMapa mesa = mapa.objetoEm(6, 6);
mesa.setAlturaRelativa(0.36);
mesa.setDimensoes(0.90, 0.68);
mesa.setRotacao(90);
```

`setAlturaRelativa`, `setDimensoes` e `setRotacao` continuam funcionando. A colisão usa largura e profundidade. O desenho usa também a altura e as superfícies superior/inferior. **Alterações de dimensão em tempo de execução não refazem automaticamente a validação das posições de outros móveis próximos**, então configure as dimensões de cada móvel respeitando o espaço livre no mapa.
