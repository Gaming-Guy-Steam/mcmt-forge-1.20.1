package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class JsonPigmentBuilder extends JsonChemicalBuilder<Pigment, PigmentBuilder, JsonPigmentBuilder> {
   public JsonPigmentBuilder(ThingParser<JsonPigmentBuilder> ownerParser, ResourceLocation registryName) {
      super(ownerParser, registryName);
   }

   protected String getThingTypeDisplayName() {
      return "Pigment";
   }

   protected Pigment buildInternal() {
      PigmentBuilder internal = this.texture == null ? PigmentBuilder.builder() : PigmentBuilder.builder(this.texture);
      this.applyBaseData(internal);
      return ChemicalUtil.pigment(internal, this.colorRepresentation);
   }
}
