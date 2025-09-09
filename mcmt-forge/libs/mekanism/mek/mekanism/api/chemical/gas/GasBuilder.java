package mekanism.api.chemical.gas;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class GasBuilder extends ChemicalBuilder<Gas, GasBuilder> {
   protected GasBuilder(ResourceLocation texture) {
      super(texture);
   }

   public static GasBuilder builder() {
      return builder(new ResourceLocation("mekanism", "liquid/liquid"));
   }

   public static GasBuilder builder(ResourceLocation texture) {
      return new GasBuilder(Objects.requireNonNull(texture));
   }
}
