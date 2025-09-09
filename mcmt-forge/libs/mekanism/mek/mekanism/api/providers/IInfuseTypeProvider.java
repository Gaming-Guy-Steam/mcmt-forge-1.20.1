package mekanism.api.providers;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import org.jetbrains.annotations.NotNull;

public interface IInfuseTypeProvider extends IChemicalProvider<InfuseType> {
   @NotNull
   default InfusionStack getStack(long size) {
      return new InfusionStack(this.getChemical(), size);
   }
}
