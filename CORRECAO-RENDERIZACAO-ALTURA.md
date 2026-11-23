# Correção de buracos e oclusão na renderização

A implementação atual de `JogoView` usa DDA para encontrar paredes e calcula interseções de cada raio com as **caixas 3D** que compõem mesas, estantes, camas e armários.

A versão anterior calculava somente uma distância horizontal para cada caixa e desenhava um retângulo vertical, sem renderizar as tampas e as bases dos móveis. Isso provocava superfícies vazadas e sobreposições incorretas, principalmente perto da câmera.

Agora cada pixel da coluna possui uma profundidade armazenada em `profundidades[]`. A interseção analítica testa tanto a entrada e saída no plano XY quanto o intervalo entre `zMin` e `zMax`: se a interseção vertical acontecer depois da horizontal, a superfície visível é o topo ou a base da caixa. A cor do pixel somente é substituída quando o novo ponto de interseção está **mais próximo** que o que já foi desenhado.

Não é necessária uma textura transparente para mostrar o fundo: apenas as superfícies fisicamente presentes são rasterizadas. O vão sob mesas e entre prateleiras é intencional, mas o tampo e as caixas sólidas não ficam mais com buracos falsos.

**Limite:** continua sendo um raycaster de caixas ortogonais com rotações de 90°, e não um motor 3D genérico com malhas e sombras dinâmicas.
