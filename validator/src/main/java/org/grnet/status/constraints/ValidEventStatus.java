package org.grnet.status.constraints;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.grnet.status.validators.EventNameValidator;
import org.grnet.status.validators.EventStatusValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RegisterForReflection
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventStatusValidator.class)
public @interface ValidEventStatus {
    String message() default "Invalid event status";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
