package br.com.hat.hat_api.repository;

import br.com.hat.hat_api.model.Exame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExameRepository extends JpaRepository<Exame, Long> {
    @Query(value = """
                select
                    sl.reg,
                    sl.data,
                    sl.nsolicitante as medico,
                    sp.nome_exame as exame,
            
                    case tb.cod
                        when 1 then 'SUS'
                        when 2 then 'Particular'
                        else 'Convenio'
                    end as convenio,
            
                    case sl.atend
                        when 'E' then 'Externo'
                        when 'I' then 'Interno'
                        else 'Desconhecido'
                    end as tipo_atendimento,
            
                    case
                        when sl.ato = 14 then 'Hemodinamica'
                        when sl.ato = 25 then 'Tomografia'
                        when sl.ato = 16 then 'Radiologia'
                        when sl.ato = 24 then 'Ultrassonografia'
                    end as ato
            
                from silanexa sl
            
                left join sicadate sc on sl.id_sicadate = sc.id
            
                left join (
                    select codalf, ato, max(nome) as nome_exame
                    from sitabpro
                    group by codalf, ato
                ) sp on sp.codalf = sl.exame and sp.ato = sl.ato
            
                left join tbconven tb on tb.cod = sc.conv
            
                where sl.ato in (14, 25, 16, 24)
                  and sl.data between :dataini and :datafim;
            """, nativeQuery = true)
    List<Object[]> findExamesCardio(@Param("dataini") String dataini, @Param("datafim") String datafim);

}
