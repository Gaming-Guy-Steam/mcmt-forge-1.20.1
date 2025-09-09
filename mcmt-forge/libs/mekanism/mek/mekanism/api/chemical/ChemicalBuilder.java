package mekanism.api.chemical;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class ChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>> {
   private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap = new Object2ObjectOpenHashMap();
   private final ResourceLocation texture;
   private int tint = 16777215;
   private boolean hidden;

   protected ChemicalBuilder(ResourceLocation texture) {
      this.texture = texture;
   }

   public BUILDER with(ChemicalAttribute attribute) {
      this.attributeMap.put((Class<? extends ChemicalAttribute>)attribute.getClass(), attribute);
      return this.self();
   }

   public Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> getAttributeMap() {
      return Collections.unmodifiableMap(this.attributeMap);
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }

   public BUILDER tint(int tint) {
      this.tint = tint;
      return this.self();
   }

   public BUILDER hidden() {
      this.hidden = true;
      return this.self();
   }

   public int getTint() {
      return this.tint;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   private BUILDER self() {
      return (BUILDER)this;
   }
}
