package br.com.conde.conjuntos;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.conde.conjuntos.Ordens.Par;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Os testes das quatro versoes de arvore contra o juiz de rotulos. */
class ConjuntosTest {

    private final Contador contador = new Contador();

    /**
     * A VARREDURA COMPLETA: todas as ordens de uniao, as quatro versoes, conferindo a
     * PARTICAO inteira par por par.
     *
     * <p>Uma amostra de perguntas nao basta: uma compressao que reaponte um no para a raiz
     * errada responde certo em quase toda pergunta, e so o par exato que ela estragou
     * denuncia.
     */
    @Test
    @DisplayName("as quatro versoes batem com o juiz de rotulos em toda ordem de uniao")
    void aVarreduraCompleta() {
        for (long passo = 1; passo <= 3; passo++) {
            long semente = passo;

            Ordens.todas(200, semente).forEach((ordem, unioes) ->
                    Conjuntos.asDeArvore().forEach((versao, fabrica) -> {
                        Juiz.Veredito veredito = Juiz.julgar(fabrica.apply(200), unioes, contador);

                        assertThat(veredito.divergencias())
                                .as("%s em %s, semente %d: %s", versao, ordem, semente,
                                        veredito.primeiraDivergencia())
                                .isZero();

                        assertThat(veredito.perguntas()).isEqualTo(200 * 199 / 2);
                    }));
        }
    }

    /**
     * AS RAIZES SAO ESTAVEIS: dois elementos do mesmo grupo devolvem a MESMA raiz.
     *
     * <p>E uma propriedade separada das perguntas de "mesmo grupo", porque uma compressao
     * errada pode deixar dois elementos do mesmo grupo com raizes diferentes e, ainda
     * assim, responder certo enquanto ninguem perguntar por aquele par exato.
     */
    @Test
    @DisplayName("dois elementos do mesmo grupo sempre devolvem a mesma raiz")
    void asRaizesSaoEstaveis() {
        Ordens.todas(500, 1).forEach((ordem, unioes) ->
                Conjuntos.asDeArvore().forEach((versao, fabrica) -> {
                    Conjuntos conjuntos = Juiz.comAsUnioes(fabrica.apply(500), unioes, contador);

                    assertThat(Juiz.asRaizesSaoEstaveis(conjuntos, contador))
                            .as("%s em %s", versao, ordem).isTrue();
                }));
    }

    @Test
    @DisplayName("unir dois elementos que ja estao juntos devolve falso e nao muda nada")
    void unirOQueJaEstaJunto() {
        Conjuntos.todas().forEach((nome, fabrica) -> {
            Conjuntos conjuntos = fabrica.apply(10);

            assertThat(conjuntos.une(1, 2, contador)).as(nome).isTrue();
            assertThat(conjuntos.une(1, 2, contador)).as(nome).isFalse();
            assertThat(conjuntos.une(2, 1, contador)).as(nome).isFalse();
            assertThat(conjuntos.quantosGrupos()).as(nome).isEqualTo(9);
        });
    }

    @Test
    @DisplayName("unir um elemento com ele mesmo nao muda nada")
    void unirConsigoMesmo() {
        Conjuntos.todas().forEach((nome, fabrica) -> {
            Conjuntos conjuntos = fabrica.apply(10);

            assertThat(conjuntos.une(3, 3, contador)).as(nome).isFalse();
            assertThat(conjuntos.quantosGrupos()).as(nome).isEqualTo(10);
            assertThat(conjuntos.mesmoGrupo(3, 3, contador)).as(nome).isTrue();
        });
    }

    @Test
    @DisplayName("os grupos caem de um a cada uniao que junta mesmo")
    void osGruposCaemDeUm() {
        Conjuntos.todas().forEach((nome, fabrica) -> {
            Conjuntos conjuntos = fabrica.apply(100);

            for (int i = 0; i + 1 < 100; i++) conjuntos.une(i, i + 1, contador);

            assertThat(conjuntos.quantosGrupos()).as(nome).isEqualTo(1);
            assertThat(conjuntos.tamanho()).as(nome).isEqualTo(100);

            for (int i = 0; i < 100; i++)
                assertThat(conjuntos.mesmoGrupo(0, i, contador)).as("%s, elemento %d", nome, i)
                        .isTrue();
        });
    }

