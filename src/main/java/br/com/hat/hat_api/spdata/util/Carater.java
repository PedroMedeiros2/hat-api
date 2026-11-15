package br.com.hat.hat_api.spdata.util;

/**
 * Caráter do atendimento da internação (ricadint.carint → TBCARATERATEND). Para os indicadores:
 * 1 = Eletiva; 2 a 6 = Urgência (urgência e os acidentes/lesões da tabela do SUS); 7 ou vazio = Não informado.
 */
public final class Carater {
    private Carater() { }

    public static String grupo(Integer codigo) {
        if (codigo == null) return "Não informado";
        if (codigo == 1) return "Eletiva";
        if (codigo >= 2 && codigo <= 6) return "Urgência";
        return "Não informado";
    }

    /** ricadint.carint é CHAR(1) no SPData ('1', '2'...); aceita número também. Inválido = null. */
    public static Integer codigo(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Number n) return n.intValue();
        String s = String.valueOf(valor).trim();
        return s.matches("\\d+") ? Integer.valueOf(s) : null;
    }
}
