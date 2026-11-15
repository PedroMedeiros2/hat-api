package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.repository.LeitoRepository;
import br.com.hat.hat_api.spdata.util.Texto;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Nomes dos procedimentos, guardados em memória. A TBPROCTO tem ~674 mil linhas e nenhum índice só no
 * código: buscar os nomes de uma lista levava ~10 s por consulta. Carregar todos (~33 mil códigos) leva
 * ~3 s; fica guardado e é recarregado a cada 12 horas. A TBPROSUS (procedimentos SUS) vem junto.
 */
@Component
public class CatalogoProcedimentos {
    private static final Duration VALIDADE = Duration.ofHours(12);

    private final LeitoRepository repositorio;
    private volatile Map<String, String> sus = Map.of();
    private volatile Map<String, String> geral = Map.of();
    private volatile Instant carregadoEm = Instant.EPOCH;

    public CatalogoProcedimentos(LeitoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Carrega em segundo plano quando a API sobe, para a primeira tela não esperar; se o SPData estiver fora, tenta no primeiro uso. */
    @EventListener(ApplicationReadyEvent.class)
    public void aoSubir() {
        Thread t = new Thread(() -> {
            try { garantirCarregado(); } catch (RuntimeException e) { /* tenta de novo no primeiro uso */ }
        }, "catalogo-procedimentos");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Nome do procedimento: SUS (convênio 1) na TBPROSUS, convênio e particular na TBPROCTO;
     * uma serve de reserva para a outra. Código vazio ou "0" = sem procedimento.
     */
    public String nome(String codigo, boolean ehSus) {
        if (codigo == null || codigo.isBlank() || "0".equals(codigo)) return null;
        garantirCarregado();
        String primeiro = ehSus ? sus.get(codigo) : geral.get(codigo);
        return primeiro != null ? primeiro : (ehSus ? geral.get(codigo) : sus.get(codigo));
    }

    private void garantirCarregado() {
        if (Duration.between(carregadoEm, Instant.now()).compareTo(VALIDADE) < 0) return;
        synchronized (this) {
            if (Duration.between(carregadoEm, Instant.now()).compareTo(VALIDADE) < 0) return;
            Map<String, String> s = new HashMap<>(), g = new HashMap<>();
            for (Object[] x : repositorio.findTodosNomesProcedimentosSus()) s.put(Texto.limpo(x[0]), Texto.limpo(x[1]));
            for (Object[] x : repositorio.findTodosNomesProcedimentos()) g.put(Texto.limpo(x[0]), Texto.limpo(x[1]));
            sus = s;
            geral = g;
            carregadoEm = Instant.now();
        }
    }
}
