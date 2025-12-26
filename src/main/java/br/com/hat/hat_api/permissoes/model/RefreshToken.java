package br.com.hat.hat_api.permissoes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MATRICULA_USUARIO", nullable = false, length = 20)
    private String matriculaUsuario;

    @Column(name = "TOKEN", nullable = false, unique = true)
    private String token;

    @Column(name = "DATA_EXPIRACAO", nullable = false)
    private Instant dataExpiracao;
}