package mekanism.common.content.qio.filter;

import java.util.Objects;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class QIOItemStackFilter extends QIOFilter<QIOItemStackFilter> implements IItemStackFilter<QIOItemStackFilter> {
   private ItemStack itemType = ItemStack.f_41583_;
   public boolean fuzzyMode;

   public QIOItemStackFilter(ItemStack item) {
      this.itemType = item;
   }

   public QIOItemStackFilter() {
   }

   @Override
   public Finder getFinder() {
      return this.fuzzyMode ? Finder.item(this.itemType) : Finder.strict(this.itemType);
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128379_("fuzzyMode", this.fuzzyMode);
      this.itemType.m_41739_(nbtTags);
      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      NBTUtils.setBooleanIfPresent(nbtTags, "fuzzyMode", fuzzy -> this.fuzzyMode = fuzzy);
      this.itemType = ItemStack.m_41712_(nbtTags);
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.writeBoolean(this.fuzzyMode);
      buffer.m_130055_(this.itemType);
   }

   @Override
   public void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.fuzzyMode = dataStream.readBoolean();
      this.itemType = dataStream.m_130267_();
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.itemType.m_41720_(), this.fuzzyMode);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass() && super.equals(o)) {
         QIOItemStackFilter other = (QIOItemStackFilter)o;
         if (this.fuzzyMode != other.fuzzyMode) {
            return false;
         } else {
            return this.fuzzyMode ? this.itemType.m_41720_() == other.itemType.m_41720_() : ItemHandlerHelper.canItemStacksStack(this.itemType, other.itemType);
         }
      } else {
         return false;
      }
   }

   public QIOItemStackFilter clone() {
      QIOItemStackFilter filter = new QIOItemStackFilter();
      filter.itemType = this.itemType.m_41777_();
      filter.fuzzyMode = this.fuzzyMode;
      return filter;
   }

   @Override
   public FilterType getFilterType() {
      return FilterType.QIO_ITEMSTACK_FILTER;
   }

   @NotNull
   @Override
   public ItemStack getItemStack() {
      return this.itemType;
   }

   @Override
   public void setItemStack(@NotNull ItemStack stack) {
      this.itemType = stack;
   }
}
