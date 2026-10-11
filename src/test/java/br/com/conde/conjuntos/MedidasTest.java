package br.com.conde.conjuntos;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Os testes das TABELAS que o medidor imprime.
 *
 * <p>Toda frase do README que e um numero esta aqui como afirmacao. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
class MedidasTest {

    private static final List<Medidas.LinhaCorrecao> CORRECAO = Medidas.aCorrecao();
    private static final List<Medidas.LinhaAltura> ALTURA = Medidas.aAltura();
    private static final List<Medidas.LinhaCusto> CUSTO = Medidas.oCusto();
    private static final List<Medidas.LinhaOrdem> ORDEM = Medidas.aOrdem();
    private static final List<Medidas.LinhaGanho> GANHO = Medidas.oGanho();

    // ───────────────────── a correcao ─────────────────────

    @Test
    @DisplayName("as vinte combinacoes de ordem e versao batem com o juiz de rotulos")
    void aTabelaDaCorrecao() {
        assertThat(CORRECAO).hasSize(5 * 4);
        assertThat(CORRECAO).allSatisfy(l -> {
            assertThat(l.divergencias()).as("%s em %s", l.versao(), l.ordem()).isZero();
            assertThat(l.perguntas()).isEqualTo(300 * 299 / 2);
        });

        // as quatro versoes acabam com o MESMO numero de grupos, em cada ordem
        for (Medidas.LinhaCorrecao l : CORRECAO)
            assertThat(l.grupos()).as("%s em %s", l.versao(), l.ordem())
                    .isEqualTo(acharCorrecao(l.ordem(), "os dois").grupos());
    }

    // ───────────────────── a altura ─────────────────────

    /**
     * O ACHADO QUE ME CORRIGIU: a ordem "une(0,1), une(1,2), une(2,3)" nao quebra nada.
     *
     * <p>Eu a escrevi como "a que faz a arvore degenerar". A arvore pendura o segundo
     * argumento no primeiro, e o primeiro ja esta na arvore grande: a altura fica em UM,
     * nas quatro versoes.
     */
    @Test
    @DisplayName("na ordem em corrente a altura fica em um, nas quatro versoes")
    void aCorrenteNaoQuebraNada() {
        for (Medidas.LinhaAltura l : ALTURA)
            if (l.ordem().equals("em corrente")) {
                assertThat(l.maiorAltura()).as(l.versao()).isEqualTo(1);

                // a media fica um fio abaixo de um: a RAIZ tem altura zero
                assertThat(l.alturaMedia()).as(l.versao()).isCloseTo(1.0, within(0.001));
            }
    }

    /**
     * E OS MESMOS PARES COM OS ARGUMENTOS TROCADOS viram uma corrente de n elementos.
     *
     * <p>O que decide e qual dos dois lados fica por baixo.
     */
    @Test
    @DisplayName("pendurando a arvore grande no elemento novo, a altura vira n menos um")
    void aOrdemAdversariaViraCorrente() {
        Medidas.LinhaAltura semTruque = acharAltura("pendura a grande", "sem truque");

        assertThat(semTruque.maiorAltura()).isEqualTo(semTruque.elementos() - 1);
        assertThat(semTruque.sobreOLogaritmo()).isGreaterThan(1_000);

        // e a uniao por tamanho a mantem em um
        assertThat(acharAltura("pendura a grande", "so por tamanho").maiorAltura()).isEqualTo(1);
        assertThat(acharAltura("pendura a grande", "os dois").maiorAltura()).isEqualTo(1);
    }

    /**
     * A COMPRESSAO SOZINHA nao IMPEDE a arvore alta: logo depois das unioes, a altura dela
     * e a mesma da versao sem truque nenhum.
     *
     * <p>Ela conserta no caminho de volta da primeira busca, que e outra coisa.
     */
    @Test
    @DisplayName("logo apos as unioes, a compressao sozinha tem a mesma altura da sem truque")
    void aCompressaoNaoImpedeAAlturaDeAcontecer() {
        for (String ordem : new String[] {"pendura a grande", "em torneio"}) {
            assertThat(acharAltura(ordem, "so compressao").maiorAltura())
                    .as(ordem).isEqualTo(acharAltura(ordem, "sem truque").maiorAltura());
        }
    }

