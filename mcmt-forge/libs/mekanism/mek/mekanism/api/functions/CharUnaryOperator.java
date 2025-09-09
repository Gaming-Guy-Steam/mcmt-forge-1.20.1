package mekanism.api.functions;

import java.util.Objects;

@FunctionalInterface
public interface CharUnaryOperator {
   char applyAsChar(char var1);

   default CharUnaryOperator compose(CharUnaryOperator before) {
      Objects.requireNonNull(before);
      return c -> this.applyAsChar(before.applyAsChar(c));
   }

   default CharUnaryOperator andThen(CharUnaryOperator after) {
      Objects.requireNonNull(after);
      return t -> after.applyAsChar(this.applyAsChar(t));
   }

   static CharUnaryOperator identity() {
      return t -> t;
   }
}
