package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class JsonGasBuilder extends JsonChemicalBuilder<Gas, GasBuilder, JsonGasBuilder> {
   public JsonGasBuilder(ThingParser<JsonGasBuilder> ownerParser, ResourceLocation registryName) {
      super(ownerParser, registryName);
   }

   protected String getThingTypeDisplayName() {
      return "Gas";
   }

   protected Gas buildInternal() {
      GasBuilder internal = this.texture == null ? GasBuilder.builder() : GasBuilder.builder(this.texture);
      this.applyBaseData(internal);
      return ChemicalUtil.gas(internal, this.colorRepresentation);
   }
}
