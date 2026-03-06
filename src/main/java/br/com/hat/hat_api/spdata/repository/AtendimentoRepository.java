package br.com.hat.hat_api.spdata.repository;

import br.com.hat.hat_api.spdata.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {
    @Query(value = """
                SELECT
                            aa.cod_atendimento,
                            rp.pront,
                            aa.data_hora_entrada,
                            rp.nome,
                            tu.nome AS unidade,
                            te.nome AS especialidade,
                            CASE
                                WHEN aa.tp_atendimento = 'E'
                                 AND EXISTS (
                                     SELECT 1
                                     FROM recadate rt
                                     WHERE rt.reg = aa.cod_atendimento
                                       AND rt.pext = 'S'
                                 )
                                THEN 'Sim'
                                ELSE 'Não'
                            END AS consulta,
                            CASE
                                WHEN aa.atendimento_retorno = 'S' THEN 'Sim'
                                ELSE 'Não'
                            END AS retorno,
                            tc.nome AS convenio,
                            CASE
                                WHEN aa.tp_atendimento = 'E'
                                 AND EXISTS (
                                     SELECT 1
                                     FROM atcabecatend aa2
                                     JOIN ricadpac rp2 ON rp2.id = aa2.id_ricadpac
                                     WHERE rp2.pront = rp.pront
                                       AND aa2.tp_atendimento = 'I'
                                       AND aa2.data_hora_entrada
                                           BETWEEN aa.data_hora_entrada
                                           AND aa.data_hora_entrada + 1
                                 )
                                THEN 'Sim'
                                ELSE 'Não'
                            END AS converteu_internacao,
                            CASE
                                WHEN aa.tp_atendimento = 'E'
                                 AND EXISTS (
                                     SELECT 1
                                     FROM recadate rt
                                     WHERE rt.reg = aa.cod_atendimento
                                       AND rt.pext = 'S'
                                 )
                                 AND EXISTS (
                                     SELECT 1
                                     FROM silanexa sl
                                     WHERE sl.pront = rp.pront
                                       AND sl.data_hora_inclusao > aa.data_hora_entrada
                                       AND sl.data_hora_inclusao
                                           BETWEEN aa.data_hora_entrada
                                           AND aa.data_hora_entrada + 1
                                 )
                                THEN 'Sim'
                                ELSE 'Não'
                            END AS converteu_exame
                        FROM atcabecatend aa
                        JOIN ricadpac rp ON rp.id = aa.id_ricadpac
                        JOIN tbunidad tu ON tu.cod = aa.id_tbunidad
                        JOIN tbespec te ON te.cod = aa.id_tbespec
                        JOIN tbconven tc ON tc.cod = aa.id_tbconven
                        WHERE
                            aa.id_tbunidad NOT IN (11)
                            AND aa.data_hora_entrada BETWEEN :dataini AND :datafim
                            AND aa.ativo = 'T'
                            AND aa.tp_atendimento = 'E'
                        ORDER BY rp.pront, aa.data_hora_entrada
            """, nativeQuery = true)
    List<Object[]> findAtendimentos(@Param("dataini") LocalDateTime dataini, @Param("datafim") LocalDateTime datafim);
}
