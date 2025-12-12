package org.grnet.status.validators;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.grnet.status.constraints.ValidEventName;
import org.grnet.status.enums.EventName;

import java.util.Arrays;

@RegisterForReflection
public class EventNameValidator implements ConstraintValidator<ValidEventName, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return Arrays.stream(EventName.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(value));
    }
}

