package br.com.hat.hat_api.spdata.repository;

import br.com.hat.hat_api.spdata.model.Hemodinamica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HemodinamicaRepository extends JpaRepository<Hemodinamica, Long> {

    @Query(value = """
            SELECT DISTINCT
                l.procto,
                c.data,
                c.reg,
                l.seq,
                l.cirur,
                c.out_tip_anes,
                l.id_cirur,
                c.sala,
                (SELECT esp FROM spespcir(c.atend, c.reg)) AS esp,
                (SELECT nome_esp FROM spespcir(c.atend, c.reg)) AS nome_esp,
                (SELECT nome FROM tbprofis tbProf
                    INNER JOIN tbcbopro tbCbo ON tbCbo.id_tbprofis = tbProf.id
                    WHERE tbCbo.id = l.id_cirur AND tbCbo.cod = l.cirur) AS nome_cir,
                (SELECT nome FROM tbanestesia WHERE cod = c.tipo_anes) AS anestesia,
                (SELECT nome FROM spnompro(
                    (SELECT conv FROM spconv(c.atend, c.reg)), l.procto, c.atend)) AS nome_pro,
                (SELECT nome FROM spdadpac(c.atend, c.reg,
                    (SELECT pac_uni FROM reparam))) AS nome,
                c.conv AS cod_conv,
                (SELECT FIRST 1 t.nome FROM tbconven t WHERE t.cod = c.conv) AS convenio,
                c.hora_i,
                c.hora_t,
                CASE
                    WHEN c.hora_t >= c.hora_i THEN
                        (
                            (CAST(SUBSTRING(c.hora_t FROM 1 FOR 2) AS INTEGER) * 60 +
                             CAST(SUBSTRING(c.hora_t FROM 3 FOR 2) AS INTEGER))
                            -
                            (CAST(SUBSTRING(c.hora_i FROM 1 FOR 2) AS INTEGER) * 60 +
                             CAST(SUBSTRING(c.hora_i FROM 3 FOR 2) AS INTEGER))
                        )
                    ELSE
                        (
                            ((CAST(SUBSTRING(c.hora_t FROM 1 FOR 2) AS INTEGER) * 60 +
                              CAST(SUBSTRING(c.hora_t FROM 3 FOR 2) AS INTEGER)) + 1440)
                            -
                            (CAST(SUBSTRING(c.hora_i FROM 1 FOR 2) AS INTEGER) * 60 +
                             CAST(SUBSTRING(c.hora_i FROM 3 FOR 2) AS INTEGER))
                        )
                END AS duracao_minutos,
                c.tipo_cir AS tipo_cirurgia,
                ug.nome AS urgencia,
                (SELECT nome FROM tbprofis tbProf
                    INNER JOIN tbcbopro tbCbo ON tbCbo.id_tbprofis = tbProf.id
                    WHERE tbCbo.id = l.id_anest AND tbCbo.cod = l.anest) AS anest
            FROM cccadcir c
            INNER JOIN cclancir l ON c.id = l.id_cccadcir
            INNER JOIN cctpurgencia ug ON ug.id = c.id_cctpurgencia
            LEFT OUTER JOIN ccturnos t ON (
                (c.hora_i BETWEEN t.hora_i AND t.hora_f)
                OR (t.hora_f < t.hora_i AND c.hora_i >= t.hora_i)
            )
            WHERE
                c.data BETWEEN :dataini AND :datafim
                AND l.cirur <> 0
                AND l.tipo = 'H'
                AND c.sala = 20
            ORDER BY c.data, c.hora_i, c.hora_t
            """, nativeQuery = true)
    List<Object[]> findHemodinamica(@Param("dataini") LocalDate dataini, @Param("datafim") LocalDate datafim);

    @Query(value = """
            SELECT
                sl.pront,
                sl.reg,
                sl.data,
                sl.nsolicitante AS medico_solicitante,
                sl.nexecutante AS medico_executante,
                sp.nome_exame AS exame,
                tb.nome AS convenio,
                CASE sl.atend
                    WHEN 'E' THEN 'Externo'
                    WHEN 'I' THEN 'Interno'
                    ELSE 'Desconhecido'
                END AS tipo_atendimento,
                CASE
                    WHEN sl.ato = 14 THEN 'Hemodinamica'
                END AS ato
            FROM silanexa sl
            LEFT JOIN sicadate sc ON sl.id_sicadate = sc.id
            LEFT JOIN (
                SELECT codalf, ato, MAX(nome) AS nome_exame
                FROM sitabpro
                GROUP BY codalf, ato
            ) sp ON sp.codalf = sl.exame AND sp.ato = sl.ato
            LEFT JOIN tbconven tb ON tb.cod = sc.conv
            WHERE sl.ato IN (14)
              AND sl.pend_sadt = 'F'
              AND sl.data BETWEEN :dataini AND :datafim
            ORDER BY sl.data, sl.pront
            """, nativeQuery = true)
    List<Object[]> findExamesHemodinamica(@Param("dataini") LocalDate dataini, @Param("datafim") LocalDate datafim);

    @Query(value = """
            SELECT
                c.sala_ant,
                 CASE\s
                    WHEN c.operacao = 'E' THEN 'Cancelamento'
                    WHEN c.operacao = 'R' THEN 'Remarcado'
                       ELSE c.operacao
                 END AS operacao,
                c.data_hora_ant,
                c.paciente,
                c.conv,
                c.justif,
                c.procto,
                c.crm,
                p2.nome AS nome_profissional,
                p.nome AS nome_pro,
                (SELECT co.nome FROM tbconven co WHERE co.cod = c.conv) AS conven
            FROM cchisage c
            INNER JOIN tbprocto p ON c.procto = p.cod_procedimento
            LEFT JOIN tbcbopro cbo ON cbo.cod = c.crm
            LEFT JOIN tbprofis p2 ON p2.id = cbo.id_tbprofis
            WHERE
                c.operacao = 'E'
                AND c.sala_ant = 20
                AND c.data_hora_ant BETWEEN :dataini AND :datafim
            GROUP BY
                c.sala_ant,\s
                c.operacao,\s
                c.data_hora_ant,\s
                c.paciente,
                c.conv,\s
                c.justif,\s
                c.procto,\s
                c.crm,\s
                p2.nome,
                p.nome,
                (SELECT co.nome FROM tbconven co WHERE co.cod = c.conv)
            ORDER BY c.data_hora_ant, c.sala_ant, c.crm
            """, nativeQuery = true)
    List<Object[]> findCancelamentosHemodinamica(@Param("dataini") LocalDateTime dataini, @Param("datafim") LocalDateTime datafim);

    @Query(value = """
            SELECT
                ccca.sala,
                ccca.data_hora,
                ccca.justif,
                ccca.pront,
                ccca.procto,
                ccca.id_tbsitage as situacao
            FROM ccagenda ccca
            WHERE ccca.sala = 20
              AND ccca.data_hora BETWEEN :dataini AND :datafim
            ORDER BY ccca.data_hora, ccca.sala
            """, nativeQuery = true)
    List<Object[]> findAgendaHemodinamica(@Param("dataini") LocalDateTime dataini, @Param("datafim") LocalDateTime datafim);

}