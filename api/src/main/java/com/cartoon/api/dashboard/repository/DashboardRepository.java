package com.cartoon.api.dashboard.repository;

import com.cartoon.api.dashboard.dto.projecao.ContagemMecanicoProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsAberturaProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.projecao.SomaPorOsProjecao;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DashboardRepository extends Repository<OrdemServico, Integer> {

    @Query("""
            select coalesce(sum(o.total), 0)
            from OrdemServico o
            where o.oficina.id = :oficinaId
              and o.statusServico = :status
            """)
    BigDecimal somarTotalPorStatus(@Param("oficinaId") Integer oficinaId,
                                   @Param("status") StatusServico status);

    @Query("""
            select new com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao(
                o.id, o.dataInicio, o.dataFinalizacao, o.total, m.id, m.nome)
            from OrdemServico o
            join o.mecanico m
            where o.oficina.id = :oficinaId
              and o.statusServico = com.cartoon.api.ordemServico.models.StatusServico.FINALIZADO
              and o.dataFinalizacao between :de and :ate
            """)
    List<OsFinalizadaProjecao> listarFinalizadas(@Param("oficinaId") Integer oficinaId,
                                         @Param("de") LocalDate de,
                                         @Param("ate") LocalDate ate);

    @Query("""
            select new com.cartoon.api.dashboard.dto.projecao.SomaPorOsProjecao(
                o.id, coalesce(sum(coalesce(i.subtotal, i.valorUnitario * i.quantidade)), 0))
            from ItemPeca i
            join i.ordemServico o
            where o.oficina.id = :oficinaId
              and o.statusServico = com.cartoon.api.ordemServico.models.StatusServico.FINALIZADO
              and o.dataFinalizacao between :de and :ate
            group by o.id
            """)
    List<SomaPorOsProjecao> somarPecasDasFinalizadas(@Param("oficinaId") Integer oficinaId,
                                             @Param("de") LocalDate de,
                                             @Param("ate") LocalDate ate);

    @Query("""
            select new com.cartoon.api.dashboard.dto.projecao.SomaPorOsProjecao(
                o.id, coalesce(sum(coalesce(i.subtotal, i.valorUnitario * i.quantidade)), 0))
            from ItemServico i
            join i.ordemServico o
            where o.oficina.id = :oficinaId
              and o.statusServico = com.cartoon.api.ordemServico.models.StatusServico.FINALIZADO
              and o.dataFinalizacao between :de and :ate
            group by o.id
            """)
    List<SomaPorOsProjecao> somarServicosDasFinalizadas(@Param("oficinaId") Integer oficinaId,
                                                @Param("de") LocalDate de,
                                                @Param("ate") LocalDate ate);

    @Query("""
            select new com.cartoon.api.dashboard.dto.projecao.OsAberturaProjecao(
                o.dataOrcamento, o.statusServico)
            from OrdemServico o
            where o.oficina.id = :oficinaId
              and o.dataOrcamento between :de and :ate
            """)
    List<OsAberturaProjecao> listarAbertas(@Param("oficinaId") Integer oficinaId,
                                   @Param("de") LocalDate de,
                                   @Param("ate") LocalDate ate);

    @Query("""
            select new com.cartoon.api.dashboard.dto.projecao.ContagemMecanicoProjecao(
                m.id, m.nome, count(o))
            from OrdemServico o
            join o.mecanico m
            where o.oficina.id = :oficinaId
              and o.statusServico = :status
            group by m.id, m.nome
            """)
    List<ContagemMecanicoProjecao> contarPorMecanico(@Param("oficinaId") Integer oficinaId,
                                             @Param("status") StatusServico status);
}
