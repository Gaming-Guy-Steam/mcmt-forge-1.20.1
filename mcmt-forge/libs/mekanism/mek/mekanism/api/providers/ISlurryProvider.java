package mekanism.api.providers;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.NotNull;

public interface ISlurryProvider extends IChemicalProvider<Slurry> {
   @NotNull
   default SlurryStack getStack(long size) {
      return new SlurryStack(this.getChemical(), size);
   }
}
