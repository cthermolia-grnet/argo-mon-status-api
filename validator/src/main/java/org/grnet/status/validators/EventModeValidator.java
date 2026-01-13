package org.grnet.status.validators;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.grnet.status.constraints.ValidEventMode;
import org.grnet.status.enums.EventMode;

import java.util.Arrays;

@RegisterForReflection
public class EventModeValidator implements ConstraintValidator<ValidEventMode, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return Arrays.stream(EventMode.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(value));
    }
}

