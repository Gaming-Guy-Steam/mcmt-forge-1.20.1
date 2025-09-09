package mekanism.api.chemical.pigment;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class PigmentBuilder extends ChemicalBuilder<Pigment, PigmentBuilder> {
   protected PigmentBuilder(ResourceLocation texture) {
      super(texture);
   }

   public static PigmentBuilder builder() {
      return builder(new ResourceLocation("mekanism", "pigment/base"));
   }

   public static PigmentBuilder builder(ResourceLocation texture) {
      return new PigmentBuilder(Objects.requireNonNull(texture));
   }
}
