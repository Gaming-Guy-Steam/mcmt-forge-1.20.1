package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface WrappingComputerMethod {
   Class<? extends SpecialComputerMethodWrapper> wrapper();

   String[] methodNames();

   MethodRestriction restriction() default MethodRestriction.NONE;

   String[] requiredMods() default {};

   boolean threadSafe() default false;

   String docPlaceholder();

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.METHOD})
   public @interface WrappingComputerMethodHelp {
      String value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.METHOD})
   public @interface WrappingComputerMethodIndex {
      int value();
   }
}
