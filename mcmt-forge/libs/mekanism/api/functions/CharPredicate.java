package mekanism.api.functions;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
   boolean test(char var1);

   default CharPredicate and(CharPredicate other) {
      Objects.requireNonNull(other);
      return value -> this.test(value) && other.test(value);
   }

   default CharPredicate negate() {
      return value -> !this.test(value);
   }

   default CharPredicate or(CharPredicate other) {
      Objects.requireNonNull(other);
      return value -> this.test(value) || other.test(value);
   }
}
