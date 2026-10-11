package br.com.conde.conjuntos;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao
 * quebra quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("altura") || comando.equals("tudo")) altura();
        if (comando.equals("custo") || comando.equals("tudo")) custo();
        if (comando.equals("ordem") || comando.equals("tudo")) ordem();
        if (comando.equals("ganho") || comando.equals("tudo")) ganho();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("TODA UNIAO BATE COM O JUIZ DE ROTULOS, conferindo a particao inteira.");
        System.out.println();

        var linhas = Medidas.aCorrecao();
        long erradas = linhas.stream().filter(l -> l.divergencias() > 0).count();

        System.out.printf("  %-18s %-18s %12s %10s %14s %14s %10s%n",
                "ordem", "versao", "elementos", "unioes", "perguntas", "divergencias", "grupos");

        for (var l : linhas)
            System.out.printf("  %-18s %-18s %12d %10d %14d %14d %10d%n",
                    l.ordem(), l.versao(), l.elementos(), l.unioes(), l.perguntas(),
                    l.divergencias(), l.grupos());

        System.out.println();
        System.out.printf("  %d combinacoes, %d com divergencia.%n", linhas.size(), erradas);
        System.out.println();
    }

    private static void altura() {
        System.out.println();
        System.out.println("A ALTURA que cada versao deixa a arvore ficar, com 20.000 elementos.");
        System.out.println();
        System.out.printf("  %-18s %-18s %16s %18s %24s%n",
                "ordem", "versao", "maior altura", "altura media", "sobre o logaritmo de n");

        for (var l : Medidas.aAltura())
            System.out.printf(Locale.ROOT, "  %-18s %-18s %16d %18.2f %24.1f%n",
                    l.ordem(), l.versao(), l.maiorAltura(), l.alturaMedia(), l.sobreOLogaritmo());

        System.out.println();
    }

    private static void custo() {
        System.out.println();
        System.out.println("O CUSTO DE UMA BUSCA em saltos, com " + Medidas.ELEMENTOS + " elementos.");
        System.out.println();
        System.out.printf("  %-18s %-18s %20s %24s %22s%n",
                "ordem", "versao", "saltos por busca", "compressoes por busca", "saltos nas unioes");

        for (var l : Medidas.oCusto())
            System.out.printf(Locale.ROOT, "  %-18s %-18s %20.2f %24.2f %22d%n",
                    l.ordem(), l.versao(), l.saltosPorBusca(), l.compressoesPorBusca(),
                    l.saltosNasUnioes());

        System.out.println();
    }

    private static void ordem() {
        System.out.println();
        System.out.println("A ORDEM QUE FAZ A ARVORE DEGENERAR: unir 1 com 0, 2 com 1, 3 com 2.");
        System.out.println();
        System.out.printf("  %12s %-18s %16s %20s %22s %20s%n",
                "elementos", "versao", "maior altura", "saltos por busca", "saltos nas unioes",
                "altura sobre n");

        for (var l : Medidas.aOrdem())
            System.out.printf(Locale.ROOT, "  %12d %-18s %16d %20.2f %22d %20.4f%n",
                    l.elementos(), l.versao(), l.maiorAltura(), l.saltosPorBusca(),
                    l.saltosNasUnioes(), l.sobreOTamanho());

        System.out.println();
    }

    private static void ganho() {
        System.out.println();
        System.out.println("O QUE CADA TRUQUE SOZINHO JA RESOLVE, com " + Medidas.ELEMENTOS + " elementos.");
        System.out.println();
        System.out.printf("  %-18s %28s %26s %22s %26s%n",
                "versao", "saltos por busca na corrente", "saltos por busca no sorteio",
                "altura na corrente", "quantas vezes os dois juntos");

        for (var l : Medidas.oGanho())
            System.out.printf(Locale.ROOT, "  %-18s %28.2f %26.2f %22d %26.1f%n",
                    l.versao(), l.saltosPorBuscaNaCorrente(), l.saltosPorBuscaNoSorteio(),
                    l.alturaNaCorrente(), l.quantasVezesOCompleto());

        System.out.println();
    }
}
