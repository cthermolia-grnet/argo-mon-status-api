package org.grnet.status.constraints;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.grnet.status.validators.EventModeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RegisterForReflection
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventModeValidator.class)
public @interface ValidEventMode {
    String message() default "Invalid event mode";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
