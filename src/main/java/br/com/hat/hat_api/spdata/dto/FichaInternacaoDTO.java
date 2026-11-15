package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ficha de uma internação (pelo registro), com o que não cabe na lista: CID, procedimento, leito,
 * alta prevista. Sem nome do paciente.
 */
public record FichaInternacaoDTO(
        Long reg,
        Long pront,
        LocalDateTime entrada,
        /** nula enquanto o paciente está internado */
        LocalDateTime alta,
        LocalDate altaPrevista,
        String blocoCodigo,
        String blocoNome,
        String leito,
        String cidPrincipalCodigo,
        String cidPrincipalNome,
        String cidSecundarioCodigo,
        String cidSecundarioNome,
        String procedimentoCodigo,
        String procedimentoNome,
        String motivo,
        String motivoNome,
        /** F ou M (cadastro do paciente) */
        String sexo
) { }
