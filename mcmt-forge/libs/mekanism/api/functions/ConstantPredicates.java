package mekanism.api.functions;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import net.minecraftforge.common.util.TriPredicate;

public class ConstantPredicates {
   private static final Predicate<Object> alwaysTrue = t -> true;
   private static final BiPredicate<Object, Object> alwaysTrueBi = (t, u) -> true;
   private static final TriPredicate<Object, Object, Object> alwaysTrueTri = (t, u, v) -> true;
   private static final Predicate<Object> alwaysFalse = t -> false;
   private static final BiPredicate<Object, Object> alwaysFalseBi = (t, u) -> false;
   private static final TriPredicate<Object, Object, Object> alwaysFalseTri = (t, u, v) -> false;
   private static final BiPredicate<Object, AutomationType> internalOnly = (t, automationType) -> automationType == AutomationType.INTERNAL;
   private static final BiPredicate<Object, AutomationType> notExternal = (t, automationType) -> automationType != AutomationType.EXTERNAL;

   private ConstantPredicates() {
   }

   public static <T> Predicate<T> alwaysTrue() {
      return (Predicate<T>)alwaysTrue;
   }

   public static <T, U> BiPredicate<T, U> alwaysTrueBi() {
      return (BiPredicate<T, U>)alwaysTrueBi;
   }

   public static <T, U, V> TriPredicate<T, U, V> alwaysTrueTri() {
      return (TriPredicate<T, U, V>)alwaysTrueTri;
   }

   public static <T> Predicate<T> alwaysFalse() {
      return (Predicate<T>)alwaysFalse;
   }

   public static <T, V> BiPredicate<T, V> alwaysFalseBi() {
      return (BiPredicate<T, V>)alwaysFalseBi;
   }

   public static <T, U, V> TriPredicate<T, U, V> alwaysFalseTri() {
      return (TriPredicate<T, U, V>)alwaysFalseTri;
   }

   public static <T> BiPredicate<T, AutomationType> internalOnly() {
      return (BiPredicate<T, AutomationType>)internalOnly;
   }

   public static <T> BiPredicate<T, AutomationType> notExternal() {
      return (BiPredicate<T, AutomationType>)notExternal;
   }
}
