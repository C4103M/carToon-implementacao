package com.cartoon.api.oficina.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OficinaRequest (@NotBlank @Size(max=250) String nome,
                              @NotBlank @Size(max=250) String endereco,
                              @NotBlank @Pattern(
                                      regexp = "^(\\(\\d{2}\\)|\\d{2})[\\s-]?\\d{4,5}[\\s-]?\\d{4}$",
                                      message = "O número de telefone informado é inválido.")
                              String telefone) {
}
