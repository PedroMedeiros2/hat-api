package br.com.hat.hat_api.spdata.repository;

import br.com.hat.hat_api.spdata.model.Leito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeitoRepository extends JpaRepository<Leito, Long> {

    @Query(value = """
            WITH total_leitos AS (
                SELECT
                    B.BLOCO,
                    B.NOME,
                    B.ORDEM,
                    COUNT(*) AS total_leitos
                FROM
                    RILEITOS L
                    INNER JOIN RIACOMOD A ON L.ACOMOD = A.ACOMOD
                    INNER JOIN RIBLOCOS B ON B.BLOCO = A.BLOCO AND B.BLOCO = L.BLOCO
                WHERE
                    B.BLOCO IN (:blocos)
                    AND L.STATUS <> 'I'
                GROUP BY B.BLOCO, B.NOME, B.ORDEM
            ),
            ocupacao AS (
                SELECT
                    B.BLOCO,
                    CASE
                        WHEN v.cod = 1 THEN 'SUS'
                        WHEN v.cod = 2 THEN 'Particular'
                        ELSE 'Convenio'
                    END AS convenio,
                    COUNT(*) AS leitos_ocupados
                FROM
                    RILEITOS L
                    INNER JOIN RIACOMOD A ON L.ACOMOD = A.ACOMOD
                    INNER JOIN RIBLOCOS B ON B.BLOCO = A.BLOCO AND B.BLOCO = L.BLOCO
                    INNER JOIN RICADINT CI ON CI.REG = L.REG
                    LEFT JOIN tbconven v ON v.cod = CI.CONV
                WHERE
                    B.BLOCO IN (:blocos)
                    AND L.STATUS <> 'I'
                    AND L.REG <> 0
                GROUP BY
                    B.BLOCO,
                    CASE
                        WHEN v.cod = 1 THEN 'SUS'
                        WHEN v.cod = 2 THEN 'Particular'
                        ELSE 'Convenio'
                    END
            ),
            ocupados_totais AS (
                SELECT
                    BLOCO,
                    SUM(leitos_ocupados) AS total_ocupados
                FROM ocupacao
                GROUP BY BLOCO
            )

            SELECT
                t.BLOCO,
                t.NOME,
                o.convenio,
                COALESCE(o.leitos_ocupados, 0) AS leitos_ocupados,
                t.total_leitos,
                t.total_leitos - COALESCE(ot.total_ocupados, 0) AS leitos_disponiveis
            FROM
                total_leitos t
                LEFT JOIN ocupacao o ON o.BLOCO = t.BLOCO
                LEFT JOIN ocupados_totais ot ON ot.BLOCO = t.BLOCO
            ORDER BY
                t.ORDEM, t.NOME, o.convenio
            """, nativeQuery = true)
    List<Object[]> getTaxaOcupacaoByBlocos(@Param("blocos") List<String> blocos);

    @Query(value = """
            SELECT B.BLOCO
            FROM RIBLOCOS B
            WHERE EXISTS (
                SELECT 1
                FROM RILEITOS L
                WHERE L.BLOCO = B.BLOCO
                  AND COALESCE(L.STATUS, '') NOT IN ('I')
            )
            ORDER BY B.ORDEM, B.NOME
            """, nativeQuery = true)
    List<String> findBlocosAtivos();


    /** Internações pela data de entrada. Por dia e convênio: [data, código do convênio, nome do convênio, quantidade]. */
    @Query(value = """
            SELECT
                CAST(i.entrada AS DATE) AS dia,
                i.conv,
                TRIM(MAX(cv.nome)) AS convenio,
                COUNT(*) AS qtd
            FROM ricadint i
            LEFT JOIN tbconven cv ON cv.cod = i.conv
            WHERE CAST(i.entrada AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
            GROUP BY CAST(i.entrada AS DATE), i.conv
            ORDER BY 1, 2
            """, nativeQuery = true)
    List<Object[]> findInternacoes(@Param("dataini") String dataini, @Param("datafim") String datafim);

    /** Altas (saídas vivas) pela data de alta: os óbitos (motivo 41 a 45) ficam de fora e são contados em findObitos. Por dia e convênio: [data, código do convênio, nome do convênio, quantidade]. */
    @Query(value = """
            SELECT
                CAST(i.alta AS DATE) AS dia,
                i.conv,
                TRIM(MAX(cv.nome)) AS convenio,
                COUNT(*) AS qtd
            FROM ricadint i
            LEFT JOIN tbconven cv ON cv.cod = i.conv
            WHERE i.alta IS NOT NULL
              AND CAST(i.alta AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
              AND (i.motivo IS NULL OR i.motivo NOT IN ('41','42','43','44','45'))
            GROUP BY CAST(i.alta AS DATE), i.conv
            ORDER BY 1, 2
            """, nativeQuery = true)
    List<Object[]> findAltas(@Param("dataini") String dataini, @Param("datafim") String datafim);

    /** Óbitos: alta com motivo 41 a 45. Por dia e convênio: [data, código do convênio, nome do convênio, quantidade]. */
    @Query(value = """
            SELECT
                CAST(i.alta AS DATE) AS dia,
                i.conv,
                TRIM(MAX(cv.nome)) AS convenio,
                COUNT(*) AS qtd
            FROM ricadint i
            LEFT JOIN tbconven cv ON cv.cod = i.conv
            WHERE i.alta IS NOT NULL
              AND CAST(i.alta AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
              AND i.motivo IN ('41','42','43','44','45')
            GROUP BY CAST(i.alta AS DATE), i.conv
            ORDER BY 1, 2
            """, nativeQuery = true)
    List<Object[]> findObitos(@Param("dataini") String dataini, @Param("datafim") String datafim);

    /** Óbitos com mais de 24 horas (1.440 minutos) de internação. Por dia e convênio: [data, código do convênio, nome do convênio, quantidade]. */
    @Query(value = """
            SELECT
                CAST(i.alta AS DATE) AS dia,
                i.conv,
                TRIM(MAX(cv.nome)) AS convenio,
                COUNT(*) AS qtd
            FROM ricadint i
            LEFT JOIN tbconven cv ON cv.cod = i.conv
            WHERE i.alta IS NOT NULL
              AND CAST(i.alta AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
              AND i.motivo IN ('41','42','43','44','45')
              AND DATEDIFF(
                    MINUTE,
                    CAST(i.entrada || ' ' || SUBSTRING(i.horaent FROM 1 FOR 2) || ':' || SUBSTRING(i.horaent FROM 3 FOR 2) AS TIMESTAMP),
                    CAST(i.alta || ' ' || SUBSTRING(i.horasai FROM 1 FOR 2) || ':' || SUBSTRING(i.horasai FROM 3 FOR 2) AS TIMESTAMP)
                  ) > 1440
            GROUP BY CAST(i.alta AS DATE), i.conv
            ORDER BY 1, 2
            """, nativeQuery = true)
    List<Object[]> findObitos24h(@Param("dataini") String dataini, @Param("datafim") String datafim);

    @Query(value = """
            SELECT
                i.pront,
                i.reg,
                i.entrada,
                r1.nome,
                p1.nome AS medico_assistencial,
                p2.nome AS medico_solicitante,
                i.bloco,
                c.nome AS convenio,
                cl.nome AS clinica,
                ti.nome AS internacao,
                r1.nasc AS nascimento,
                i.carint,
                cid.cod AS cid_cod,
                cid.nome AS cid_nome,
                i.proced,
                i.conv
            FROM ricadint i
            LEFT JOIN tbcid10 cid        ON cid.id = i.id_tbcid10_principal
            LEFT JOIN tbprofis p1        ON p1.cnpj_cpf = i.medass
            LEFT JOIN tbprofis p2        ON p2.cnpj_cpf = i.medsol
            LEFT JOIN ricadpac r1        ON r1.pront = i.pront
            LEFT JOIN tbconven c         ON c.cod = i.conv
            LEFT JOIN tbcarateratend ti  ON ti.id = i.carint
            LEFT JOIN tbclinica cl       ON cl.cod = i.clinica
            WHERE i.entrada BETWEEN :dataini AND :datafim
            ORDER BY i.entrada, r1.nome
            """, nativeQuery = true)
    List<Object[]> findInternacoesDetalhadas(@Param("dataini") LocalDateTime dataini,
                                             @Param("datafim") LocalDateTime datafim);

    /**
     * Saídas (altas e óbitos) caso a caso pela data de alta: as mesmas internações que formam as contagens
     * de Altas e Óbitos. As horas vêm como texto HHMM (horaent/horasai) e o tempo de internação é calculado
     * no serviço, para uma hora mal preenchida não derrubar a consulta.
     */
    @Query(value = """
            SELECT
                i.pront,
                i.reg,
                i.entrada,
                i.horaent,
                i.alta,
                i.horasai,
                r1.nome,
                r1.nasc,
                p1.nome AS medico_assistencial,
                i.bloco,
                i.conv,
                c.nome AS convenio,
                cl.nome AS clinica,
                ti.nome AS carater,
                i.motivo,
                mt.nome AS motivo_nome,
                i.carint,
                cid.cod AS cid_cod,
                cid.nome AS cid_nome,
                i.proced
            FROM ricadint i
            LEFT JOIN tbcid10 cid        ON cid.id = i.id_tbcid10_principal
            LEFT JOIN ricadpac r1        ON r1.pront = i.pront
            LEFT JOIN tbprofis p1        ON p1.cnpj_cpf = i.medass
            LEFT JOIN tbconven c         ON c.cod = i.conv
            LEFT JOIN tbclinica cl       ON cl.cod = i.clinica
            LEFT JOIN tbcarateratend ti  ON ti.id = i.carint
            LEFT JOIN tbmotalt mt        ON mt.cod = i.motivo
            WHERE i.alta IS NOT NULL
              AND CAST(i.alta AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
            ORDER BY i.alta, i.horasai, i.reg
            """, nativeQuery = true)
    List<Object[]> findSaidas(@Param("dataini") String dataini, @Param("datafim") String datafim);

    /** Ficha de uma internação pelo registro (sem dados pessoais do paciente). */
    @Query(value = """
            SELECT
                i.reg,
                i.pront,
                i.entrada,
                i.horaent,
                i.alta,
                i.horasai,
                i.altaprev,
                i.bloco,
                b.nome AS bloco_nome,
                i.leito,
                c1.cod AS cid1_cod,
                c1.nome AS cid1_nome,
                c2.cod AS cid2_cod,
                c2.nome AS cid2_nome,
                i.proced,
                -- procedimento da internação: SUS na tabela de procedimentos SUS (TBPROSUS.COD),
                -- convênio e particular na TBPROCTO; uma serve de reserva para a outra
                CASE WHEN i.conv = 1 THEN
                    COALESCE((SELECT MIN(ps.nome) FROM tbprosus ps WHERE ps.cod = i.proced),
                             (SELECT MIN(p.nome) FROM tbprocto p WHERE p.cod_procedimento = CAST(i.proced AS VARCHAR(20))))
                ELSE
                    COALESCE((SELECT MIN(p.nome) FROM tbprocto p WHERE p.cod_procedimento = CAST(i.proced AS VARCHAR(20))),
                             (SELECT MIN(ps.nome) FROM tbprosus ps WHERE ps.cod = i.proced))
                END AS proced_nome,
                i.motivo,
                mt.nome AS motivo_nome,
                pac.sexo
            FROM ricadint i
            LEFT JOIN ricadpac pac ON pac.pront = i.pront
            LEFT JOIN riblocos b  ON b.bloco = i.bloco
            LEFT JOIN tbcid10 c1  ON c1.id = i.id_tbcid10_principal
            LEFT JOIN tbcid10 c2  ON c2.id = i.id_tbcid10_secundario
            LEFT JOIN tbmotalt mt ON mt.cod = i.motivo
            WHERE i.reg = :reg
            """, nativeQuery = true)
    List<Object[]> findFicha(@Param("reg") Long reg);


    /** Todos os procedimentos SUS (TBPROSUS). [cod, nome] */
    @Query(value = "SELECT ps.cod, ps.nome FROM tbprosus ps", nativeQuery = true)
    List<Object[]> findTodosNomesProcedimentosSus();

    /** Todos os procedimentos da tabela geral (TBPROCTO), um nome por código. [cod, nome] */
    @Query(value = "SELECT p.cod_procedimento, MIN(p.nome) FROM tbprocto p GROUP BY p.cod_procedimento", nativeQuery = true)
    List<Object[]> findTodosNomesProcedimentos();
}
