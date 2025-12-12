package org.grnet.status.validators;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.grnet.status.constraints.ValidEventName;
import org.grnet.status.constraints.ValidEventStatus;
import org.grnet.status.enums.ContactType;
import org.grnet.status.enums.EventStatus;

import java.util.Arrays;

@RegisterForReflection
public class EventStatusValidator implements ConstraintValidator<ValidEventStatus, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return Arrays.stream(EventStatus.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(value));
    }
}

