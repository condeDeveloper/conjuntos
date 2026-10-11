package br.com.conde.conjuntos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AS ORDENS DE UNIAO que as tabelas medem.
 *
 * <p>A ordem em que as unioes chegam decide tudo. Unir 0 com 1, depois 1 com 2, depois 2
 * com 3 faz a arvore sem truque virar uma CORRENTE de n elementos; unir pares sorteados
 * faz ela ficar baixa por acaso.
 *
 * <p>E a ordem EM CORRENTE nao e exotica: e o que um laco sobre uma lista faz.
 */
public final class Ordens {

    private Ordens() {
    }

    /** Um par de elementos a unir. */
    public record Par(int a, int b) {
    }

    /**
     * O CONGRUENTE LINEAR do repositorio, com o descarte dos bits de baixo.
     *
     * <p>O deslocamento de 32 bits tira os bits que alternam par e impar a cada passo.
     * Sem ele, os elementos sorteados alternariam par e impar, e unir sempre um par com um
     * impar e uma estrutura escondida: a arvore sairia mais baixa do que sairia de um
     * sorteio de verdade.
     */
    public static final class Sorteio {

        private long estado;

        public Sorteio(long semente) {
            this.estado = semente == 0 ? 1 : semente;
        }

        public int proximo() {
            estado = estado * 6364136223846793005L + 1442695040888963407L;

            return (int) (estado >>> 32);
        }

        public int ate(int limite) {
            if (limite <= 0) throw new IllegalArgumentException("o limite tem de ser positivo");

            return (int) (Integer.toUnsignedLong(proximo()) % limite);
        }
    }

    /**
     * EM CORRENTE: une(0, 1), une(1, 2), une(2, 3). O que um laco sobre uma lista faz.
     *
     * <p>E ela NAO quebra nada, e isso e o achado. A arvore sem truque pendura o segundo
     * argumento no primeiro, e o primeiro ja esta na arvore grande: todo elemento novo
     * aponta direto para a raiz e a altura fica em UM.
     *
     * <p>Eu tinha escrito esta ordem como "a que faz a arvore degenerar". Ela e a que mais
     * ajuda.
     */
    public static List<Par> emCorrente(int n) {
        List<Par> pares = new ArrayList<>();

        for (int i = 0; i + 1 < n; i++) pares.add(new Par(i, i + 1));

        return pares;
    }

    /**
     * PENDURANDO A GRANDE: une(1, 0), une(2, 1), une(3, 2).
     *
     * <p>Os mesmos pares da corrente, com os argumentos TROCADOS. O primeiro argumento e
     * sempre o elemento sozinho, e a arvore sem truque pendura o segundo no primeiro:
     * a arvore inteira passa a pender do elemento novo, e a altura cresce de um a cada
     * uniao.
     *
     * <p>Essa e a ordem que faz a arvore degenerar, e ela tambem nao e exotica: e o que um
     * laco sobre uma lista de arestas faz quando a aresta vem com o no novo na frente.
     */
    public static List<Par> penduraAGrande(int n) {
        List<Par> pares = new ArrayList<>();

        for (int i = 0; i + 1 < n; i++) pares.add(new Par(i + 1, i));

        return pares;
    }

    /** SORTEADA: pares sorteados, que e o caso medio. */
    public static List<Par> sorteada(int n, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        List<Par> pares = new ArrayList<>();

        for (int i = 0; i < n; i++) pares.add(new Par(sorteio.ate(n), sorteio.ate(n)));

        return pares;
    }

    /**
     * EM TORNEIO: une de dois em dois, depois de quatro em quatro.
     *
     * <p>E a ordem que deixa a arvore mais baixa sem truque nenhum: ela ja une arvores de
     * tamanhos parecidos, que e exatamente o que a uniao por tamanho faria sozinha.
     */
    public static List<Par> emTorneio(int n) {
        List<Par> pares = new ArrayList<>();

        for (int passo = 1; passo < n; passo *= 2)
            for (int i = 0; i + passo < n; i += 2 * passo) pares.add(new Par(i, i + passo));

        return pares;
    }

    /**
     * A ORDEM FEITA PARA QUEBRAR A UNIAO POR TAMANHO sem compressao.
     *
     * <p>Une sempre uma arvore grande com um elemento SOZINHO, mas tomando o elemento mais
     * fundo da arvore grande como primeiro argumento. Nao muda a altura, porque a uniao
     * por tamanho decide pela raiz; serve para mostrar que ela NAO e quebravel assim.
     */
    public static List<Par> umPorVez(int n) {
        List<Par> pares = new ArrayList<>();

        for (int i = 1; i < n; i++) pares.add(new Par(0, i));

        return pares;
    }

    /** As formas, na ordem em que as tabelas as mostram. */
    public static Map<String, List<Par>> todas(int n, long semente) {
        Map<String, List<Par>> mapa = new LinkedHashMap<>();

        mapa.put("em corrente", emCorrente(n));
        mapa.put("pendura a grande", penduraAGrande(n));
        mapa.put("um por vez", umPorVez(n));
        mapa.put("em torneio", emTorneio(n));
        mapa.put("sorteada", sorteada(n, semente));

        return mapa;
    }
}
