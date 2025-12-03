package org.grnet.status.constraints;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.grnet.status.validators.ContactTypeValidator;
import org.grnet.status.validators.TopologyTypeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RegisterForReflection
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TopologyTypeValidator.class)
public @interface ValidTopologyType {
    String message() default "Invalid topology type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
