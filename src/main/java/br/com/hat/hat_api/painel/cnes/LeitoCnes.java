package br.com.hat.hat_api.painel.cnes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Um leito do cadastro CNES do hospital (mapeamento SMS, Núcleo Interno de Regulação).
 * perfil: SUS = leito do SUS; CONV/PART = leito de convênio ou particular (saúde suplementar).
 * quarto e leito seguem o cadastro (ex.: Q:100 / L:1; na UTI "BOX" / 01). Leito inativo não entra no total.
 */
public record LeitoCnes(
        Long id,
        String bloco,
        String quarto,
        String leito,
        String perfil,
        String especialidade,
        String observacao,
        int ordem,
        boolean ativo,
        LocalDateTime atualizadoEm,
        String atualizadoPor,
        /** tipo próprio do leito (código de TipoLeito); nulo = segue o tipo do bloco */
        String tipo
) {
    /** O que a tela manda para criar ou mudar um leito. */
    public record Dados(String bloco, String quarto, String leito, String perfil, String especialidade, String observacao,
                        Integer ordem, Boolean ativo, String tipo) { }

    /**
     * Tipo de leito, definido pela gestão: diz como o leito entra na taxa de ocupação.
     * contaNaTaxa = entra no total de leitos e nos ocupados da taxa (ex.: Fixo);
     * aParte = fora da taxa, mas mostrado separado (ex.: Provisório, PS e PA). Sem os dois = não conta (ex.: Virtual).
     */
    public record TipoLeito(String codigo, String nome, boolean contaNaTaxa, boolean aParte, String descricao, int ordem) { }

    public record DadosTipo(String nome, Boolean contaNaTaxa, Boolean aParte, String descricao, Integer ordem) { }

    /** Tipo padrão de um bloco (os leitos do bloco seguem este tipo, salvo o tipo próprio do leito). */
    public record BlocoTipo(String bloco, String tipo) { }

    /** Tudo o que as telas precisam: leitos, tipos e o tipo de cada bloco. */
    public record Cadastro(boolean editavel, List<LeitoCnes> leitos, List<TipoLeito> tipos, List<BlocoTipo> blocos, String tipoPadrao) { }

    /** Uma mudança no cadastro: quem, quando, o quê, de e para. */
    public record Mudanca(Long id, LocalDateTime quando, String usuario, String acao, Long leitoId, String bloco,
                          String antes, String depois) { }
}
