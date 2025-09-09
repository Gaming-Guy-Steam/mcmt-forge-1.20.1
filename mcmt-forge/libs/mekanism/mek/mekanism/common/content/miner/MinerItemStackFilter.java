package mekanism.common.content.miner;

import java.util.Objects;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tags.MekanismTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MinerItemStackFilter extends MinerFilter<MinerItemStackFilter> implements IItemStackFilter<MinerItemStackFilter> {
   private ItemStack itemType = ItemStack.f_41583_;

   public MinerItemStackFilter(ItemStack item) {
      this.itemType = item;
   }

   public MinerItemStackFilter() {
   }

   public MinerItemStackFilter(MinerItemStackFilter filter) {
      super(filter);
      this.itemType = filter.itemType.m_41777_();
   }

   @Override
   public boolean canFilter(BlockState state) {
      ItemStack itemStack = new ItemStack(state.m_60734_());
      return itemStack.m_41619_() ? false : this.itemType.m_41720_() == itemStack.m_41720_();
   }

   @Override
   public boolean hasBlacklistedElement() {
      return !this.itemType.m_41619_()
         && this.itemType.m_41720_() instanceof BlockItem blockItem
         && MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.contains(blockItem.m_40614_());
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      super.write(nbtTags);
      this.itemType.m_41739_(nbtTags);
      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      this.itemType = ItemStack.m_41712_(nbtTags);
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.m_130055_(this.itemType);
   }

   @Override
   public void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.itemType = dataStream.m_130267_();
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.itemType.m_41720_());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() && super.equals(o)
            ? this.itemType.m_41720_() == ((MinerItemStackFilter)o).itemType.m_41720_()
            : false;
      }
   }

   public MinerItemStackFilter clone() {
      return new MinerItemStackFilter(this);
   }

   @Override
   public FilterType getFilterType() {
      return FilterType.MINER_ITEMSTACK_FILTER;
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
