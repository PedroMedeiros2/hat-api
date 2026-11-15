package br.com.hat.hat_api.seguranca;

import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * Uma permissao do GAW por indicador (aplicacao "indicadores" no catalogo do GAW). Substitui as
 * permissoes do DBCONFIG (VISUALIZAR + codigo HAT do indicador). A mesma tabela existe no servidor da
 * tela (hat-dashboard: src/lib/acesso.ts) para esconder o que a pessoa nao ve. Rota fora daqui = negada.
 */
public final class Permissoes {

    /** HAT0002: movimentacao de leitos e internacoes detalhadas. */
    public static final String MOVIMENTACAO = "indicadores:movimentacao:ver";
    /** HAT0005: exames cardiologicos. */
    public static final String EXAMES_CARDIOLOGICOS = "indicadores:exames-cardiologicos:ver";
    /** HAT0006: exames de imagem. */
    public static final String EXAMES_IMAGEM = "indicadores:exames-imagem:ver";
    /** HAT0007: taxa de ocupacao operacional (geral). */
    public static final String OCUPACAO_GERAL = "indicadores:ocupacao-geral:ver";
    /** HAT0008: taxa de ocupacao oficial (CNES). */
    public static final String OCUPACAO_CNES = "indicadores:ocupacao-cnes:ver";
    /** HAT0009: atendimentos externos. */
    public static final String ATENDIMENTOS = "indicadores:atendimentos:ver";
    /** HAT0010: censo hospitalar. */
    public static final String CENSO = "indicadores:censo:ver";
    /** HAT0011: centro cirurgico. */
    public static final String CENTRO_CIRURGICO = "indicadores:centro-cirurgico:ver";
    /** HAT0012: hemodinamica. */
    public static final String HEMODINAMICA = "indicadores:hemodinamica:ver";

    /** Metodo + prefixo da rota (sem barra no fim) -> permissao; {@code null} = basta ter entrado. A primeira que casar vale. */
    record Regra(HttpMethod metodo, String rota, String permissao) {
    }

    static final List<Regra> REGRAS = List.of(
            new Regra(HttpMethod.GET, "/api/leitos/movimentacao", MOVIMENTACAO),
            new Regra(HttpMethod.GET, "/api/leitos/internacoes", MOVIMENTACAO),
            new Regra(HttpMethod.GET, "/api/leitos/saidas", MOVIMENTACAO),
            new Regra(HttpMethod.GET, "/api/leitos/ficha", MOVIMENTACAO),
            new Regra(HttpMethod.GET, "/api/leitos/ocupacao-geral", OCUPACAO_GERAL),
            new Regra(HttpMethod.GET, "/api/leitos/ocupacao-cnes", OCUPACAO_CNES),
            new Regra(HttpMethod.GET, "/api/exames/cardiologia", EXAMES_CARDIOLOGICOS),
            new Regra(HttpMethod.GET, "/api/exames/imagem", EXAMES_IMAGEM),
            new Regra(HttpMethod.GET, "/api/atendimentos", ATENDIMENTOS),
            // a lista de blocos serve de filtro em mais de uma tela: so os nomes, sem pacientes
            new Regra(HttpMethod.GET, "/api/censo/blocos", null),
            new Regra(HttpMethod.GET, "/api/censo", CENSO),
            new Regra(HttpMethod.GET, "/api/centro-cirurgico", CENTRO_CIRURGICO),
            new Regra(HttpMethod.GET, "/api/hemodinamica", HEMODINAMICA),
            // feriados (contas de dias uteis em varias telas): nenhum dado de paciente
            new Regra(HttpMethod.GET, "/api/feriados", null)
    );

    private Permissoes() {
    }
}
