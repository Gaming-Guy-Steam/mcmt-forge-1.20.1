package mekanism.common.content.miner;

import java.util.Objects;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class MinerFilter<FILTER extends MinerFilter<FILTER>> extends BaseFilter<FILTER> {
   @SyntheticComputerMethod(
      getter = "getReplaceTarget",
      setter = "setReplaceTarget",
      threadSafeGetter = true,
      threadSafeSetter = true
   )
   public Item replaceTarget = Items.f_41852_;
   @SyntheticComputerMethod(
      getter = "getRequiresReplacement",
      setter = "setRequiresReplacement",
      threadSafeSetter = true,
      threadSafeGetter = true
   )
   public boolean requiresReplacement;

   protected MinerFilter() {
   }

   protected MinerFilter(FILTER filter) {
      this.replaceTarget = filter.replaceTarget;
      this.requiresReplacement = filter.requiresReplacement;
   }

   public boolean replaceTargetMatches(@NotNull Item target) {
      return this.replaceTarget != Items.f_41852_ && this.replaceTarget == target;
   }

   public abstract boolean canFilter(BlockState state);

   @ComputerMethod
   public abstract boolean hasBlacklistedElement();

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128379_("requireStack", this.requiresReplacement);
      if (this.replaceTarget != Items.f_41852_) {
         NBTUtils.writeRegistryEntry(nbtTags, "replaceStack", ForgeRegistries.ITEMS, this.replaceTarget);
      }

      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      this.requiresReplacement = nbtTags.m_128471_("requireStack");
      this.replaceTarget = NBTUtils.readRegistryEntry(nbtTags, "replaceStack", ForgeRegistries.ITEMS, Items.f_41852_);
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.writeBoolean(this.requiresReplacement);
      buffer.writeRegistryId(ForgeRegistries.ITEMS, this.replaceTarget);
   }

   @Override
   public void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.requiresReplacement = dataStream.readBoolean();
      this.replaceTarget = (Item)dataStream.readRegistryIdSafe(Item.class);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.replaceTarget, this.requiresReplacement);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass() && super.equals(o)) {
         MinerFilter<?> other = (MinerFilter<?>)o;
         return this.requiresReplacement == other.requiresReplacement && this.replaceTarget == other.replaceTarget;
      } else {
         return false;
      }
   }

   @ComputerMethod(
      threadSafe = true
   )
   public abstract FILTER clone();
}
