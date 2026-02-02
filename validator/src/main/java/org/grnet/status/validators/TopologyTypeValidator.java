package org.grnet.status.validators;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.grnet.status.constraints.ValidTopologyType;
import org.grnet.status.enums.TopologyType;

import java.util.Arrays;

@RegisterForReflection
public class TopologyTypeValidator implements ConstraintValidator<ValidTopologyType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null|| value.equals("")) return true;
        return Arrays.stream(TopologyType.values())
                .anyMatch(e -> e.name().equals(value));
    }
}
