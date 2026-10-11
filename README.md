# conjuntos

Conjuntos disjuntos do zero em Java 21: sem truque nenhum, só com compressão de caminho,
só com união por tamanho, e com os dois.

O juiz é o conjunto **por rótulo**: guarda o rótulo do grupo de cada elemento e, para
juntar, reescreve todos os rótulos de um lado. Custa n por união e é obviamente correto,
porque não há caminho nenhum a subir para errar.

E o que ele confere não é a resposta de cada pergunta: é a **partição inteira**, par por
par. Com 300 elementos são 44.850 perguntas. Uma compressão que reaponte um nó para a raiz
errada responde certo em quase toda pergunta, e só o par exato que ela estragou denuncia.

A unidade é o **salto de ponteiro**: a conta inteira de conjuntos disjuntos é subir de pai
em pai até a raiz, e o que os truques fazem é encurtar esse caminho.

## Cinquenta mil saltos contra um

```
$ mvn exec:java -Dexec.args="ordem"

     elementos versao                 maior altura     saltos por busca      saltos nas unioes       altura sobre n
         50000 sem truque                    49999             24999.50                      0               1.0000
         50000 so compressao                 49999                 2.00                      0               1.0000
         50000 so por tamanho                    1                 1.00                  49997               0.0000
         50000 os dois                           1                 1.00                  49997               0.0000
```

Sem truque nenhum, a árvore é **literalmente uma corrente**: a altura é n−1 e uma busca do
fundo custa n saltos. A coluna da direita é 1,0000.

E note a linha do meio: **só compressão também deixa a altura em 49.999**. Ela não impede a
árvore alta, ela a conserta no caminho de volta da primeira busca. Depois dessa busca, dois
saltos.

A união por tamanho faz o contrário: impede de antemão, e paga 49.997 saltos nas uniões
para isso.

## Cada truque sozinho já resolve

```
$ mvn exec:java -Dexec.args="ganho"

  versao             saltos por busca na corrente saltos por busca no sorteio     altura na corrente quantas vezes os dois juntos
  sem truque                              9999.50                    1084.37                  19999                    10000.0
  so compressao                              2.00                       1.64                  19999                        2.0
  so por tamanho                             1.00                       1.82                      1                        1.0
  os dois                                    1.00                       1.11                      1                        1.0
```

O salto de qualidade está no **primeiro** truque, qualquer que seja ele: de 9.999 saltos
para 2,00 ou 1,00.

O segundo compra a diferença entre 1,64 e 1,11 na ordem sorteada. Uns 40%, e não uma ordem
de grandeza.

E os dois sozinhos trocam de lugar conforme a ordem. Na ordem sorteada, a compressão sozinha
ganha (1,64 contra 1,82). Na ordem adversária ela perde, e perde na **altura**: 19.999
contra 1.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `altura`, `custo`, `ordem`, `ganho` e `tudo`.

## O que as medidas me corrigiram

**A ordem que eu escrevi como "a que faz a árvore degenerar" é a que mais ajuda.**

Unir 0 com 1, depois 1 com 2, depois 2 com 3: a corrente clássica, o que um laço sobre uma
lista faz. Eu a escrevi como o caso adversário e a tabela deu **altura 1** nas quatro
versões.

A árvore pendura o **segundo** argumento no primeiro, e o primeiro já está na árvore grande.
Todo elemento novo aponta direto para a raiz.

Os mesmos pares com os argumentos **trocados** — `une(1,0)`, `une(2,1)`, `une(3,2)` — viram
uma corrente de n elementos.

```
$ mvn exec:java -Dexec.args="altura"

  ordem              versao                 maior altura       altura media   sobre o logaritmo de n
  em corrente        sem truque                        1               1.00                      0.1
  pendura a grande   sem truque                    19999            9999.50                   1399.7
  pendura a grande   so compressao                 19999            9999.50                   1399.7
  pendura a grande   so por tamanho                    1               1.00                      0.1
  em torneio         sem truque                       14               6.96                      1.0
  sorteada           sem truque                     1983            1084.37                    138.8
```

O que decide não é a ordem dos pares: é **qual dos dois lados fica por baixo**. E essa
também não é uma ordem exótica: é o que um laço sobre uma lista de arestas faz quando a
aresta vem com o nó novo na frente.

**A união por tamanho não muda nada numa ordem em torneio.**

```
$ mvn exec:java -Dexec.args="custo"

  ordem              versao                 saltos por busca    compressoes por busca      saltos nas unioes
  em torneio         sem truque                         6.96                     0.00                      0
  em torneio         so compressao                      2.00                     1.00                      0
  em torneio         so por tamanho                     6.96                     0.00                      0
  em torneio         os dois                            2.00                     1.00                      0
```

Exatamente o mesmo número, com e sem o truque. Numa ordem em torneio as árvores já são do
mesmo tamanho quando se juntam, e escolher pela maior não escolhe nada.

A união por tamanho não é uma melhoria: é uma **proteção contra uma ordem ruim**. Onde a
ordem já é boa, ela cobra a conta de manter o tamanho e não entrega nada.

**A suíte levava dois minutos e meio, e a culpa era de medir o caso ruim grande demais.**

A tabela do custo roda a versão sem truque com 100.000 elementos e uniões sorteadas. A
árvore dela fica com altura média de 1.084, e as uniões custam **221 milhões de saltos**.

O tamanho caiu para 20.000. O que as tabelas afirmam são **razões**, e elas não mudam: o
custo da versão sem truque continua sendo metade de n, e o das outras continua sendo 1 ou 2.

## Licença

MIT.
