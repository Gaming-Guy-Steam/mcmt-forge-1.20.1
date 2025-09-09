package mekanism.common.inventory.slot;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIOFrequency;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class QIODriveSlot extends BasicInventorySlot {
   private final IQIODriveHolder driveHolder;
   private final QIODriveData.QIODriveKey key;

   public <TILE extends IMekanismInventory & IQIODriveHolder> QIODriveSlot(TILE inventory, int slot, @Nullable IContentsListener listener, int x, int y) {
      super(notExternal, notExternal, stack -> stack.m_41720_() instanceof IQIODriveItem, listener, x, y);
      this.key = new QIODriveData.QIODriveKey(inventory, slot);
      this.driveHolder = inventory;
   }

   @Override
   public void setStack(ItemStack stack) {
      if (!this.isRemote() && !this.isEmpty()) {
         this.removeDrive();
      }

      super.setStack(stack);
      if (!this.isRemote() && !this.isEmpty()) {
         this.addDrive(this.getStack());
      }
   }

   @Override
   protected void setStackUnchecked(ItemStack stack) {
      if (!this.isRemote() && !this.isEmpty()) {
         this.removeDrive();
      }

      super.setStackUnchecked(stack);
      if (!this.isRemote() && !this.isEmpty()) {
         this.addDrive(this.getStack());
      }
   }

   @Override
   public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
      ItemStack ret = super.insertItem(stack, action, automationType);
      if (!this.isRemote() && action.execute() && ret.m_41619_()) {
         this.addDrive(stack);
      }

      return ret;
   }

   @Override
   public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
      if (!this.isRemote() && action.execute()) {
         ItemStack ret = super.extractItem(amount, Action.SIMULATE, automationType);
         if (!ret.m_41619_()) {
            this.removeDrive();
         }
      }

      return super.extractItem(amount, action, automationType);
   }

   public QIODriveData.QIODriveKey getKey() {
      return this.key;
   }

   private boolean isRemote() {
      Level world = ((BlockEntity)this.driveHolder).m_58904_();
      return world == null || world.m_5776_();
   }

   private void addDrive(ItemStack stack) {
      QIOFrequency frequency = this.driveHolder.getQIOFrequency();
      if (frequency != null) {
         frequency.addDrive(this.key);
      }
   }

   private void removeDrive() {
      QIOFrequency frequency = this.driveHolder.getQIOFrequency();
      if (frequency != null) {
         frequency.removeDrive(this.key, true);
      }
   }
}
