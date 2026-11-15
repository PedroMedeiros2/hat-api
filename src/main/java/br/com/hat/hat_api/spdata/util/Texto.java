package br.com.hat.hat_api.spdata.util;

/**
 * Texto vindo do SPData (Firebird). Colunas CHAR e o resultado de um CASE com textos de tamanhos
 * diferentes voltam completados com espaços até o maior ("SUS       "); sem limpar, a tela não
 * reconhece o valor (ex.: SUS caía em "Convenio" na Movimentação).
 */
public final class Texto {
    private Texto() { }

    /** O texto sem espaços nas pontas; null continua null. */
    public static String limpo(Object valor) {
        return valor == null ? null : String.valueOf(valor).trim();
    }
}
