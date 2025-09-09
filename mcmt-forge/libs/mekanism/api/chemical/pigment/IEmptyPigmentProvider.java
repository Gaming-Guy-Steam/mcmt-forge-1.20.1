package mekanism.api.chemical.pigment;

import mekanism.api.chemical.IEmptyStackProvider;
import org.jetbrains.annotations.NotNull;

public interface IEmptyPigmentProvider extends IEmptyStackProvider<Pigment, PigmentStack> {
   @NotNull
   default PigmentStack getEmptyStack() {
      return PigmentStack.EMPTY;
   }
}
