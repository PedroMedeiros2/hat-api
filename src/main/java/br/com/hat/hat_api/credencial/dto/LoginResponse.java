package br.com.hat.hat_api.credencial.dto;

import java.util.List;

public record LoginResponse(String token, String nome, List<String> permissoes) {
}
