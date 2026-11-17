package br.com.hat.hat_api.seguranca;

import br.com.hat.hat_api.spdata.controller.CensoController;
import br.com.hat.hat_api.spdata.controller.FeriadoController;
import br.com.hat.hat_api.spdata.service.BlocoService;
import br.com.hat.hat_api.spdata.service.CensoService;
import br.com.hat.hat_api.spdata.service.FeriadoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** So o token do GAW com a permissao do indicador abre a rota; o resto e negado. */
@WebMvcTest(controllers = {CensoController.class, FeriadoController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"hat.gaw.issuer=http://gaw.teste", "hat.ocupacao.foto.ligada=false", "hat.banco.url="})
class SegurancaTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CensoService censo;
    @MockitoBean
    BlocoService blocos;
    @MockitoBean
    FeriadoService feriados;

    private static final String CENSO = "/api/censo/todos-blocos?dataini=2026-11-01&datafim=2026-11-13";

    @Test
    void semTokenRecebe401() throws Exception {
        mvc.perform(get(CENSO)).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSemAPermissaoDoIndicadorRecebe403() throws Exception {
        mvc.perform(get(CENSO).with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.HEMODINAMICA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenComAPermissaoAbreOIndicador() throws Exception {
        when(blocos.listarBlocosAtivos()).thenReturn(List.of());
        mvc.perform(get(CENSO).with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.CENSO))))
                .andExpect(status().isOk());
    }

    @Test
    void listaDeBlocosEFeriadosBastaTerEntrado() throws Exception {
        var qualquer = jwt().authorities(new SimpleGrantedAuthority(Permissoes.HEMODINAMICA));
        mvc.perform(get("/api/censo/blocos").with(qualquer)).andExpect(status().isOk());
        mvc.perform(get("/api/feriados/ano/2026").with(qualquer)).andExpect(status().isOk());
    }

    @Test
    void rotasAntigasDeLoginEUsuariosNaoExistemMais() throws Exception {
        var todas = jwt().authorities(Permissoes.REGRAS.stream().filter(r -> r.permissao() != null)
                .map(r -> (org.springframework.security.core.GrantedAuthority) new SimpleGrantedAuthority(r.permissao())).toList());
        mvc.perform(get("/api/usuarios").with(todas)).andExpect(status().isForbidden());
        mvc.perform(get("/api/permissoes").with(todas)).andExpect(status().isForbidden());
    }

    @Test
    void todaPermissaoEDaAplicacaoIndicadores() {
        assertThat(Permissoes.REGRAS).allSatisfy(r ->
                assertThat(r.permissao() == null || r.permissao().matches("indicadores:[a-z-]+:(ver|editar)")).isTrue());
    }
}
