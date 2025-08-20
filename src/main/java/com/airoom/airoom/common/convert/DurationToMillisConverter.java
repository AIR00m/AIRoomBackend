package com.airoom.airoom.common.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = true)
public class DurationToMillisConverter implements AttributeConverter<Duration, Long> {
    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        if (attribute != null) {
            return attribute.toMillis();
        }
        return null;
    }

    @Override
    public Duration convertToEntityAttribute(Long dbData) {
        if (dbData != null) {
            return Duration.ofMillis(dbData);
        }
        return null;
    }
}
