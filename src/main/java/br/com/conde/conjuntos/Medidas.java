package br.com.conde.conjuntos;

import java.util.ArrayList;
import java.util.List;

import br.com.conde.conjuntos.Ordens.Par;

/**
 * As MEDIDAS, em saltos de ponteiro e em altura de arvore, nunca em segundos.
 *
 * <p>Segundo nao e medida: muda com a maquina, com o que mais esta rodando e com o humor
 * do coletor de lixo, e a integracao continua roda em tres sistemas.
 *
 * <p>O SALTO e a medida certa e nao so a conveniente: a conta inteira de conjuntos
 * disjuntos e subir de pai em pai ate a raiz, e o que os truques fazem e encurtar esse
 * caminho.
 */
public final class Medidas {

    private Medidas() {
    }

    /**
     * Quantos elementos as tabelas usam.
     *
     * <p>Vinte mil e nao cem mil por causa da versao SEM TRUQUE: com cem mil elementos e
     * unioes sorteadas, a arvore dela fica com altura media de mil e as unioes custam
     * duzentos e vinte e um MILHOES de saltos. A suite levava dois minutos e meio.
     *
     * <p>O que as tabelas afirmam sao RAZOES, e elas nao mudam com o tamanho: o custo da
     * versao sem truque continua sendo metade de n e o das outras continua sendo um ou
     * dois.
     */
    public static final int ELEMENTOS = 20_000;

    // ───────────────────── 1. a correcao ─────────────────────

    public record LinhaCorrecao(String ordem, String versao, int elementos, int unioes,
                                int perguntas, int divergencias, int grupos) {
    }

    /**
     * TODA UNIAO BATE COM O JUIZ DE ROTULOS, em todas as ordens de uniao.
     *
     * <p>A conferencia e da PARTICAO inteira, par por par, e nao de uma amostra. Uma
     * compressao que reaponte um no para a raiz errada responde certo em quase toda
     * pergunta: so o par exato que ela estragou denuncia.
     */
    public static List<LinhaCorrecao> aCorrecao() {
        List<LinhaCorrecao> linhas = new ArrayList<>();
        Contador contador = new Contador();
        int n = 300;

        Ordens.todas(n, 1).forEach((ordem, unioes) ->
                Conjuntos.asDeArvore().forEach((versao, fabrica) -> {
                    Juiz.Veredito veredito = Juiz.julgar(fabrica.apply(n), unioes, contador);

                    linhas.add(new LinhaCorrecao(ordem, versao, n, unioes.size(),
                            veredito.perguntas(), veredito.divergencias(), veredito.grupos()));
                }));

        return linhas;
    }

    // ───────────────────── 2. a altura ─────────────────────

    public record LinhaAltura(String ordem, String versao, int elementos,
                              int maiorAltura, double alturaMedia, double sobreOLogaritmo) {
    }

    /**
     * A ALTURA que cada versao deixa a arvore ficar, por ordem de uniao.
     *
     * <p>A coluna da direita e a altura dividida pelo logaritmo de n, que e o que uma
     * arvore equilibrada daria. Um na coluna quer dizer equilibrada; cinco mil quer dizer
     * corrente.
     */
    public static List<LinhaAltura> aAltura() {
        List<LinhaAltura> linhas = new ArrayList<>();
        Contador contador = new Contador();
        int n = 20_000;
        double logaritmo = Math.log(n) / Math.log(2);

        Ordens.todas(n, 1).forEach((ordem, unioes) ->
                Conjuntos.asDeArvore().forEach((versao, fabrica) -> {
                    Conjuntos.Arvore arvore = (Conjuntos.Arvore) Juiz.comAsUnioes(
                            fabrica.apply(n), unioes, contador);

                    linhas.add(new LinhaAltura(ordem, versao, n, arvore.maiorAltura(),
                            arvore.alturaMedia(), arvore.maiorAltura() / logaritmo));
                }));

        return linhas;
    }

    // ───────────────────── 3. o custo ─────────────────────

    public record LinhaCusto(String ordem, String versao, int elementos,
                             double saltosPorBusca, double compressoesPorBusca,
                             long saltosNasUnioes) {
    }

