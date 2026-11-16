package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Um leito ativo agora (Taxa de ocupação, HAT0007/HAT0008): cada linha da RILEITOS sem status I dos
 * blocos pedidos, a mesma base da contagem por bloco. Leito livre: reg nulo e os campos do paciente nulos.
 * Traz o que a lista e a ficha mostram, para a tela não precisar da ficha da Movimentação.
 * entrada tem a hora quando o SPData tem (horaent).
 */
public record LeitoOcupacaoDTO(
        String bloco,
        /** quarto (RILEITOS.ACOMOD), ex.: 100, BOX_A, PS */
        String quarto,
        /** leito no quarto (RILEITOS.LEITO), ex.: 001 */
        String leito,
        /** A = ativo, P = provisório (PS e PA) */
        String status,
        Long reg,
        Long pront,
        LocalDateTime entrada,
        LocalDate altaPrevista,
        String nome,
        LocalDate nascimento,
        /** F ou M (cadastro do paciente) */
        String sexo,
        String medicoAssistencial,
        String medicoSolicitante,
        Integer codigoConvenio,
        String convenio,
        /** SUS, Particular ou Convenio (LeitoService.tipoDoConvenio) */
        String tipoConvenio,
        String clinica,
        String carater,
        Integer codigoCarater,
        String grupoCarater,
        String cidPrincipalCodigo,
        String cidPrincipalNome,
        String cidSecundarioCodigo,
        String cidSecundarioNome,
        String procedimentoCodigo,
        String procedimentoNome
) { }
