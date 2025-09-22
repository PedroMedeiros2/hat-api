// src/main/java/br/com/hat/hat_api/repository/LeitoRepository.java
package br.com.hat.hat_api.repository;

import br.com.hat.hat_api.model.Leito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeitoRepository extends JpaRepository<Leito, Long> {

    @Query(value = """
        WITH ocupacao AS (
            SELECT 
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
                CASE
                    WHEN v.cod = 1 THEN 'SUS'
                    WHEN v.cod = 2 THEN 'Particular'
                    ELSE 'Convenio'
                END
        ),
        total_leitos AS (
            SELECT 
                COUNT(*) AS total_leitos
            FROM 
                RILEITOS L
                INNER JOIN RIACOMOD A ON L.ACOMOD = A.ACOMOD
                INNER JOIN RIBLOCOS B ON B.BLOCO = A.BLOCO AND B.BLOCO = L.BLOCO
            WHERE 
                B.BLOCO IN (:blocos)
                AND L.STATUS <> 'I'
        ),
        ocupados_totais AS (
            SELECT 
                SUM(leitos_ocupados) AS total_ocupados
            FROM ocupacao
        )
        
        SELECT 
            o.convenio,
            o.leitos_ocupados,
            t.total_leitos,
            t.total_leitos - ot.total_ocupados AS leitos_disponiveis
        FROM 
            ocupacao o
            CROSS JOIN total_leitos t
            CROSS JOIN ocupados_totais ot
        ORDER BY 
            o.convenio
        """, nativeQuery = true)
    List<Object[]> getTaxaOcupacaoByBlocos(@Param("blocos") List<String> blocos);


    @Query(value = """
        WITH tipos AS (
            SELECT 'SUS' AS tipo_convenio FROM RDB$DATABASE
            UNION ALL
            SELECT 'Particular' FROM RDB$DATABASE
            UNION ALL
            SELECT 'Convenio' FROM RDB$DATABASE
        ),
        internacoes AS (
            SELECT CASE WHEN i.conv = 1 THEN 'SUS'
                        WHEN i.conv = 2 THEN 'Particular'
                        ELSE 'Convenio' END AS tipo_convenio,
                   COUNT(*) AS qtd_internacoes
            FROM ricadint i
            WHERE CAST(i.entrada AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
            GROUP BY 1
        ),
        altas AS (
            SELECT CASE WHEN i.conv = 1 THEN 'SUS'
                        WHEN i.conv = 2 THEN 'Particular'
                        ELSE 'Convenio' END AS tipo_convenio,
                   COUNT(*) AS qtd_altas
            FROM ricadint i
            WHERE i.alta IS NOT NULL
              AND CAST(i.alta AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
            GROUP BY 1
        ),
        obitos AS (
            SELECT CASE WHEN i.conv = 1 THEN 'SUS'
                        WHEN i.conv = 2 THEN 'Particular'
                        ELSE 'Convenio' END AS tipo_convenio,
                   COUNT(*) AS qtd_obitos
            FROM ricadint i
            WHERE i.alta IS NOT NULL
              AND CAST(i.alta AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
              AND i.motivo IN ('41','42','43','44','45')
            GROUP BY 1
        ),
        obitos_24h AS (
            SELECT CASE WHEN i.conv = 1 THEN 'SUS'
                        WHEN i.conv = 2 THEN 'Particular'
                        ELSE 'Convenio' END AS tipo_convenio,
                   COUNT(*) AS qtd_obitos_24h
            FROM ricadint i
            WHERE i.alta IS NOT NULL
              AND CAST(i.alta AS DATE) BETWEEN CAST(:dataini AS DATE) AND CAST(:datafim AS DATE)
              AND i.motivo IN ('41','42','43','44','45')
              AND DATEDIFF(MINUTE,
                    CAST(i.entrada || ' ' || 
                         SUBSTRING(i.horaent FROM 1 FOR 2) || ':' || 
                         SUBSTRING(i.horaent FROM 3 FOR 2) AS TIMESTAMP),
                    CAST(i.alta || ' ' || 
                         SUBSTRING(i.horasai FROM 1 FOR 2) || ':' || 
                         SUBSTRING(i.horasai FROM 3 FOR 2) AS TIMESTAMP)
                  ) > 1440
            GROUP BY 1
        ),
        base AS (
            SELECT t.tipo_convenio,
                   COALESCE(i.qtd_internacoes, 0)   AS qtd_internacoes,
                   COALESCE(a.qtd_altas, 0)         AS qtd_altas,
                   COALESCE(o.qtd_obitos, 0)        AS qtd_obitos,
                   COALESCE(o24.qtd_obitos_24h, 0)  AS qtd_obitos_24h
            FROM tipos t
            LEFT JOIN internacoes i  ON t.tipo_convenio = i.tipo_convenio
            LEFT JOIN altas a        ON t.tipo_convenio = a.tipo_convenio
            LEFT JOIN obitos o       ON t.tipo_convenio = o.tipo_convenio
            LEFT JOIN obitos_24h o24 ON t.tipo_convenio = o24.tipo_convenio
        ),
        resultado AS (
            SELECT * FROM base
            UNION ALL
            SELECT 'TOTAL',
                   SUM(qtd_internacoes),
                   SUM(qtd_altas),
                   SUM(qtd_obitos),
                   SUM(qtd_obitos_24h)
            FROM base
        )
        SELECT tipo_convenio,
               qtd_internacoes,
               qtd_altas,
               qtd_obitos,
               qtd_obitos_24h
        FROM resultado
        ORDER BY 1
        """, nativeQuery = true)
    List<Object[]> findMovimentacao(@Param("dataini") String dataini, @Param("datafim") String datafim);
}
