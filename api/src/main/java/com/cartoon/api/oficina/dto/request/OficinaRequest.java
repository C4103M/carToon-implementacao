package com.cartoon.api.oficina.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OficinaRequest (@NotBlank(message = "O nome é obrigatório")
                              @Size(max=250, message = "O nome deve ter no máximo 250 caracteres.")
                              String nome,

                              @NotBlank(message = "O endereço é obrigatório")
                              @Size(max=250)
                              String endereco,

                              @NotBlank(message = "O telefone é obrigatório")
                              @Pattern(
                                      regexp = "^(\\(\\d{2}\\)|\\d{2})[\\s-]?\\d{4,5}[\\s-]?\\d{4}$",
                                      message = "O número de telefone informado é inválido.")
                              String telefone) {
}
