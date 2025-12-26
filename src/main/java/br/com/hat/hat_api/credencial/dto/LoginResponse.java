package br.com.hat.hat_api.credencial.dto;

import java.util.List;

public record LoginResponse(String matricula, String nome, List<String> permissoes, long expiresAt) {
}
