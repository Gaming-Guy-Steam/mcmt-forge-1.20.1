package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class JsonSlurryBuilder extends JsonChemicalBuilder<Slurry, SlurryBuilder, JsonSlurryBuilder> {
   @Nullable
   private Boolean clean;

   public JsonSlurryBuilder(ThingParser<JsonSlurryBuilder> ownerParser, ResourceLocation registryName) {
      super(ownerParser, registryName);
   }

   public JsonSlurryBuilder ore(ResourceLocation oreTag) {
      return this.baseData(builder -> builder.ore(oreTag));
   }

   public JsonSlurryBuilder texture(ResourceLocation texture) {
      if (this.clean != null) {
         throw new IllegalStateException("Texture cannot be used in combination with clean");
      } else {
         return (JsonSlurryBuilder)super.texture(texture);
      }
   }

   public JsonSlurryBuilder clean(boolean clean) {
      if (this.texture != null) {
         throw new IllegalStateException("Clean cannot be used in combination with specifying an explicit texture");
      } else {
         this.clean = clean;
         return this;
      }
   }

   protected String getThingTypeDisplayName() {
      return "Slurry";
   }

   protected Slurry buildInternal() {
      SlurryBuilder internal;
      if (this.texture == null) {
         if (this.clean == null) {
            throw new IllegalStateException(
               "Slurry " + this.getRegistryName() + " didn't have a texture or fallback texture (whether it is clean or not) specified"
            );
         }

         internal = this.clean ? SlurryBuilder.clean() : SlurryBuilder.dirty();
      } else {
         internal = SlurryBuilder.builder(this.texture);
      }

      this.applyBaseData(internal);
      return ChemicalUtil.slurry(internal, this.colorRepresentation);
   }
}
