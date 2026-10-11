package br.com.conde.conjuntos;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * CONJUNTOS DISJUNTOS: uma coleccao de grupos que so sabe fazer duas coisas.
 *
 * <p>Perguntar se dois elementos estao no mesmo grupo, e juntar dois grupos. Nunca
 * separar: e por nao haver separacao que da para ser tao barato.
 *
 * <p>Sao cinco implementacoes, e quatro delas sao a mesma ideia com truques diferentes
 * ligados. A ideia e guardar, para cada elemento, quem e o PAI dele, e o grupo ser
 * identificado pela RAIZ que se alcanca subindo.
 *
 * <ul>
 *   <li>{@link PorRotulo} nao usa arvore nenhuma: guarda o rotulo do grupo de cada
 *       elemento e, para juntar, reescreve todos os rotulos de um lado. E o JUIZ.
 *   <li>{@link SemTruque} sobe ate a raiz e pendura uma na outra. A arvore pode virar
 *       uma corrente de n elementos.
 *   <li>{@link SoCompressao} aponta cada no visitado direto para a raiz, no caminho de
 *       volta.
 *   <li>{@link SoPorTamanho} pendura sempre a arvore MENOR na maior.
 *   <li>{@link Completo} liga os dois.
 * </ul>
 */
public interface Conjuntos {

    /** Junta os grupos de a e de b. Devolve falso se eles ja estavam juntos. */
    boolean une(int a, int b, Contador contador);

    /** a e b estao no mesmo grupo? */
    boolean mesmoGrupo(int a, int b, Contador contador);

    /** O representante do grupo de a. Dois elementos do mesmo grupo tem o mesmo. */
    int raizDe(int a, Contador contador);

    /** Quantos grupos existem agora. */
    int quantosGrupos();

    /** Quantos elementos ha ao todo. */
    int tamanho();

    /**
     * A MAIOR ALTURA de arvore da estrutura agora.
     *
     * <p>E a medida que separa as quatro versoes de arvore, e a unica que o juiz de
     * rotulos nao tem, porque ele nao tem arvore.
     */
    int maiorAltura();

    String nome();

    /**
     * O JUIZ: um ROTULO por elemento, e juntar e reescrever todos os rotulos de um lado.
     *
     * <p>Custa n por uniao e e obviamente correto: dois elementos estao no mesmo grupo
     * exatamente quando o rotulo deles e igual, e nao ha caminho nenhum a subir para
     * errar.
     *
     * <p>E por ser burro que ele julga. Ele nao tem arvore, entao nao pode ter arvore
     * torta; nao tem compressao, entao nao pode comprimir errado.
     */
    final class PorRotulo implements Conjuntos {

        private final int[] rotulo;
        private int grupos;

        public PorRotulo(int tamanho) {
            rotulo = new int[tamanho];

            for (int i = 0; i < tamanho; i++) rotulo[i] = i;

            grupos = tamanho;
        }

        @Override
        public boolean une(int a, int b, Contador contador) {
            conferir(a);
            conferir(b);

            int deA = rotulo[a];
            int deB = rotulo[b];

            if (deA == deB) return false;

            for (int i = 0; i < rotulo.length; i++) {
                contador.saltou();

                if (rotulo[i] == deB) rotulo[i] = deA;
            }

            grupos--;

            return true;
        }

        @Override
        public boolean mesmoGrupo(int a, int b, Contador contador) {
            conferir(a);
            conferir(b);
            contador.buscou();
            contador.saltou(2);

            return rotulo[a] == rotulo[b];
        }

        @Override
        public int raizDe(int a, Contador contador) {
            conferir(a);
            contador.buscou();
            contador.saltou();

            return rotulo[a];
        }

        @Override
        public int quantosGrupos() {
            return grupos;
        }

        @Override
        public int tamanho() {
            return rotulo.length;
        }

        @Override
        public int maiorAltura() {
            return 1;
        }

        @Override
        public String nome() {
            return "por rotulo";
        }

        private void conferir(int elemento) {
            if (elemento < 0 || elemento >= rotulo.length)
                throw new IllegalArgumentException("elemento fora de 0 a " + (rotulo.length - 1)
                        + ": " + elemento);
        }
    }

    /**
     * A ARVORE com os truques que se quiser ligados.
     *
     * <p>As quatro versoes sao esta mesma classe com dois interruptores, de proposito: um
     * codigo por versao esconderia que a diferenca entre "lento demais para usar" e
     * "praticamente constante" sao duas linhas.
     */
    final class Arvore implements Conjuntos {

        private final int[] pai;
        private final int[] tamanhoDaArvore;
        private final boolean comprimindo;
        private final boolean porTamanho;
        private final String nome;
        private int grupos;

        public Arvore(int tamanho, boolean comprimindo, boolean porTamanho, String nome) {
            pai = new int[tamanho];
            tamanhoDaArvore = new int[tamanho];

            for (int i = 0; i < tamanho; i++) {
                pai[i] = i;
                tamanhoDaArvore[i] = 1;
            }

            this.comprimindo = comprimindo;
            this.porTamanho = porTamanho;
            this.nome = nome;

            grupos = tamanho;
        }

