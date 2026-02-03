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
                    CAST(aa.data_hora_entrada AS DATE) AS data_entrada,
                    aa.tp_atendimento,
                    tu.nome AS unidade,
                    te.nome AS especialidade,
                    CASE\s
                        WHEN aa.atendimento_retorno = 'S' THEN 'Sim'
                        ELSE 'Não'
                    END AS retorno,
                    tc.nome AS convenio
                FROM atcabecatend aa
                JOIN tbunidad tu ON tu.cod = aa.id_tbunidad
                JOIN tbespec te ON te.cod = aa.id_tbespec
                JOIN tbconven tc ON tc.cod = aa.id_tbconven
                WHERE aa.id_tbunidad IN (1, 3, 4)
                  AND aa.data_hora_entrada BETWEEN :dataini AND :datafim;
            """, nativeQuery = true)
    List<Object[]> findAtendimentos(@Param("dataini") LocalDateTime dataini, @Param("datafim") LocalDateTime datafim);
}
