package com.cartoon.api.compartilhado.config;

import com.cartoon.api.servico.dto.deserializer.TempoDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule tempoModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalTime.class, new TempoDeserializer());
        return module;
    }
}
