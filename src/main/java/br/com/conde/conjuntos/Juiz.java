package br.com.conde.conjuntos;

import java.util.List;

import br.com.conde.conjuntos.Ordens.Par;

/**
 * O JUIZ: o conjunto POR ROTULO.
 *
 * <p>Ele guarda o rotulo do grupo de cada elemento e, para juntar dois grupos, reescreve
 * todos os rotulos de um lado. Custa n por uniao e e obviamente correto: dois elementos
 * estao no mesmo grupo exatamente quando o rotulo deles e igual.
 *
 * <p>E por ser burro que ele julga. Ele nao tem arvore, entao nao pode ter arvore torta;
 * nao tem compressao, entao nao pode comprimir errado.
 *
 * <p>O que ele confere nao e so a resposta de cada pergunta: e a PARTICAO inteira, grupo
 * por grupo. Duas implementacoes podem concordar em todas as perguntas que alguem pensou
 * em fazer e discordar na particao, e a particao e o que a estrutura promete.
 */
public final class Juiz {

    private Juiz() {
    }

    /** O resultado de rodar uma lista de unioes e conferir tudo. */
    public record Veredito(int elementos, int unioes, int perguntas, int divergencias,
                           String primeiraDivergencia, int grupos, int maiorAltura) {

        public boolean bateu() {
            return divergencias == 0;
        }
    }

    /**
     * Roda as unioes nos dois e confere a PARTICAO inteira no fim.
     *
     * <p>A conferencia e de todos os pares de elementos, e nao de uma amostra: com n
     * elementos sao n vezes n menos um sobre dois perguntas, e e isso que pega uma
     * compressao que reaponta um no para a raiz errada.
     */
    public static Veredito julgar(Conjuntos emTeste, List<Par> unioes, Contador contador) {
        int n = emTeste.tamanho();
        Conjuntos espelho = new Conjuntos.PorRotulo(n);

        int divergencias = 0;
        String primeira = null;

        for (Par par : unioes) {
            boolean juntou = emTeste.une(par.a(), par.b(), contador);
            boolean juntouNoEspelho = espelho.une(par.a(), par.b(), contador);

            if (juntou != juntouNoEspelho) {
                divergencias++;

                if (primeira == null)
                    primeira = "une(" + par.a() + ", " + par.b() + "): " + juntou
                            + " contra " + juntouNoEspelho;
            }
        }

        int perguntas = 0;

        for (int a = 0; a < n; a++)
            for (int b = a + 1; b < n; b++) {
                perguntas++;

                boolean doTeste = emTeste.mesmoGrupo(a, b, contador);
                boolean doEspelho = espelho.mesmoGrupo(a, b, contador);

                if (doTeste != doEspelho) {
                    divergencias++;

                    if (primeira == null)
                        primeira = "mesmoGrupo(" + a + ", " + b + "): " + doTeste
                                + " contra " + doEspelho;
                }
            }

        if (emTeste.quantosGrupos() != espelho.quantosGrupos()) {
            divergencias++;

            if (primeira == null)
                primeira = "quantosGrupos(): " + emTeste.quantosGrupos()
                        + " contra " + espelho.quantosGrupos();
        }

        return new Veredito(n, unioes.size(), perguntas, divergencias, primeira,
                emTeste.quantosGrupos(), emTeste.maiorAltura());
    }

    /**
     * A RAIZ E ESTAVEL: dois elementos do mesmo grupo devolvem a MESMA raiz.
     *
     * <p>E uma propriedade que as perguntas de "mesmo grupo" ja cobrem, e esta separada
     * porque uma compressao errada pode deixar dois elementos do mesmo grupo com raizes
     * diferentes e, ainda assim, responder certo enquanto ninguem perguntar por aquele
     * par exato.
     */
    public static boolean asRaizesSaoEstaveis(Conjuntos conjuntos, Contador contador) {
        int n = conjuntos.tamanho();
        int[] raizDoGrupo = new int[n];

        java.util.Arrays.fill(raizDoGrupo, -1);

        Conjuntos espelho = new Conjuntos.PorRotulo(n);

        for (int i = 0; i < n; i++) {
            int rotulo = espelho.raizDe(i, contador);
            int raiz = conjuntos.raizDe(i, contador);

            if (raizDoGrupo[rotulo] == -1) raizDoGrupo[rotulo] = raiz;
            else if (raizDoGrupo[rotulo] != raiz) return false;
        }

        return true;
    }

    /** Roda as unioes e devolve a estrutura, sem conferir nada. */
    public static Conjuntos comAsUnioes(Conjuntos conjuntos, List<Par> unioes, Contador contador) {
        for (Par par : unioes) conjuntos.une(par.a(), par.b(), contador);

        return conjuntos;
    }

    /** Faz uma busca a partir de cada elemento, para medir o custo de uma busca tipica. */
    public static void buscarTodos(Conjuntos conjuntos, Contador contador) {
        for (int i = 0; i < conjuntos.tamanho(); i++) conjuntos.raizDe(i, contador);
    }
}
