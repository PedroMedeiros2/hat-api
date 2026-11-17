package br.com.hat.hat_api.painel.ocupacao;

import br.com.hat.hat_api.painel.cnes.CnesService;
import br.com.hat.hat_api.spdata.repository.LeitoRepository;
import br.com.hat.hat_api.spdata.service.BlocoService;
import br.com.hat.hat_api.spdata.service.LeitoService;
import br.com.hat.hat_api.spdata.util.Texto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

/**
 * Rotina da foto diária da ocupação (histórico do cartão "Últimos 7 dias" da Taxa de ocupação).
 *
 * <ul>
 *   <li>Uma foto por dia, no horário de {@code hat.ocupacao.foto.hora} (padrão 00:05, fuso America/Sao_Paulo).
 *       Horário antes do meio-dia = censo do dia anterior fechado (a foto das 00:05 do dia 18 é do dia 17).</li>
 *   <li>Confere a cada minuto: se falta a foto do último dia fechado, grava. Até 30 min depois do horário é
 *       "agendada"; depois (a API estava fora) é "atrasada", com o momento real.</li>
 *   <li>Ao subir, reconstrói pela RICTRLOC (passagem pelos leitos no SPData) os dias sem foto dos últimos
 *       {@code hat.ocupacao.reconstruir-dias} (padrão 60; 0 desliga). Leitos ativos de um dia passado = os ativos
 *       de agora mais os que estiveram ocupados naquele dia (o SPData não guarda o status antigo do leito).</li>
 *   <li>Sem banco do painel a rotina não roda (aviso no log).</li>
 * </ul>
 */
@Component
public class FotoOcupacao {

    public static final String ZONA = "America/Sao_Paulo";
    private static final ZoneId FUSO = ZoneId.of(ZONA);
    private static final Logger LOG = LoggerFactory.getLogger(FotoOcupacao.class);
    private static final LocalDate SEM_SAIDA = LocalDate.of(1899, 12, 30);

    private final OcupacaoDiaria diaria;
    private final LeitoService leitos;
    private final BlocoService blocos;
    private final LeitoRepository repositorio;
    private final CnesService cnes;
    private final LocalTime hora;
    private final boolean ligada;
    private final int reconstruirDias;
    private volatile String ultimoAviso;

    public FotoOcupacao(OcupacaoDiaria diaria, LeitoService leitos, BlocoService blocos, LeitoRepository repositorio, CnesService cnes,
                        @Value("${hat.ocupacao.foto.hora:00:05}") String hora,
                        @Value("${hat.ocupacao.foto.ligada:true}") boolean ligada,
                        @Value("${hat.ocupacao.reconstruir-dias:60}") int reconstruirDias) {
        this.diaria = diaria;
        this.leitos = leitos;
        this.blocos = blocos;
        this.repositorio = repositorio;
        this.cnes = cnes;
        this.hora = LocalTime.parse(hora.trim());
        this.ligada = ligada;
        this.reconstruirDias = Math.max(0, Math.min(reconstruirDias, OcupacaoDiaria.DIAS_MAX));
    }

    public LocalTime hora() {
        return hora;
    }

    public static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    /** Dia do censo de uma foto tirada no horário marcado. */
    static LocalDate diaDaFoto(LocalDateTime momento, LocalTime hora) {
        return hora.isBefore(LocalTime.NOON) ? momento.toLocalDate().minusDays(1) : momento.toLocalDate();
    }

    /** Momento marcado da foto de um dia. */
    static LocalDateTime momentoDoDia(LocalDate dia, LocalTime hora) {
        return hora.isBefore(LocalTime.NOON) ? dia.plusDays(1).atTime(hora) : dia.atTime(hora);
    }

