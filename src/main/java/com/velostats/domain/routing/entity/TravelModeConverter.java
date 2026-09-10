package com.velostats.domain.routing.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores a travel mode as the lowercase string every backend agrees on, rather than as the enum
 * constant name.
 */
@Converter(autoApply = true)
public class TravelModeConverter implements AttributeConverter<TravelMode, String> {

    @Override
    public String convertToDatabaseColumn(TravelMode mode) {
        return mode == null ? null : mode.value();
    }

    @Override
    public TravelMode convertToEntityAttribute(String value) {
        return value == null ? null : TravelMode.fromValue(value);
    }
}
