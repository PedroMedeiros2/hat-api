package br.com.hat.hat_api.internados;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Tela Pacientes internados (HAT0013): o que a API devolve e o que a tela manda registrar. */
public final class Internados {

    private Internados() {
    }

    /** Um paciente internado agora. Nome, nascimento e filiação são dado pessoal sensível (LGPD). */
    public record Paciente(
            Long reg,
            Long pront,
            String bloco,
            /** quarto (RILEITOS.ACOMOD), ex.: 100, BOX_A */
            String quarto,
            String leito,
            LocalDateTime entrada,
            String nome,
            LocalDate nascimento,
            String sexo,
            String mae,
            String pai,
            String medicoAssistente,
            String convenio,
            String clinica) {
    }

    /** Contagem de um bloco: internados agora e leitos ativos. */
    public record Bloco(String bloco, int internados, int leitos) {
    }

    /** Resposta da consulta: momento da leitura, blocos (na ordem do SPData) e pacientes (por nome). */
    public record Consulta(LocalDateTime atualizadoEm, List<Bloco> blocos, List<Paciente> pacientes) {
    }

    /**
     * O que a tela registra: BUSCA (texto pesquisado, campo, bloco e quantos achou) ou FICHA (registro
     * da internação aberta). Abrir a lista é registrado pela própria consulta (LISTA).
     */
    public record Registro(String acao, String busca, String campo, String bloco, Long reg, Integer resultados) {
    }

    /** Uma linha do registro de consultas. */
    public record Linha(LocalDateTime quando, String usuario, String acao, String busca, String campo, String bloco,
                        Long reg, Integer resultados) {
    }
}
