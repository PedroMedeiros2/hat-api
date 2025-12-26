package br.com.hat.hat_api.permissoes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "PERMISSAO_INDICADOR")
@Getter
@Setter
public class PermissaoIndicador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "matricula_usuario", length = 20)
    private String matriculaUsuario;

    @Column(name = "pode_visualizar")
    @ColumnDefault("1")
    private Integer podeVisualizar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_indicador", nullable = false)
    private Indicador indicador;
}