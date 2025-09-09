package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.common.integration.computer.MethodRestriction;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface ComputerMethod {
   String nameOverride() default "";

   MethodRestriction restriction() default MethodRestriction.NONE;

   String[] requiredMods() default {};

   boolean threadSafe() default false;

   boolean requiresPublicSecurity() default false;

   String methodDescription() default "";

   Class[] possibleReturns() default {};
}
