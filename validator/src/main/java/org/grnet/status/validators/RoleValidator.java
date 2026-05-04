package org.grnet.status.validators;

import io.quarkus.arc.Arc;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;
import org.grnet.status.constraints.ValidRole;

@RegisterForReflection
public class RoleValidator implements ConstraintValidator<ValidRole, String> {

    private ResourceAuthorizationService resourceAuthorizationService;

    @Override
    public void initialize(ValidRole constraintAnnotation) {
        this.resourceAuthorizationService =
                Arc.container().instance(ResourceAuthorizationService.class).get();
    }
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return true; // or false depending on your API rules
        }

        return resourceAuthorizationService.getAllRoles()
                .stream()
                .anyMatch(r -> value.equals(r.id));
    }
}