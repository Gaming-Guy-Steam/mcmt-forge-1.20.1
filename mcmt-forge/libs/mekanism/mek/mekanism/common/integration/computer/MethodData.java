package mekanism.common.integration.computer;

import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public record MethodData<T>(
   String name,
   MethodRestriction restriction,
   String[] requiredMods,
   boolean threadSafe,
   String[] argumentNames,
   Class<?>[] argClasses,
   Class<?> returnType,
   Class<?>[] returnExtra,
   ComputerMethodFactory.ComputerFunctionCaller<T> handler,
   @Nullable String methodDescription,
   boolean requiresPublicSecurity
) {
   static String[] NO_STRINGS = new String[0];
   static Class<?>[] NO_CLASSES = new Class[0];

   public MethodData(
      String name,
      MethodRestriction restriction,
      String[] requiredMods,
      boolean threadSafe,
      String[] argumentNames,
      Class<?>[] argClasses,
      Class<?> returnType,
      Class<?>[] returnExtra,
      ComputerMethodFactory.ComputerFunctionCaller<T> handler,
      @Nullable String methodDescription,
      boolean requiresPublicSecurity
   ) {
      if (argClasses.length != argumentNames.length) {
         throw new IllegalStateException("Argument arrays should be the same length");
      } else {
         this.name = name;
         this.restriction = restriction;
         this.requiredMods = requiredMods;
         this.threadSafe = threadSafe;
         this.argumentNames = argumentNames;
         this.argClasses = argClasses;
         this.returnType = returnType;
         this.returnExtra = returnExtra;
         this.handler = handler;
         this.methodDescription = methodDescription;
         this.requiresPublicSecurity = requiresPublicSecurity;
      }
   }

   public boolean supports(@Nullable T subject) {
      return this.restriction.test(subject) && this.modsLoaded(this.requiredMods);
   }

   private boolean modsLoaded(String[] mods) {
      for (String mod : mods) {
         if (!ModList.get().isLoaded(mod)) {
            return false;
         }
      }

      return true;
   }

   public static <T> MethodData.Builder<T> builder(String methodName, ComputerMethodFactory.ComputerFunctionCaller<T> handler) {
      return new MethodData.Builder<>(methodName, handler);
   }

   public static class Builder<T> {
      private final String methodName;
      private MethodRestriction restriction = MethodRestriction.NONE;
      private String[] requiredMods = MethodData.NO_STRINGS;
      private boolean threadSafe = false;
      private String[] argumentNames = MethodData.NO_STRINGS;
      private Class<?>[] argClasses = MethodData.NO_CLASSES;
      private Class<?> returnType = void.class;
      private Class<?>[] returnExtra = MethodData.NO_CLASSES;
      private final ComputerMethodFactory.ComputerFunctionCaller<T> handler;
      @Nullable
      private String methodDescription = null;
      private boolean requiresPublicSecurity = false;

      private Builder(String methodName, ComputerMethodFactory.ComputerFunctionCaller<T> handler) {
         this.methodName = methodName;
         this.handler = handler;
      }

      public MethodData<T> build() {
         return new MethodData<>(
            this.methodName,
            this.restriction,
            this.requiredMods,
            this.threadSafe,
            this.argumentNames,
            this.argClasses,
            this.returnType,
            this.returnExtra,
            this.handler,
            this.methodDescription,
            this.requiresPublicSecurity
         );
      }

      public MethodData.Builder<T> restriction(MethodRestriction restriction) {
         this.restriction = restriction;
         return this;
      }

      public MethodData.Builder<T> requiredMods(String... requiredMods) {
         this.requiredMods = requiredMods;
         return this;
      }

      public MethodData.Builder<T> threadSafe() {
         this.threadSafe = true;
         return this;
      }

      public MethodData.Builder<T> arguments(String[] argumentNames, Class<?>[] argClasses) {
         if (argClasses.length != argumentNames.length) {
            throw new IllegalStateException("Argument arrays should be the same length");
         } else {
            this.argumentNames = argumentNames;
            this.argClasses = argClasses;
            return this;
         }
      }

      public MethodData.Builder<T> returnType(Class<?> returnType) {
         this.returnType = returnType;
         return this;
      }

      public MethodData.Builder<T> returnExtra(Class<?>... returnExtra) {
         this.returnExtra = returnExtra;
         return this;
      }

      public MethodData.Builder<T> methodDescription(String methodDescription) {
         this.methodDescription = methodDescription;
         return this;
      }

      public MethodData.Builder<T> requiresPublicSecurity() {
         this.requiresPublicSecurity = true;
         return this;
      }
   }
}