    /**
     * NA ORDEM SORTEADA, cada truque sozinho ja derruba a altura de 1.983 para 11 ou 7,
     * e os dois juntos levam a 4.
     */
    @Test
    @DisplayName("na ordem sorteada a altura cai de 1983 para 11, para 7 e para 4")
    void aAlturaNaOrdemSorteada() {
        assertThat(acharAltura("sorteada", "sem truque").maiorAltura()).isGreaterThan(1_000);
        assertThat(acharAltura("sorteada", "so compressao").maiorAltura()).isLessThan(20);
        assertThat(acharAltura("sorteada", "so por tamanho").maiorAltura()).isLessThan(10);
        assertThat(acharAltura("sorteada", "os dois").maiorAltura()).isLessThan(6);

        assertThat(acharAltura("sorteada", "os dois").maiorAltura())
                .isLessThan(acharAltura("sorteada", "so por tamanho").maiorAltura());
    }

    // ───────────────────── o custo ─────────────────────

    /**
     * O NUMERO QUE FECHA O REPOSITORIO: sem truque nenhum, uma busca custa 49.999 saltos
     * na ordem adversaria.
     */
    @Test
    @DisplayName("sem truque, uma busca custa cinquenta mil saltos, e com qualquer truque custa dois")
    void aTabelaDoCusto() {
        assertThat(CUSTO).hasSize(5 * 4);

        Medidas.LinhaCusto semTruque = acharCusto("pendura a grande", "sem truque");

        assertThat(semTruque.saltosPorBusca()).isGreaterThan(0.4 * Medidas.ELEMENTOS);

        for (String versao : new String[] {"so compressao", "so por tamanho", "os dois"})
            assertThat(acharCusto("pendura a grande", versao).saltosPorBusca())
                    .as(versao).isLessThanOrEqualTo(2.0);
    }

    /**
     * A COMPRESSAO COBRA um ponteiro reapontado por no do caminho, UMA VEZ SO: o no
     * comprimido nao e comprimido de novo.
     */
    @Test
    @DisplayName("so quem comprime tem compressoes, e quem nao comprime tem zero")
    void soQuemComprimeComprime() {
        for (Medidas.LinhaCusto l : CUSTO)
            if (l.versao().equals("sem truque") || l.versao().equals("so por tamanho"))
                assertThat(l.compressoesPorBusca()).as("%s em %s", l.versao(), l.ordem()).isZero();

        assertThat(acharCusto("pendura a grande", "so compressao").compressoesPorBusca())
                .isPositive();
    }

    /**
     * E A UNIAO POR TAMANHO nao muda NADA numa ordem em torneio: ali as arvores ja sao do
     * mesmo tamanho quando se juntam, e escolher pela maior nao escolhe nada.
     */
    @Test
    @DisplayName("em torneio, a uniao por tamanho da exatamente o mesmo custo de nao ter truque")
    void emTorneioAUniaoPorTamanhoNaoMudaNada() {
        assertThat(acharCusto("em torneio", "so por tamanho").saltosPorBusca())
                .isEqualTo(acharCusto("em torneio", "sem truque").saltosPorBusca());

        // e a compressao, ali, derruba o custo a dois
        assertThat(acharCusto("em torneio", "so compressao").saltosPorBusca())
                .isCloseTo(2.0, within(0.001));
    }

    // ───────────────────── a ordem ─────────────────────

    /**
     * A ALTURA SOBRE N e um: a arvore sem truque e literalmente uma corrente, em todo
     * tamanho.
     */
    @Test
    @DisplayName("a altura da arvore sem truque e n menos um, em mil, dez mil e cinquenta mil")
    void aTabelaDaOrdem() {
        assertThat(ORDEM).hasSize(3 * 4);

        for (Medidas.LinhaOrdem l : ORDEM) {
            if (!l.versao().equals("sem truque")) continue;

            assertThat(l.maiorAltura()).as("com %d elementos", l.elementos())
                    .isEqualTo(l.elementos() - 1);

            assertThat(l.sobreOTamanho()).as("com %d elementos", l.elementos())
                    .isCloseTo(1.0, within(0.001));

            // e uma busca custa metade de n, que e a altura media da corrente
            assertThat(l.saltosPorBusca()).as("com %d elementos", l.elementos())
                    .isCloseTo(l.elementos() / 2.0, within(1.0));
        }
    }

