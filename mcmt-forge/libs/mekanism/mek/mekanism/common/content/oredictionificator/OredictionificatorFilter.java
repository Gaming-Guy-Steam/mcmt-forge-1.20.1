package mekanism.common.content.oredictionificator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OredictionificatorFilter<TYPE, STACK, FILTER extends OredictionificatorFilter<TYPE, STACK, FILTER>> extends BaseFilter<FILTER> {
   @Nullable
   private TagKey<TYPE> filterLocation;
   @Nullable
   private ITag<TYPE> filterTag;
   @NotNull
   private TYPE selectedOutput = this.getFallbackElement();
   @Nullable
   private STACK cachedSelectedStack;
   private boolean isValid;

   protected OredictionificatorFilter() {
   }

   protected OredictionificatorFilter(OredictionificatorFilter<TYPE, STACK, FILTER> filter) {
      this.filterLocation = filter.filterLocation;
      this.filterTag = filter.filterTag;
      this.selectedOutput = filter.selectedOutput;
      this.cachedSelectedStack = filter.cachedSelectedStack;
      this.isValid = filter.isValid;
   }

   public void flushCachedTag() {
      this.filterTag = this.filterLocation == null ? null : this.getTagManager().getTag(this.filterLocation);
      if (this.filterTag == null || !this.filterTag.isBound()) {
         this.setSelectedOutput(this.getFallbackElement());
      } else if (!this.filterTag.contains(this.selectedOutput)) {
         this.filterTag.stream().findFirst().ifPresentOrElse(this::setSelectedOutput, () -> this.setSelectedOutput(this.getFallbackElement()));
      }
   }

   @Override
   public boolean hasFilter() {
      return this.filterLocation != null && this.isValid;
   }

   public void checkValidity() {
      if (this.filterLocation != null && this.getTagManager().isKnownTagName(this.filterLocation)) {
         for (String filter : ((Map)this.getValidValuesConfig().get()).getOrDefault(this.filterLocation.f_203868_().m_135827_(), Collections.emptyList())) {
            if (this.filterLocation.f_203868_().m_135815_().startsWith(filter)) {
               this.isValid = true;
               return;
            }
         }
      }

      this.isValid = false;
   }

   @ComputerMethod(
      nameOverride = "getFilter",
      threadSafe = true
   )
   public String getFilterText() {
      return this.filterLocation == null ? "" : this.filterLocation.f_203868_().toString();
   }

   public final void setFilter(@Nullable ResourceLocation location) {
      this.filterLocation = location == null ? null : this.getTagManager().createTagKey(location);
      this.flushCachedTag();
      this.isValid = true;
   }

   @ComputerMethod(
      nameOverride = "setFilter"
   )
   public void computerSetFilter(ResourceLocation tag) throws ComputerException {
      if (tag != null && TileEntityOredictionificator.isValidTarget(tag)) {
         this.setFilter(tag);
      } else {
         throw new ComputerException("Invalid tag");
      }
   }

   public final void setSelectedOutput(@NotNull TYPE output) {
      this.selectedOutput = output;
      this.cachedSelectedStack = null;
   }

   public boolean filterMatches(ResourceLocation location) {
      return this.filterLocation != null && this.filterLocation.f_203868_().equals(location);
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128359_("filter", this.getFilterText());
      if (this.selectedOutput != this.getFallbackElement()) {
         NBTUtils.writeRegistryEntry(nbtTags, "selected", this.getRegistry(), this.selectedOutput);
      }

      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      NBTUtils.setResourceLocationIfPresentElse(nbtTags, "filter", this::setFilter, () -> this.setFilter(null));
      NBTUtils.setResourceLocationIfPresent(nbtTags, "selected", this::setSelectedOrFallback);
      this.checkValidity();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      BasePacketHandler.writeOptional(buffer, this.filterLocation, (buf, location) -> buf.m_130085_(location.f_203868_()));
      buffer.m_130085_(this.getRegistry().getKey(this.selectedOutput));
      buffer.writeBoolean(this.isValid);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      super.read(buffer);
      this.setFilter(BasePacketHandler.readOptional(buffer, FriendlyByteBuf::m_130281_));
      this.setSelectedOrFallback(buffer.m_130281_());
      this.isValid = buffer.readBoolean();
   }

   private void setSelectedOrFallback(@NotNull ResourceLocation resourceLocation) {
      TYPE output = (TYPE)this.getRegistry().getValue(resourceLocation);
      this.setSelectedOutput(output == null ? this.getFallbackElement() : output);
   }

   public STACK getResult() {
      if (this.cachedSelectedStack == null) {
         List<TYPE> matchingElements = this.matchingElements();
         if (matchingElements.isEmpty()) {
            this.cachedSelectedStack = this.getEmptyStack();
         } else {
            if (this.selectedOutput == this.getFallbackElement() || !matchingElements.contains(this.selectedOutput)) {
               this.selectedOutput = matchingElements.get(0);
            }

            this.cachedSelectedStack = this.createResultStack(this.selectedOutput);
         }
      }

      return this.cachedSelectedStack;
   }

   public final void next() {
      this.adjustSelected((index, size) -> index < size - 1 ? index + 1 : 0);
   }

   public final void previous() {
      this.adjustSelected((index, size) -> {
         if (index == -1) {
            return 0;
         } else {
            return index > 0 ? index - 1 : size - 1;
         }
      });
   }

   private List<TYPE> matchingElements() {
      return this.filterTag != null && this.filterTag.isBound() ? this.filterTag.stream().toList() : Collections.emptyList();
   }

   private void adjustSelected(IntBinaryOperator calculateSelected) {
      List<TYPE> matchingElements = this.matchingElements();
      int size = matchingElements.size();
      if (size > 1) {
         int selected;
         if (this.selectedOutput == this.getFallbackElement()) {
            selected = size - 1;
         } else {
            selected = calculateSelected.applyAsInt(matchingElements.indexOf(this.selectedOutput), size);
         }

         this.setSelectedOutput(matchingElements.get(selected));
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.filterLocation, this.selectedOutput);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o != null && this.getClass() == o.getClass() && super.equals(o)) {
         OredictionificatorFilter<?, ?, ?> other = (OredictionificatorFilter<?, ?, ?>)o;
         return Objects.equals(this.filterLocation, other.filterLocation) && this.selectedOutput == other.selectedOutput;
      } else {
         return false;
      }
   }

   public abstract TYPE getResultElement();

   protected abstract IForgeRegistry<TYPE> getRegistry();

   protected abstract ITagManager<TYPE> getTagManager();

   protected abstract TYPE getFallbackElement();

   protected abstract STACK getEmptyStack();

   protected abstract STACK createResultStack(TYPE type);

   protected abstract CachedOredictionificatorConfigValue getValidValuesConfig();

   @ComputerMethod(
      threadSafe = true
   )
   public abstract FILTER clone();
}