    @Test
    @DisplayName("a transitividade vale: se a esta com b e b com c, entao a esta com c")
    void aTransitividade() {
        Conjuntos.todas().forEach((nome, fabrica) -> {
            Conjuntos conjuntos = fabrica.apply(50);

            conjuntos.une(1, 2, contador);
            conjuntos.une(2, 3, contador);
            conjuntos.une(10, 20, contador);

            assertThat(conjuntos.mesmoGrupo(1, 3, contador)).as(nome).isTrue();
            assertThat(conjuntos.mesmoGrupo(3, 1, contador)).as(nome).isTrue();
            assertThat(conjuntos.mesmoGrupo(1, 10, contador)).as(nome).isFalse();
            assertThat(conjuntos.quantosGrupos()).as(nome).isEqualTo(50 - 3);
        });
    }

    @Test
    @DisplayName("um elemento fora da faixa reclama em vez de corromper o vetor")
    void oElementoForaDaFaixa() {
        Conjuntos.todas().forEach((nome, fabrica) -> {
            Conjuntos conjuntos = fabrica.apply(10);

            assertThatThrownBy(() -> conjuntos.une(0, 10, contador)).as(nome)
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("0 a 9");

            assertThatThrownBy(() -> conjuntos.raizDe(-1, contador)).as(nome)
                    .isInstanceOf(IllegalArgumentException.class);
        });
    }

    // ───────────────────── as alturas ─────────────────────

    /**
     * O ACHADO QUE ME CORRIGIU: a ordem "une(0,1), une(1,2), une(2,3)" NAO quebra nada.
     *
     * <p>A arvore pendura o segundo argumento no primeiro, e o primeiro ja esta na arvore
     * grande: todo elemento novo aponta direto para a raiz e a altura fica em UM.
     *
     * <p>Os mesmos pares com os argumentos TROCADOS viram uma corrente de n elementos. O
     * que decide e qual dos dois lados fica por baixo.
     */
    @Test
    @DisplayName("a corrente nao quebra nada, e os mesmos pares trocados viram uma corrente de n")
    void aOrdemQueQuebraEhAOutra() {
        int n = 2_000;

        Conjuntos benigna = Juiz.comAsUnioes(Conjuntos.semTruque(n), Ordens.emCorrente(n), contador);
        Conjuntos adversaria = Juiz.comAsUnioes(Conjuntos.semTruque(n), Ordens.penduraAGrande(n), contador);

        assertThat(benigna.maiorAltura()).isEqualTo(1);
        assertThat(adversaria.maiorAltura()).isEqualTo(n - 1);

        // e os dois acabam com um grupo so: a particao e a mesma
        assertThat(benigna.quantosGrupos()).isEqualTo(1);
        assertThat(adversaria.quantosGrupos()).isEqualTo(1);
    }

    /**
     * A COMPRESSAO SOZINHA nao impede a arvore alta: ela a conserta no caminho de volta
     * da primeira busca.
     *
     * <p>Logo depois das unioes, a altura dela e a mesma da versao sem truque nenhum.
     */
    @Test
    @DisplayName("a compressao sozinha deixa a arvore alta e a achata na primeira busca")
    void aCompressaoConsertaDepoisDeDeixarAcontecer() {
        int n = 2_000;
        List<Par> adversaria = Ordens.penduraAGrande(n);

        Conjuntos semTruque = Juiz.comAsUnioes(Conjuntos.semTruque(n), adversaria, contador);
        Conjuntos comCompressao = Juiz.comAsUnioes(Conjuntos.soCompressao(n), adversaria, contador);

        assertThat(comCompressao.maiorAltura()).as("antes de buscar").isEqualTo(semTruque.maiorAltura());

        Juiz.buscarTodos(comCompressao, new Contador());

        assertThat(comCompressao.maiorAltura()).as("depois de buscar").isEqualTo(1);
        assertThat(semTruque.maiorAltura()).as("a sem truque continua alta").isEqualTo(n - 1);
    }

