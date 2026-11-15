package br.com.hat.hat_api.spdata.util;

import java.util.Set;

/**
 * Motivo de saída da internação (ricadint.motivo). O nome vem da tabela TBMOTALT do SPData; ela é
 * organizada em grupos pela dezena do código (os títulos 10, 20, 30... têm status T).
 */
public final class MotivoSaida {
    private MotivoSaida() { }

    /** Óbitos: os mesmos códigos usados na contagem da Movimentação. */
    public static final Set<String> OBITOS = Set.of("41", "42", "43", "44", "45");

    public static boolean obito(String codigo) {
        return codigo != null && OBITOS.contains(codigo.trim());
    }

    /** Grupo do motivo pela dezena do código (como na TBMOTALT). */
    public static String grupo(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        String c = codigo.trim();
        if (c.length() == 1) return "Alta";
        return switch (c.charAt(0)) {
            case '1' -> "Alta";
            case '2' -> "Permanência";
            case '3' -> "Transferência";
            case '4' -> "Óbito";
            case '6' -> "Parto";
            default -> "Outros motivos";
        };
    }
}
