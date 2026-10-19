package com.cartoon.api.fipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FipeVeiculoResponse(
        Integer vehicleType,
        String price,
        String brand,
        String model,
        Integer modelYear,
        String fuel,
        String codeFipe,
        String referenceMonth,
        String fuelAcronym
) {}
