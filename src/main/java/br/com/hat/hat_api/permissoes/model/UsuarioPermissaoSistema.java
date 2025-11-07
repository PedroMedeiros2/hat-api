package br.com.hat.hat_api.permissoes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario_permissao_sistema")
@Getter
@Setter
public class UsuarioPermissaoSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "matricula_usuario", nullable = false, length = 20)
    private String matriculaUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permissao")
    private PermissaoSistema permissaoSistema;
}
