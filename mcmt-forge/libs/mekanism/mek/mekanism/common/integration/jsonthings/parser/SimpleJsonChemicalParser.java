package mekanism.common.integration.jsonthings.parser;

import com.google.gson.JsonObject;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalType;
import mekanism.common.integration.jsonthings.builder.JsonChemicalBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

@NothingNullByDefault
public class SimpleJsonChemicalParser<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>, THING_BUILDER extends JsonChemicalBuilder<CHEMICAL, BUILDER, THING_BUILDER>>
   extends JsonChemicalParser<CHEMICAL, BUILDER, THING_BUILDER> {
   private final BiFunction<ThingParser<THING_BUILDER>, ResourceLocation, THING_BUILDER> builderFunction;

   SimpleJsonChemicalParser(
      IEventBus bus,
      ChemicalType chemicalType,
      String thingType,
      ResourceKey<? extends Registry<CHEMICAL>> registryKey,
      BiFunction<ThingParser<THING_BUILDER>, ResourceLocation, THING_BUILDER> builderFunction
   ) {
      super(bus, chemicalType, thingType, registryKey);
      this.builderFunction = builderFunction;
   }

   protected THING_BUILDER processThing(ResourceLocation key, JsonObject data, Consumer<THING_BUILDER> builderModification) {
      THING_BUILDER builder = this.builderFunction.apply(this, key);
      this.parseCommon(data, builder);
      builderModification.accept(builder);
      return builder;
   }
}
