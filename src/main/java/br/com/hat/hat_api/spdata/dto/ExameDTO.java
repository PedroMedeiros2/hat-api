package br.com.hat.hat_api.spdata.dto;

import java.util.Date;

public record ExameDTO(
        Long reg,
        Date data,
        String medico,
        String exame,
        String convenio,
        String tipoAtendimento,
        String ato,
        /** Código do convênio (tbconven.cod); null = sem convênio */
        Integer codigoConvenio,
        /** Nome do convênio no cadastro (tbconven.nome) */
        String nomeConvenio,
        /** Data e hora do lançamento do exame (silanexa.data_hora_inclusao) */
        Date dataHora
) {}
