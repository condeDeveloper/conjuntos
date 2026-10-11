package br.com.conde.conjuntos;

/**
 * O CONTADOR de SALTOS DE PONTEIRO, que e a unica medida que vale aqui.
 *
 * <p>Salto, nunca segundo. Segundo muda com a maquina, com o que mais esta rodando e com
 * o humor do coletor de lixo, e a integracao continua roda em tres sistemas.
 *
 * <p>O salto e a medida CERTA e nao so a conveniente: a conta inteira de conjuntos
 * disjuntos e subir de pai em pai ate a raiz, e o que os truques fazem e encurtar esse
 * caminho.
 */
public final class Contador {

    private long buscas;
    private long saltos;
    private long compressoes;

    public void buscou() {
        buscas++;
    }

    public void saltou() {
        saltos++;
    }

    public void saltou(long quantos) {
        saltos += quantos;
    }

    /** Um ponteiro reapontado para a raiz, que e o trabalho EXTRA da compressao. */
    public void comprimiu() {
        compressoes++;
    }

    public long buscas() {
        return buscas;
    }

    public long saltos() {
        return saltos;
    }

    public long compressoes() {
        return compressoes;
    }

    public double saltosPorBusca() {
        return buscas == 0 ? 0 : (double) saltos / buscas;
    }

    public double compressoesPorBusca() {
        return buscas == 0 ? 0 : (double) compressoes / buscas;
    }

    public void zerar() {
        buscas = 0;
        saltos = 0;
        compressoes = 0;
    }

    @Override
    public String toString() {
        return buscas + " buscas, " + saltos + " saltos, " + compressoes + " compressoes";
    }
}
