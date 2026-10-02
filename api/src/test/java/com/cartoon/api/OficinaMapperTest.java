package com.cartoon.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import org.junit.jupiter.api.Test;

class OficinaMapperTest {

    private final OficinaMapper mapper = new OficinaMapper();

    @Test
    void toEntity_deveNormalizarTelefone() {
        OficinaRequest request = new OficinaRequest("Oficina X", "Rua A, 10", "(11) 91234-5678");

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getTelefone()).isEqualTo("11912345678");
    }

    @Test
    void toEntity_deveRemoverEspacosDoNome() {
        OficinaRequest request = new OficinaRequest("  Oficina X  ", "Rua A, 10", "11912345678");

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getNome()).isEqualTo("Oficina X");
    }

    @Test
    void toEntity_novaOficinaDeveNascerAtiva() {
        OficinaRequest request = new OficinaRequest("Oficina X", "Rua A, 10", "11912345678");

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getAtivo()).isTrue();
    }

    @Test
    void updateEntity_deveSubstituirOsDadosDaOficinaExistente() {
        Oficina oficina = new Oficina();
        oficina.setId(7);
        oficina.setNome("Nome Antigo");
        oficina.setEndereco("Rua Antiga, 1");
        oficina.setTelefone("11900000000");
        oficina.setAtivo(true);
        OficinaRequest request = new OficinaRequest("Nome Novo", "Av. Nova, 99", "(11) 97777-6666");

        mapper.updateEntity(oficina, request);

        assertThat(oficina.getId()).isEqualTo(7);
        assertThat(oficina.getNome()).isEqualTo("Nome Novo");
        assertThat(oficina.getEndereco()).isEqualTo("Av. Nova, 99");
        assertThat(oficina.getTelefone()).isEqualTo("11977776666");
        assertThat(oficina.getAtivo()).isTrue();
    }

    @Test
    void toResponse_deveCopiarTodosOsCampos() {
        Oficina oficina = new Oficina();
        oficina.setId(3);
        oficina.setNome("Oficina X");
        oficina.setEndereco("Rua A, 10");
        oficina.setTelefone("11912345678");
        oficina.setAtivo(false);

        OficinaResponse response = mapper.toResponse(oficina);

        assertThat(response.id()).isEqualTo(3);
        assertThat(response.nome()).isEqualTo("Oficina X");
        assertThat(response.endereco()).isEqualTo("Rua A, 10");
        assertThat(response.telefone()).isEqualTo("11912345678");
        assertThat(response.ativo()).isFalse();
    }
}