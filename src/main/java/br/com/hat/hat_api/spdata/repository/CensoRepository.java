package br.com.hat.hat_api.spdata.repository;

import br.com.hat.hat_api.spdata.model.Censo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CensoRepository extends JpaRepository<Censo, Long> {
    @Query(value = """
            WITH RECURSIVE Datas (dia) AS (
                SELECT CAST(:dataini AS DATE)
                FROM RDB$DATABASE
            
                UNION ALL
            
                SELECT DATEADD(1 DAY TO d.dia)
                FROM Datas d
                WHERE d.dia < CAST(:datafim AS DATE)
            ),
            
            InternacoesFiltradas AS (
                SELECT
                    rc.reg,
                    ri.pront,
                    CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                    rc.acomod,
                    rc.entrada,
                    rc.saida,
                    ri.conv,
                    ri.espec,
                    ri.alta,
                    ri.motivo
                FROM rictrloc rc
                JOIN ricadint ri ON ri.reg = rc.reg
                WHERE
                    rc.entrada <= DATEADD(1 DAY TO CAST(:datafim AS DATE))
                    AND (
                        rc.saida = DATE '1899-12-30' OR rc.saida >= CAST(:dataini AS DATE)
                    )
                    AND rc.bloco = :bloco_selecionado
            )
            
            
            SELECT
                'Pernoite' AS tipo_evento,
                i.reg,
                i.pront,
                CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                i.acomod,
                tc.nome AS convenio,
                te.nome AS especialidade,
                d.dia AS data_referencia
            FROM Datas d
            JOIN InternacoesFiltradas i
                ON i.entrada <= d.dia
               AND (i.saida = DATE '1899-12-30' OR i.saida > d.dia)
            JOIN tbconven tc ON tc.cod = i.conv
            JOIN tbespec te ON te.cod = i.espec
            
            UNION ALL
            
            
            SELECT
                'Paciente Dia' AS tipo_evento,
                ri.reg,
                ri.pront,
                CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                ri.acomod,
                tc.nome,
                te.nome,
                CAST(ri.entrada AS DATE)
            FROM ricadint ri
            JOIN tbconven tc ON tc.cod = ri.conv
            JOIN tbespec te ON te.cod = ri.espec
            WHERE
                :incluir_mesmo_dia = 1
                AND ri.entrada >= CAST(:dataini AS DATE)
                AND ri.entrada < DATEADD(1 DAY TO CAST(:datafim AS DATE))
                AND CAST(ri.entrada AS DATE) = CAST(ri.alta AS DATE)
                AND ri.bloco = :bloco_selecionado
            
            UNION ALL
            
            SELECT
                'Altas' AS tipo_evento,
                ri.reg,
                ri.pront,
                CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                ri.acomod,
                tc.nome,
                te.nome,
                CAST(ri.alta AS DATE)
            FROM ricadint ri
            JOIN tbconven tc ON tc.cod = ri.conv
            JOIN tbespec te ON te.cod = ri.espec
            WHERE
                ri.alta >= CAST(:dataini AS DATE)
                AND ri.alta <= DATEADD(1 DAY TO CAST(:datafim AS DATE))
                AND ri.bloco = :bloco_selecionado
                AND ri.motivo NOT IN ('41','42','43','44','45')
            
            UNION ALL
            
            
            SELECT
                'Obitos' AS tipo_evento,
                ri.reg,
                ri.pront,
                CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                ri.acomod,
                tc.nome,
                te.nome,
                CAST(ri.alta AS DATE)
            FROM ricadint ri
            JOIN tbconven tc ON tc.cod = ri.conv
            JOIN tbespec te ON te.cod = ri.espec
            WHERE
                ri.alta >= CAST(:dataini AS DATE)
                AND ri.alta <= DATEADD(1 DAY TO CAST(:datafim AS DATE))
                AND ri.bloco = :bloco_selecionado
                AND ri.motivo IN ('41','42','43','44','45')
            
            
            UNION ALL
            
            select
                'Saida' AS tipo_evento,
                ri.reg,
                ri.pront,
                CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                ri.acomod,
                tc.nome AS convenio,
                te.nome AS especialidade,
                CAST(ri.alta AS DATE)
                from ritransf rf
            JOIN ricadint ri ON ri.reg = rf.reg
            JOIN tbconven tc ON tc.cod = ri.conv
            JOIN tbespec te ON te.cod = ri.espec
            where rf.data >= CAST(:dataini AS DATE)
            and rf.data <= CAST(:datafim AS DATE)
            and (rf.blocoo <> rf.blocod
                    or (rf.blocoo = rf.blocod and rf.acomodo <> rf.acomodd))
            and rf.blocoo = :bloco_selecionado
            
            UNION ALL
            
            select
                'Entrada' AS tipo_evento,
                ri.reg,
                ri.pront,
                CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                ri.acomod,
                tc.nome AS convenio,
                te.nome AS especialidade,
                CAST(ri.entrada AS DATE)
                from ritransf rf
            JOIN ricadint ri ON ri.reg = rf.reg
            JOIN tbconven tc ON tc.cod = ri.conv
            JOIN tbespec te ON te.cod = ri.espec
            where rf.data >= CAST(:dataini AS DATE)
            and rf.data <= CAST(:datafim AS DATE)
            and (rf.blocoo <> rf.blocod
                    or (rf.blocoo = rf.blocod and rf.acomodo <> rf.acomodd))
            and rf.blocod = :bloco_selecionado
            
            UNION ALL
            
            SELECT
                'Internacoes Diretas' AS tipo_evento,
                ri.reg,
                ri.pront,
                CAST(:bloco_selecionado AS VARCHAR(10)) as bloco,
                ri.acomod,
                tc.nome AS convenio,
                te.nome AS especialidade,
                CAST(ri.entrada AS DATE)
            FROM RICTRLOC rl
            JOIN RICADINT ri
                ON ri.REG = rl.REG
               AND ri.ENTRADA = rl.ENTRADA
               AND RIGHT('0000' || TRIM(ri.HORAENT), 4) = RIGHT('0000' || TRIM(rl.HORAENT), 4)
            JOIN tbconven tc ON tc.cod = ri.conv
            JOIN tbespec te ON te.cod = ri.espec
            WHERE rl.ENTRADA BETWEEN :dataini AND :datafim
            AND rl.bloco = :bloco_selecionado
            
            ORDER BY 5,2,1;
            """, nativeQuery = true)
    List<Object[]> findCenso(@Param("dataini") LocalDateTime dataini, @Param("datafim") LocalDateTime datafim, @Param("bloco_selecionado")String blocoSelecionado, @Param("incluir_mesmo_dia") Integer incluirMesmoDia);

    @Query(value = """
            SELECT ta.bloco, COUNT(*) AS leitos
                FROM rileitos ta WHERE ta.status <> 'I'
                GROUP BY ta.bloco;
            """, nativeQuery = true)
    List<Object[]> findBlocos();


}
