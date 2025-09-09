package mekanism.api.chemical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.IReverseTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class Chemical<CHEMICAL extends Chemical<CHEMICAL>> implements IChemicalProvider<CHEMICAL>, IChemicalAttributeContainer<CHEMICAL> {
   private final ChemicalTags<CHEMICAL> chemicalTags;
   private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap;
   private final ResourceLocation iconLocation;
   private final boolean hidden;
   private final int tint;
   @Nullable
   private String translationKey;

   protected Chemical(ChemicalBuilder<CHEMICAL, ?> builder, ChemicalTags<CHEMICAL> chemicalTags) {
      this.chemicalTags = chemicalTags;
      this.attributeMap = new HashMap<>(builder.getAttributeMap());
      this.iconLocation = builder.getTexture();
      this.tint = builder.getTint();
      this.hidden = builder.isHidden();
   }

   @NotNull
   @Override
   public CHEMICAL getChemical() {
      return (CHEMICAL)this;
   }

   @Override
   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = this.getDefaultTranslationKey();
      }

      return this.translationKey;
   }

   @Override
   public boolean has(Class<? extends ChemicalAttribute> type) {
      return this.attributeMap.containsKey(type);
   }

   @Nullable
   @Override
   public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> type) {
      return (ATTRIBUTE)this.attributeMap.get(type);
   }

   public void addAttribute(ChemicalAttribute attribute) {
      this.attributeMap.put((Class<? extends ChemicalAttribute>)attribute.getClass(), attribute);
   }

   @Override
   public Collection<ChemicalAttribute> getAttributes() {
      return this.attributeMap.values();
   }

   @Override
   public Collection<Class<? extends ChemicalAttribute>> getAttributeTypes() {
      return this.attributeMap.keySet();
   }

   public abstract CompoundTag write(CompoundTag var1);

   protected abstract String getDefaultTranslationKey();

   @Override
   public Component getTextComponent() {
      return TextComponentUtil.translate(this.getTranslationKey());
   }

   public ResourceLocation getIcon() {
      return this.iconLocation;
   }

   public int getTint() {
      return this.tint;
   }

   public int getColorRepresentation() {
      return this.getTint();
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public boolean is(TagKey<CHEMICAL> tag) {
      return this.getReverseTag().map(reverseTag -> reverseTag.containsTag(tag)).orElse(false);
   }

   public Stream<TagKey<CHEMICAL>> getTags() {
      return this.getReverseTag().<Stream<TagKey<CHEMICAL>>>map(IReverseTag::getTagKeys).orElseGet(Stream::empty);
   }

   protected Optional<IReverseTag<CHEMICAL>> getReverseTag() {
      return this.chemicalTags.getManager().flatMap(manager -> manager.getReverseTag(this.getChemical()));
   }

   public abstract boolean isEmptyType();

   @Override
   public abstract ResourceLocation getRegistryName();
}