    /** O último dia cuja foto já devia existir. */
    static LocalDate ultimoDiaFechado(LocalDateTime agora, LocalTime hora) {
        LocalDateTime marcadoHoje = agora.toLocalDate().atTime(hora);
        return diaDaFoto(agora.isBefore(marcadoHoje) ? marcadoHoje.minusDays(1) : marcadoHoje, hora);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoSubir() {
        if (!ligada) return;
        if (!diaria.configurado()) {
            LOG.warn("Banco do painel não configurado (INDICADORES_DB_URL): a foto diária da ocupação não vai rodar.");
            return;
        }
        Thread t = new Thread(() -> {
            verificar();
            reconstruir();
        }, "foto-ocupacao-inicio");
        t.setDaemon(true);
        t.start();
    }

    @Scheduled(cron = "0 * * * * *", zone = ZONA)
    public void verificar() {
        if (!ligada || !diaria.configurado()) return;
        try {
            LocalDateTime agora = LocalDateTime.now(FUSO).withNano(0);
            LocalDate dia = ultimoDiaFechado(agora, hora);
            synchronized (this) {
                if (!diaria.diasGravados(dia, dia).isEmpty()) return;
                String origem = agora.isAfter(momentoDoDia(dia, hora).plusMinutes(30)) ? OcupacaoDiaria.ATRASADA : OcupacaoDiaria.AGENDADA;
                fotografarAgora(dia, agora, origem);
                LOG.info("Foto da ocupação do dia {} gravada ({}).", dia, origem);
            }
            ultimoAviso = null;
        } catch (RuntimeException e) {
            avisar("A foto diária da ocupação falhou; tenta de novo no próximo minuto: " + e.getMessage());
        }
    }

    /** Foto da posição de agora (as mesmas contas da tela). */
    void fotografarAgora(LocalDate dia, LocalDateTime momento, String origem) {
        List<ContaOcupacao.LeitoSp> sp = leitos.getLeitosOcupacao(blocos.listarBlocosAtivos()).stream()
                .map(l -> new ContaOcupacao.LeitoSp(l.bloco(), l.quarto(), l.leito(), l.reg() != null ? l.tipoConvenio() : null))
                .toList();
        diaria.gravar(dia, momento, origem, ContaOcupacao.contar(sp, cnes.cadastro()));
    }

    /** Reconstrói os dias sem foto dos últimos N dias pela RICTRLOC. Nunca troca uma foto de verdade. */
    synchronized void reconstruir() {
        if (reconstruirDias == 0 || !diaria.configurado()) return;
        try {
            LocalDate ultimo = ultimoDiaFechado(LocalDateTime.now(FUSO), hora);
            LocalDate de = ultimo.minusDays(reconstruirDias);
            Set<LocalDate> gravados = diaria.diasGravados(de, ultimo); // o último dia fica para a foto de verdade (verificar)
            List<LocalDate> faltam = de.datesUntil(ultimo).filter(d -> !gravados.contains(d)).toList();
            if (faltam.isEmpty()) return;

            List<ContaOcupacao.LeitoSp> base = leitos.getLeitosOcupacao(blocos.listarBlocosAtivos()).stream()
                    .map(l -> new ContaOcupacao.LeitoSp(l.bloco(), l.quarto(), l.leito(), null)).toList();
            List<Passagem> passagens = repositorio.findPassagens(faltam.get(0).toString(), momentoDoDia(ultimo, hora).toLocalDate().toString())
                    .stream().map(FotoOcupacao::passagem).toList();
            var cad = cnes.cadastro();
            for (LocalDate d : faltam) {
                LocalDateTime m = momentoDoDia(d, hora);
                diaria.gravar(d, m, OcupacaoDiaria.RECONSTRUIDA, ContaOcupacao.contar(leitosNoMomento(base, passagens, m), cad));
            }
            LOG.info("Ocupação reconstruída pela RICTRLOC em {} dia(s), de {} a {}.", faltam.size(), faltam.get(0), faltam.get(faltam.size() - 1));
        } catch (RuntimeException e) {
            LOG.warn("Não deu para reconstruir a ocupação dos dias passados: {}", e.getMessage());
        }
    }

    record Passagem(String bloco, String quarto, String leito, LocalDateTime entrada, LocalDateTime saida, String tipoConvenio) {
        boolean ocupaEm(LocalDateTime m) {
            return entrada != null && !entrada.isAfter(m) && (saida == null || saida.isAfter(m));
        }
    }

    /** Leitos (ordem da tela) com quem estava neles no momento; leito ocupado que hoje está inativo entra no fim do bloco. */
    static List<ContaOcupacao.LeitoSp> leitosNoMomento(List<ContaOcupacao.LeitoSp> base, List<Passagem> passagens, LocalDateTime m) {
        Map<String, String> ocupado = new LinkedHashMap<>();
        Map<String, Passagem> porChave = new LinkedHashMap<>();
        for (Passagem p : passagens) {
            if (!p.ocupaEm(m)) continue;
            String k = chave(p.bloco(), p.quarto(), p.leito());
            ocupado.putIfAbsent(k, p.tipoConvenio());
            porChave.putIfAbsent(k, p);
        }
        Set<String> blocosBase = new HashSet<>();
        Set<String> naBase = new HashSet<>();
        for (var l : base) {
            blocosBase.add(Texto.limpo(l.bloco()));
            naBase.add(chave(l.bloco(), l.quarto(), l.leito()));
        }
        List<ContaOcupacao.LeitoSp> out = new ArrayList<>();
        for (int i = 0; i < base.size(); i++) {
            var l = base.get(i);
            out.add(new ContaOcupacao.LeitoSp(l.bloco(), l.quarto(), l.leito(), ocupado.get(chave(l.bloco(), l.quarto(), l.leito()))));
            boolean fimDoBloco = i == base.size() - 1 || !Objects.equals(Texto.limpo(base.get(i + 1).bloco()), Texto.limpo(l.bloco()));
            if (fimDoBloco) {
                for (var e : porChave.entrySet()) {
                    Passagem p = e.getValue();
                    if (!naBase.contains(e.getKey()) && Objects.equals(Texto.limpo(p.bloco()), Texto.limpo(l.bloco()))) {
                        out.add(new ContaOcupacao.LeitoSp(Texto.limpo(p.bloco()), Texto.limpo(p.quarto()), Texto.limpo(p.leito()), p.tipoConvenio()));
                    }
                }
            }
        }
        // blocos sem nenhum leito ativo hoje ficam de fora (a tela também não os mostra)
        return out.stream().filter(l -> blocosBase.contains(Texto.limpo(l.bloco()))).toList();
    }

    private static String chave(String bloco, String quarto, String leito) {
        return Texto.limpo(bloco) + "|" + Texto.limpo(quarto) + "|" + Texto.limpo(leito);
    }

    private static Passagem passagem(Object[] r) {
        Integer conv = r[7] != null ? ((Number) r[7]).intValue() : null;
        LocalDateTime saida = dataHora(r[5], r[6]);
        if (saida != null && saida.toLocalDate().equals(SEM_SAIDA)) saida = null;
        String tipo = conv == null ? "Convenio" : conv == 1 ? "SUS" : conv == 2 ? "Particular" : "Convenio";
        return new Passagem(Texto.limpo(r[0]), Texto.limpo(r[1]), Texto.limpo(r[2]), dataHora(r[3], r[4]), saida, tipo);
    }

    private static LocalDateTime dataHora(Object data, Object hhmm) {
        if (data == null) return null;
        LocalDate d = data instanceof java.sql.Date sd ? sd.toLocalDate() : data instanceof LocalDate ld ? ld : LocalDate.parse(data.toString().substring(0, 10));
        String h = Texto.limpo(hhmm);
        if (h == null || !h.matches("\\d{4}")) return d.atStartOfDay();
        int hh = Integer.parseInt(h.substring(0, 2)), mm = Integer.parseInt(h.substring(2));
        return hh < 24 && mm < 60 ? d.atTime(hh, mm) : d.atStartOfDay();
    }

    private void avisar(String msg) {
        if (!msg.equals(ultimoAviso)) LOG.warn(msg);
        ultimoAviso = msg;
    }
}
