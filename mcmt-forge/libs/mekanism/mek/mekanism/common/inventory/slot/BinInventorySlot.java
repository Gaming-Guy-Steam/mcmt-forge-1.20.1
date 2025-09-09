package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BinInventorySlot extends BasicInventorySlot {
   private static final Predicate<ItemStack> validator = stack -> !(stack.m_41720_() instanceof ItemBlockBin);
   private final boolean isCreative;
   private ItemStack lockStack = ItemStack.f_41583_;

   public static BinInventorySlot create(@Nullable IContentsListener listener, BinTier tier) {
      Objects.requireNonNull(tier, "Bin tier cannot be null");
      return new BinInventorySlot(listener, tier);
   }

   private BinInventorySlot(@Nullable IContentsListener listener, BinTier tier) {
      super(tier.getStorage(), alwaysTrueBi, alwaysTrueBi, validator, listener, 0, 0);
      this.isCreative = tier == BinTier.CREATIVE;
      this.obeyStackLimit = false;
   }

   @Override
   public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
      if (this.isEmpty()) {
         if (this.isLocked() && !ItemHandlerHelper.canItemStacksStack(this.lockStack, stack)) {
            return stack;
         }

         if (this.isCreative && action.execute() && automationType != AutomationType.EXTERNAL) {
            ItemStack simulatedRemainder = super.insertItem(stack, Action.SIMULATE, automationType);
            if (simulatedRemainder.m_41619_()) {
               this.setStackUnchecked(stack.m_255036_(this.getLimit(stack)));
            }

            return simulatedRemainder;
         }
      }

      return super.insertItem(stack, action.combine(!this.isCreative), automationType);
   }

   @Override
   public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
      return super.extractItem(amount, action.combine(!this.isCreative), automationType);
   }

   @Override
   public int setStackSize(int amount, Action action) {
      return super.setStackSize(amount, action.combine(!this.isCreative));
   }

   @Nullable
   @Override
   public InventoryContainerSlot createContainerSlot() {
      return null;
   }

   public ItemStack getBottomStack() {
      return this.isEmpty() ? ItemStack.f_41583_ : this.current.m_255036_(Math.min(this.getCount(), this.current.m_41741_()));
   }

   public boolean setLocked(boolean lock) {
      if (!this.isCreative && this.isLocked() != lock && (!lock || !this.isEmpty())) {
         this.lockStack = lock ? this.current.m_255036_(1) : ItemStack.f_41583_;
         return true;
      } else {
         return false;
      }
   }

   public void setLockStack(@NotNull ItemStack stack) {
      this.lockStack = stack.m_255036_(1);
   }

   public boolean isLocked() {
      return !this.lockStack.m_41619_();
   }

   public ItemStack getRenderStack() {
      return this.isLocked() ? this.getLockStack() : this.getStack();
   }

   public ItemStack getLockStack() {
      return this.lockStack;
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = super.serializeNBT();
      if (this.isLocked()) {
         nbt.m_128365_("lockStack", this.lockStack.serializeNBT());
      }

      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) {
      NBTUtils.setItemStackOrEmpty(nbt, "lockStack", s -> this.lockStack = s);
      super.deserializeNBT(nbt);
   }
}
