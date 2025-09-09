package mekanism.api.chemical.infuse;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class InfuseTypeBuilder extends ChemicalBuilder<InfuseType, InfuseTypeBuilder> {
   protected InfuseTypeBuilder(ResourceLocation texture) {
      super(texture);
   }

   public static InfuseTypeBuilder builder() {
      return builder(new ResourceLocation("mekanism", "infuse_type/base"));
   }

   public static InfuseTypeBuilder builder(ResourceLocation texture) {
      return new InfuseTypeBuilder(Objects.requireNonNull(texture));
   }
}