    /**
     * A UNIAO POR TAMANHO impede a arvore alta DE ANTEMAO: ela nunca deixa a arvore maior
     * ficar por baixo.
     */
    @Test
    @DisplayName("a uniao por tamanho mantem a altura no logaritmo em toda ordem")
    void aUniaoPorTamanhoNuncaDeixaCrescer() {
        int n = 20_000;
        double logaritmo = Math.log(n) / Math.log(2);

        Ordens.todas(n, 1).forEach((ordem, unioes) -> {
            Conjuntos conjuntos = Juiz.comAsUnioes(Conjuntos.soPorTamanho(n), unioes, contador);

            assertThat(conjuntos.maiorAltura()).as(ordem).isLessThanOrEqualTo((int) logaritmo + 1);
        });
    }

    @Test
    @DisplayName("o juiz de rotulos tem altura um por construcao")
    void oJuizNaoTemArvore() {
        Conjuntos juiz = Juiz.comAsUnioes(new Conjuntos.PorRotulo(100), Ordens.sorteada(100, 1), contador);

        assertThat(juiz.maiorAltura()).isEqualTo(1);
        assertThat(juiz.nome()).isEqualTo("por rotulo");
    }

    // ───────────────────── as pecas ─────────────────────

    @Test
    @DisplayName("as ordens de uniao sao o que prometem")
    void asOrdensDeUniao() {
        assertThat(Ordens.emCorrente(4)).containsExactly(
                new Par(0, 1), new Par(1, 2), new Par(2, 3));

        assertThat(Ordens.penduraAGrande(4)).containsExactly(
                new Par(1, 0), new Par(2, 1), new Par(3, 2));

        assertThat(Ordens.umPorVez(4)).containsExactly(
                new Par(0, 1), new Par(0, 2), new Par(0, 3));

        assertThat(Ordens.emTorneio(4)).hasSize(3);
        assertThat(Ordens.todas(10, 1)).hasSize(5);
    }

    /**
     * O DESLOCAMENTO DE 32 BITS: sem ele, os elementos sorteados alternariam par e impar,
     * e unir sempre um par com um impar e uma estrutura escondida.
     */
    @Test
    @DisplayName("o sorteio nao alterna par e impar")
    void oSorteioNaoAlterna() {
        Ordens.Sorteio sorteio = new Ordens.Sorteio(123);
        int alternou = 0;
        int anterior = sorteio.proximo() & 1;

        for (int i = 0; i < 1_000; i++) {
            int agora = sorteio.proximo() & 1;

            if (agora != anterior) alternou++;

            anterior = agora;
        }

        assertThat(alternou).isBetween(420, 580);
    }

    @Test
    @DisplayName("o contador separa saltos de compressoes")
    void oContador() {
        Contador proprio = new Contador();
        Conjuntos conjuntos = Conjuntos.soCompressao(10);

        conjuntos.une(1, 0, proprio);
        conjuntos.une(2, 1, proprio);
        proprio.zerar();

        conjuntos.raizDe(0, proprio);

        assertThat(proprio.buscas()).isEqualTo(1);
        assertThat(proprio.saltos()).isEqualTo(2);
        assertThat(proprio.compressoes()).isEqualTo(1);
        assertThat(proprio.saltosPorBusca()).isEqualTo(2.0);
        assertThat(proprio.toString()).contains("1 buscas");

        proprio.zerar();

        assertThat(proprio.saltosPorBusca()).isZero();
    }

    /**
     * A BUSCA E ITERATIVA e nao recursiva. A arvore sem truque com a ordem adversaria tem
     * profundidade n, e uma recursao de duzentos mil niveis estoura a pilha da maquina
     * virtual em tamanhos diferentes em cada sistema.
     */
    @Test
    @DisplayName("a busca aguenta uma corrente de duzentos mil elementos")
    void aBuscaAguentaProfundidade() {
        int n = 200_000;
        Conjuntos conjuntos = Juiz.comAsUnioes(Conjuntos.semTruque(n), Ordens.penduraAGrande(n),
                new Contador());

        assertThat(conjuntos.raizDe(0, contador)).isEqualTo(n - 1);
        assertThat(conjuntos.mesmoGrupo(0, n - 1, contador)).isTrue();
    }
}
