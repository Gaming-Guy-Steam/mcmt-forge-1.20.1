package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.common.integration.computer.MethodRestriction;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface SyntheticComputerMethod {
   MethodRestriction restriction() default MethodRestriction.NONE;

   String[] requiredMods() default {};

   String getter() default "";

   boolean threadSafeGetter() default false;

   String setter() default "";

   boolean threadSafeSetter() default false;

   String getterDescription() default "";

   String setterDescription() default "";
}
