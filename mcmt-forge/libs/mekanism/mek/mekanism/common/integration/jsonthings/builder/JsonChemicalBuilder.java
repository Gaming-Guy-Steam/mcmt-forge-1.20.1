package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class JsonChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>, THING_BUILDER extends JsonChemicalBuilder<CHEMICAL, BUILDER, THING_BUILDER>>
   extends BaseBuilder<CHEMICAL, THING_BUILDER> {
   private final List<Consumer<BUILDER>> baseData = new ArrayList<>();
   @Nullable
   protected ResourceLocation texture;
   @Nullable
   protected Integer colorRepresentation;

   protected JsonChemicalBuilder(ThingParser<THING_BUILDER> ownerParser, ResourceLocation registryName) {
      super(ownerParser, registryName);
   }

   protected void applyBaseData(BUILDER builder) {
      for (Consumer<BUILDER> base : this.baseData) {
         base.accept(builder);
      }
   }

   private THING_BUILDER self() {
      return (THING_BUILDER)this;
   }

   public THING_BUILDER texture(ResourceLocation texture) {
      this.texture = texture;
      return this.self();
   }

   protected THING_BUILDER baseData(Consumer<BUILDER> base) {
      this.baseData.add(base);
      return this.self();
   }

   public THING_BUILDER tint(int tint) {
      return this.baseData(builder -> builder.tint(tint));
   }

   public THING_BUILDER colorRepresentation(int color) {
      this.colorRepresentation = color;
      return this.self();
   }

   public THING_BUILDER hidden(boolean hidden) {
      return hidden ? this.baseData(ChemicalBuilder::hidden) : this.self();
   }

   public THING_BUILDER with(ChemicalAttribute attribute) {
      return this.baseData(builder -> builder.with(attribute));
   }
}