    /**
     * O CUSTO DE UMA BUSCA em saltos, DEPOIS de todas as unioes.
     *
     * <p>As duas colunas separam o que a compressao faz do que ela cobra. Ela diminui os
     * SALTOS das buscas seguintes e cobra um ponteiro reapontado por no do caminho, uma
     * vez so: o no comprimido nao e comprimido de novo.
     */
    public static List<LinhaCusto> oCusto() {
        List<LinhaCusto> linhas = new ArrayList<>();
        int n = ELEMENTOS;

        Ordens.todas(n, 1).forEach((ordem, unioes) ->
                Conjuntos.asDeArvore().forEach((versao, fabrica) -> {
                    Contador deUniao = new Contador();
                    Conjuntos conjuntos = Juiz.comAsUnioes(fabrica.apply(n), unioes, deUniao);

                    Contador deBusca = new Contador();

                    Juiz.buscarTodos(conjuntos, deBusca);

                    linhas.add(new LinhaCusto(ordem, versao, n, deBusca.saltosPorBusca(),
                            deBusca.compressoesPorBusca(), deUniao.saltos()));
                }));

        return linhas;
    }

    // ───────────────────── 4. a ordem ─────────────────────

    public record LinhaOrdem(int elementos, String versao, int maiorAltura,
                             double saltosPorBusca, long saltosNasUnioes,
                             double sobreOTamanho) {
    }

    /**
     * A ORDEM DE UNIAO QUE FAZ A ARVORE DEGENERAR, conforme o tamanho cresce.
     *
     * <p>Unir 1 com 0, depois 2 com 1, depois 3 com 2. Sem truque nenhum, a arvore vira
     * uma CORRENTE: a altura e n menos um e uma busca do fundo custa n saltos.
     *
     * <p>Os mesmos pares com os argumentos trocados nao quebram nada: ali todo elemento
     * novo aponta direto para a raiz e a altura fica em um. O que decide e QUAL dos dois
     * lados fica por baixo.
     *
     * <p>A coluna da direita e a altura dividida por n. Um quer dizer corrente.
     *
     * <p>E essa ordem nao e exotica: e o que um laco sobre uma lista de arestas faz.
     */
    public static List<LinhaOrdem> aOrdem() {
        List<LinhaOrdem> linhas = new ArrayList<>();

        for (int n : new int[] {1_000, 10_000, 50_000}) {
            List<Par> adversaria = Ordens.penduraAGrande(n);
            int finalN = n;

            Conjuntos.asDeArvore().forEach((versao, fabrica) -> {
                Contador deUniao = new Contador();
                Conjuntos conjuntos = Juiz.comAsUnioes(fabrica.apply(finalN), adversaria, deUniao);

                int altura = conjuntos.maiorAltura();

                Contador deBusca = new Contador();

                Juiz.buscarTodos(conjuntos, deBusca);

                linhas.add(new LinhaOrdem(finalN, versao, altura, deBusca.saltosPorBusca(),
                        deUniao.saltos(), (double) altura / finalN));
            });
        }

        return linhas;
    }

    // ───────────────────── 5. o ganho ─────────────────────

    public record LinhaGanho(String versao, double saltosPorBuscaNaCorrente,
                             double saltosPorBuscaNoSorteio, int alturaNaCorrente,
                             double quantasVezesOCompleto) {
    }

    /**
     * O QUE CADA TRUQUE SOZINHO JA RESOLVE.
     *
     * <p>Sao dois truques e eles nao valem a mesma coisa. A tabela compara cada um sozinho
     * com os dois juntos, na ordem que mais machuca.
     *
     * <p>A coluna da direita e quantas vezes o custo dos dois juntos cada versao paga.
     */
    public static List<LinhaGanho> oGanho() {
        List<LinhaGanho> linhas = new ArrayList<>();
        int n = ELEMENTOS;

        List<Par> corrente = Ordens.penduraAGrande(n);
        List<Par> sorteio = Ordens.sorteada(n, 1);

        double doCompleto = saltosDepoisDe(Conjuntos::completo, n, corrente);

        Conjuntos.asDeArvore().forEach((versao, fabrica) -> {
            Contador contador = new Contador();
            Conjuntos naCorrente = Juiz.comAsUnioes(fabrica.apply(n), corrente, contador);

            double naOrdemRuim = saltosDepoisDe(fabrica::apply, n, corrente);
            double noSorteio = saltosDepoisDe(fabrica::apply, n, sorteio);

            linhas.add(new LinhaGanho(versao, naOrdemRuim, noSorteio,
                    naCorrente.maiorAltura(),
                    doCompleto == 0 ? 0 : naOrdemRuim / doCompleto));
        });

        return linhas;
    }

    private static double saltosDepoisDe(java.util.function.IntFunction<Conjuntos> fabrica,
                                         int n, List<Par> unioes) {

        Conjuntos conjuntos = Juiz.comAsUnioes(fabrica.apply(n), unioes, new Contador());
        Contador deBusca = new Contador();

        Juiz.buscarTodos(conjuntos, deBusca);

        return deBusca.saltosPorBusca();
    }
}