    @Test
    @DisplayName("as tres que tem algum truque ficam com altura um ou saltos dois, em todo tamanho")
    void asTresComTruqueNaoDegeneram() {
        for (Medidas.LinhaOrdem l : ORDEM) {
            if (l.versao().equals("sem truque")) continue;

            assertThat(l.saltosPorBusca()).as("%s com %d elementos", l.versao(), l.elementos())
                    .isLessThanOrEqualTo(2.0);
        }
    }

    // ───────────────────── o ganho ─────────────────────

    /**
     * O ACHADO PRINCIPAL: CADA TRUQUE SOZINHO ja leva o custo de 50.000 saltos para 2 ou
     * para 1.
     *
     * <p>O segundo truque compra a diferenca entre 1,65 e 1,11 na ordem sorteada, que e
     * uns quarenta por cento, e nao uma ordem de grandeza. O salto de qualidade esta no
     * PRIMEIRO truque, qualquer que seja ele.
     */
    @Test
    @DisplayName("cada truque sozinho ja resolve, e o segundo compra quarenta por cento")
    void aTabelaDoGanho() {
        assertThat(GANHO).hasSize(4);

        Medidas.LinhaGanho semTruque = acharGanho("sem truque");
        Medidas.LinhaGanho soCompressao = acharGanho("so compressao");
        Medidas.LinhaGanho soPorTamanho = acharGanho("so por tamanho");
        Medidas.LinhaGanho osDois = acharGanho("os dois");

        assertThat(semTruque.quantasVezesOCompleto()).isGreaterThan(0.4 * Medidas.ELEMENTOS);

        assertThat(soCompressao.quantasVezesOCompleto()).isLessThanOrEqualTo(2.0);
        assertThat(soPorTamanho.quantasVezesOCompleto()).isLessThanOrEqualTo(1.0);

        // e no sorteio o segundo truque compra uns quarenta por cento, e nao uma ordem
        assertThat(soCompressao.saltosPorBuscaNoSorteio() / osDois.saltosPorBuscaNoSorteio())
                .isBetween(1.1, 2.0);

        assertThat(soPorTamanho.saltosPorBuscaNoSorteio() / osDois.saltosPorBuscaNoSorteio())
                .isBetween(1.1, 2.0);
    }

    /**
     * E NO SORTEIO a compressao sozinha e MELHOR do que a uniao por tamanho sozinha,
     * apesar de deixar a arvore cem mil vezes mais alta na ordem adversaria.
     */
    @Test
    @DisplayName("no sorteio a compressao sozinha ganha da uniao por tamanho sozinha")
    void noSorteioACompressaoGanha() {
        assertThat(acharGanho("so compressao").saltosPorBuscaNoSorteio())
                .isLessThan(acharGanho("so por tamanho").saltosPorBuscaNoSorteio());

        // e na ordem adversaria ela perde, e perde feio na ALTURA
        assertThat(acharGanho("so compressao").alturaNaCorrente())
                .isGreaterThan(10_000 * acharGanho("so por tamanho").alturaNaCorrente());
    }

    // ───────────────────── achadores ─────────────────────

    private static Medidas.LinhaCorrecao acharCorrecao(String ordem, String versao) {
        return CORRECAO.stream()
                .filter(l -> l.ordem().equals(ordem) && l.versao().equals(versao))
                .findFirst()
                .orElseThrow();
    }

    private static Medidas.LinhaAltura acharAltura(String ordem, String versao) {
        return ALTURA.stream()
                .filter(l -> l.ordem().equals(ordem) && l.versao().equals(versao))
                .findFirst()
                .orElseThrow();
    }

    private static Medidas.LinhaCusto acharCusto(String ordem, String versao) {
        return CUSTO.stream()
                .filter(l -> l.ordem().equals(ordem) && l.versao().equals(versao))
                .findFirst()
                .orElseThrow();
    }

    private static Medidas.LinhaGanho acharGanho(String versao) {
        return GANHO.stream().filter(l -> l.versao().equals(versao)).findFirst().orElseThrow();
    }
}
