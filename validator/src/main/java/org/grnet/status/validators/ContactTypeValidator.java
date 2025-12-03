package org.grnet.status.validators;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.grnet.status.constraints.ValidContactType;
import org.grnet.status.enums.ContactType;

import java.util.Arrays;

@RegisterForReflection
public class ContactTypeValidator implements ConstraintValidator<ValidContactType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return Arrays.stream(ContactType.values())
                .anyMatch(e -> e.name().equals(value));
    }
}