        /**
         * Sobe ate a raiz, contando os saltos.
         *
         * <p>A COMPRESSAO e a segunda passada: depois de achar a raiz, o caminho inteiro
         * passa a apontar direto para ela. Esta escrita como dois lacos e nao como
         * recursao de proposito: a profundidade da arvore sem truque nenhum e o numero de
         * elementos, e uma recursao de um milhao de niveis estoura a pilha da maquina
         * virtual em tamanhos diferentes em cada sistema.
         */
        @Override
        public int raizDe(int a, Contador contador) {
            conferir(a);
            contador.buscou();

            int raiz = a;

            while (pai[raiz] != raiz) {
                contador.saltou();
                raiz = pai[raiz];
            }

            if (comprimindo) {
                int andando = a;

                while (pai[andando] != raiz) {
                    int proximo = pai[andando];

                    pai[andando] = raiz;
                    andando = proximo;

                    contador.comprimiu();
                }
            }

            return raiz;
        }

        @Override
        public boolean une(int a, int b, Contador contador) {
            int raizDeA = raizDe(a, contador);
            int raizDeB = raizDe(b, contador);

            if (raizDeA == raizDeB) return false;

            /*
             * POR TAMANHO: a arvore MENOR e pendurada na maior.
             *
             * Sem isto, pendurar sempre a primeira na segunda deixa a altura crescer de um
             * a cada uniao quando as unioes chegam numa ordem infeliz, e uma ordem infeliz
             * nao e exotica: unir 0 com 1, depois 1 com 2, depois 2 com 3 e o que um laco
             * sobre uma lista faz.
             */
            if (porTamanho && tamanhoDaArvore[raizDeA] < tamanhoDaArvore[raizDeB]) {
                int guarda = raizDeA;

                raizDeA = raizDeB;
                raizDeB = guarda;
            }

            pai[raizDeB] = raizDeA;
            tamanhoDaArvore[raizDeA] += tamanhoDaArvore[raizDeB];

            grupos--;

            return true;
        }

        @Override
        public boolean mesmoGrupo(int a, int b, Contador contador) {
            return raizDe(a, contador) == raizDe(b, contador);
        }

        @Override
        public int quantosGrupos() {
            return grupos;
        }

        @Override
        public int tamanho() {
            return pai.length;
        }

        /**
         * A MAIOR ALTURA, calculada por varredura e sem recursao.
         *
         * <p>Ela e uma medida e nao parte do algoritmo: nenhuma das quatro versoes consulta
         * a altura para decidir nada. A de "por tamanho" olha o TAMANHO da arvore, que e
         * outra coisa, e e justamente por tamanho ser mais barato de manter que ele e o
         * que se usa.
         */
        @Override
        public int maiorAltura() {
            int[] altura = new int[pai.length];
            int maior = 0;

            for (int i = 0; i < pai.length; i++) {
                int quantos = 0;
                int andando = i;

                while (pai[andando] != andando) {
                    andando = pai[andando];
                    quantos++;
                }

                altura[i] = quantos;
                maior = Math.max(maior, quantos);
            }

            return maior;
        }

        /** A altura MEDIA, que e o que uma busca tipica paga. */
        public double alturaMedia() {
            long soma = 0;

            for (int i = 0; i < pai.length; i++) {
                int andando = i;

                while (pai[andando] != andando) {
                    andando = pai[andando];
                    soma++;
                }
            }

            return (double) soma / pai.length;
        }

        @Override
        public String nome() {
            return nome;
        }

        private void conferir(int elemento) {
            if (elemento < 0 || elemento >= pai.length)
                throw new IllegalArgumentException("elemento fora de 0 a " + (pai.length - 1)
                        + ": " + elemento);
        }
    }

    /** Sem truque nenhum: sobe ate a raiz e pendura a primeira na segunda. */
    static Conjuntos semTruque(int tamanho) {
        return new Arvore(tamanho, false, false, "sem truque");
    }

    /** So com compressao de caminho. */
    static Conjuntos soCompressao(int tamanho) {
        return new Arvore(tamanho, true, false, "so compressao");
    }

    /** So com uniao por tamanho. */
    static Conjuntos soPorTamanho(int tamanho) {
        return new Arvore(tamanho, false, true, "so por tamanho");
    }

    /** Os dois ligados. */
    static Conjuntos completo(int tamanho) {
        return new Arvore(tamanho, true, true, "os dois");
    }

    /** As cinco, na ordem em que as tabelas as mostram. */
    static Map<String, IntFunction<Conjuntos>> todas() {
        Map<String, IntFunction<Conjuntos>> mapa = new LinkedHashMap<>();

        mapa.put("por rotulo", PorRotulo::new);
        mapa.put("sem truque", Conjuntos::semTruque);
        mapa.put("so compressao", Conjuntos::soCompressao);
        mapa.put("so por tamanho", Conjuntos::soPorTamanho);
        mapa.put("os dois", Conjuntos::completo);

        return mapa;
    }

    /** As quatro de arvore, que sao as unicas que tem altura. */
    static Map<String, IntFunction<Conjuntos>> asDeArvore() {
        Map<String, IntFunction<Conjuntos>> mapa = todas();

        mapa.remove("por rotulo");

        return mapa;
    }
}
