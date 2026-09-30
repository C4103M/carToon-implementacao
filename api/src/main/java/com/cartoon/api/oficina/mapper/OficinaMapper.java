package com.cartoon.api.oficina.mapper;

import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.model.Oficina;
import org.springframework.stereotype.Component;

@Component
public class OficinaMapper {

    public Oficina toEntity(OficinaRequest request){
        Oficina oficina = new Oficina();
        oficina.setNome(request.nome());
        oficina.setEndereco(request.endereco());
        oficina.setTelefone(request.telefone().replaceAll("\\D", ""));
        return oficina;

    }

    public OficinaResponse toResponse(Oficina oficina) {
        return new OficinaResponse(
                oficina.getId(),
                oficina.getNome(),
                oficina.getEndereco(),
                oficina.getTelefone(),
                oficina.getAtivo()
        );
    }

    public void updateEntity(Oficina oficina,OficinaRequest request){
        oficina.setNome(request.nome());
        oficina.setEndereco(request.endereco());
        oficina.setTelefone(request.telefone().replaceAll("\\D", ""));
    }
}
