package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Uma saída do hospital (alta ou óbito), para a lista caso a caso da Movimentação.
 * entrada/alta trazem a hora quando o SPData tem (horaent/horasai em HHMM); minutosInternado é
 * nulo quando falta alguma hora. motivoNome vem da TBMOTALT; obito = motivo de alta 41 a 45; mais24h = óbito com mais de 1.440 minutos.
 */
public record SaidaDTO(
        Long pront,
        Long reg,
        LocalDateTime entrada,
        LocalDateTime alta,
        String nome,
        LocalDate nascimento,
        String medicoAssistencial,
        String bloco,
        Integer codigoConvenio,
        String convenio,
        String tipoConvenio,
        String clinica,
        String carater,
        Integer codigoCarater,
        String grupoCarater,
        String motivo,
        String motivoGrupo,
        String motivoNome,
        boolean obito,
        Long minutosInternado,
        boolean mais24h,
        /** CID principal e procedimento da internação */
        String cidCodigo,
        String cidNome,
        String procedimentoCodigo,
        String procedimentoNome
) { }
