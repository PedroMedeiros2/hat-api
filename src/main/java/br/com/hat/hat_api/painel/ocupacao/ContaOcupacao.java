package br.com.hat.hat_api.painel.ocupacao;

import br.com.hat.hat_api.painel.cnes.LeitoCnes;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * As contas da Taxa de ocupação por bloco, iguais às da tela (montarLeitos, casarBloco e statsBlocos em
 * hat-dashboard/src/components/indicadores/taxa-ocupacao/dados.ts), para a foto do dia bater com o que a tela
 * mostrava na hora. Só contagens: nenhum dado de paciente.
 */
public final class ContaOcupacao {

    public static final String GERAL = "geral";
    public static final String CNES = "cnes";
    static final LeitoCnes.TipoLeito TIPO_RESERVA = new LeitoCnes.TipoLeito("FIXO", "Fixo", true, false, null, 1);
    private static final Pattern NUMERO = Pattern.compile("(\\d+)");

    private ContaOcupacao() {
    }

    /** Um leito do SPData, na ordem da tela; tipoConvenio nulo = livre (SUS, Particular ou Convenio). */
    public record LeitoSp(String bloco, String quarto, String leito, String tipoConvenio) { }

    /** Um grupo (o bloco, ou bloco + tipo à parte) numa base. */
    public record Linha(String grupo, String bloco, String tipo, String tipoNome, boolean aParte, String base,
                        int ativos, int ocupados, int sus, int particular, int convenio) { }

    private record Item(String bloco, LeitoCnes.TipoLeito tipo, boolean inS, boolean inC, String tipoConvenio) { }

    public static List<Linha> contar(List<LeitoSp> sp, LeitoCnes.Cadastro cad) {
        Map<String, List<LeitoCnes>> cnes = new LinkedHashMap<>();
        cad.leitos().stream().filter(LeitoCnes::ativo)
                .sorted(Comparator.comparingInt(LeitoCnes::ordem).thenComparing(l -> l.id() == null ? 0L : l.id()))
                .forEach(c -> cnes.computeIfAbsent(c.bloco().trim().toUpperCase(), k -> new ArrayList<>()).add(c));
        Map<String, List<LeitoSp>> porBloco = new LinkedHashMap<>();
        for (LeitoSp l : sp) porBloco.computeIfAbsent(l.bloco() == null ? "" : l.bloco().trim(), k -> new ArrayList<>()).add(l);
        for (String b : cnes.keySet()) porBloco.putIfAbsent(b, new ArrayList<>());

        List<Item> itens = new ArrayList<>();
        for (var e : porBloco.entrySet()) {
            String bc = e.getKey();
            List<LeitoSp> ls = e.getValue();
            List<LeitoCnes> cn = cnes.getOrDefault(bc.toUpperCase(), List.of());
            LeitoCnes.TipoLeito doBloco = tipoDoBloco(cad, bc);
            Map<Integer, Integer> par = casarBloco(ls, cn);
            for (int i = 0; i < ls.size(); i++) {
                Integer j = par.get(i);
                LeitoCnes c = j != null ? cn.get(j) : null;
                itens.add(new Item(bc, c != null ? tipoDoLeitoCnes(cad, c) : doBloco, true, c != null, ls.get(i).tipoConvenio()));
            }
            Set<Integer> usados = new HashSet<>(par.values());
            for (int j = 0; j < cn.size(); j++) {
                if (!usados.contains(j)) itens.add(new Item(bc, tipoDoLeitoCnes(cad, cn.get(j)), false, true, null));
            }
        }

        Map<String, List<Item>> grupos = new LinkedHashMap<>();
        for (Item it : itens) {
            if (!it.tipo().contaNaTaxa() && !it.tipo().aParte()) continue; // tipo que não conta (ex.: Virtual)
            String g = it.tipo().contaNaTaxa() ? it.bloco() : it.bloco() + "|" + it.tipo().codigo();
            grupos.computeIfAbsent(g, k -> new ArrayList<>()).add(it);
        }
        List<Linha> out = new ArrayList<>();
        for (String base : List.of(GERAL, CNES)) {
            for (var g : grupos.entrySet()) {
                List<Item> ls = g.getValue();
                Item p = ls.get(0);
                int ativ = 0, oc = 0, sus = 0, part = 0, conv = 0;
                for (Item it : ls) {
                    if (GERAL.equals(base) ? it.inS() : it.inC()) ativ++;
                    if (it.tipoConvenio() != null) {
                        oc++;
                        if ("SUS".equals(it.tipoConvenio())) sus++;
                        else if ("Particular".equals(it.tipoConvenio())) part++;
                        else conv++;
                    }
                }
                if (ativ == 0 && oc == 0) continue;
                boolean aParte = !p.tipo().contaNaTaxa() && p.tipo().aParte();
                out.add(new Linha(g.getKey(), p.bloco(), p.tipo().codigo(), p.tipo().nome(), aParte, base, ativ, oc, sus, part, conv));
            }
        }
        return out;
    }

    static LeitoCnes.TipoLeito tipoDoBloco(LeitoCnes.Cadastro cad, String bloco) {
        String b = bloco.trim();
        String cod = cad.blocos().stream().filter(x -> x.bloco().equalsIgnoreCase(b)).map(LeitoCnes.BlocoTipo::tipo).findFirst().orElse(cad.tipoPadrao());
        return tipo(cad, cod).or(() -> tipo(cad, cad.tipoPadrao())).orElse(TIPO_RESERVA);
    }

    static LeitoCnes.TipoLeito tipoDoLeitoCnes(LeitoCnes.Cadastro cad, LeitoCnes c) {
        if (c.tipo() != null && !c.tipo().isBlank()) {
            var t = tipo(cad, c.tipo());
            if (t.isPresent()) return t.get();
        }
        return tipoDoBloco(cad, c.bloco());
    }

    private static Optional<LeitoCnes.TipoLeito> tipo(LeitoCnes.Cadastro cad, String codigo) {
        return cad.tipos().stream().filter(t -> t.codigo().equals(codigo)).findFirst();
    }

    /** Igual ao casarBloco da tela: pelo quarto e número quando o CNES traz "Q:", senão pela ordem no bloco. */
    static Map<Integer, Integer> casarBloco(List<LeitoSp> sp, List<LeitoCnes> cn) {
        Map<Integer, Integer> par = new HashMap<>();
        boolean porChave = !cn.isEmpty() && cn.stream().allMatch(c -> c.quarto().startsWith("Q:"));
        Map<String, Integer> chaveCn = new HashMap<>();
        for (int i = 0; i < cn.size(); i++) chaveCn.put(numero(cn.get(i).quarto()) + "|" + numero(cn.get(i).leito()), i); // a última vence, como no Map da tela
        Set<Integer> usados = new HashSet<>();
        for (int i = 0; i < sp.size(); i++) {
            Integer j = porChave ? chaveCn.get(numero(sp.get(i).quarto()) + "|" + numero(sp.get(i).leito())) : (i < cn.size() ? i : null);
            if (j != null && usados.add(j)) par.put(i, j);
        }
        return par;
    }

    static String numero(String s) {
        Matcher m = NUMERO.matcher(s == null ? "" : s);
        if (!m.find()) return "";
        String d = m.group(1).replaceFirst("^0+(?=\\d)", "");
        return d;
    }
}
