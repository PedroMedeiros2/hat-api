package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.repository.LeitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BlocoService {

    private final LeitoRepository leitoRepository;

    public List<String> listarBlocosAtivos() {
        return leitoRepository.findBlocosAtivos()
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .toList();
    }

    public List<String> resolverBlocos(List<String> blocos) {
        if (blocos == null || blocos.stream().allMatch(b -> b == null || b.isBlank())) {
            return listarBlocosAtivos();
        }
        return blocos;
    }
}
