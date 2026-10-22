package com.cartoon.api.fipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FipeItemResponse(
        String code,
        String name
) {}
