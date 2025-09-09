package mekanism.common.tile.qio;

import java.util.Map;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent {
   @Nullable
   private HashedItem itemType = null;
   private boolean fuzzy;
   private long count = 0L;
   private long clientStoredCount = 0L;

   public TileEntityQIORedstoneAdapter(BlockPos pos, BlockState state) {
      super(MekanismBlocks.QIO_REDSTONE_ADAPTER, pos, state);
      this.delaySupplier = NO_DELAY;
   }

   public int getRedstoneLevel(Direction side) {
      return side != this.getOppositeDirection() && this.getActive() ? 15 : 0;
   }

   private long getFreqStored() {
      QIOFrequency freq = this.getQIOFrequency();
      if (freq == null || this.itemType == null) {
         return 0L;
      } else {
         return this.fuzzy ? freq.getTypesForItem(this.itemType.getItem()).stream().mapToLong(freq::getStored).sum() : freq.getStored(this.itemType);
      }
   }

   public void handleStackChange(ItemStack stack) {
      this.itemType = stack.m_41619_() ? null : HashedItem.create(stack);
      this.markForSave();
   }

   public void handleCountChange(long count) {
      if (this.count != count) {
         this.count = count;
         this.markForSave();
      }
   }

   public void toggleFuzzyMode() {
      this.setFuzzyMode(!this.fuzzy);
   }

   private void setFuzzyMode(boolean fuzzy) {
      if (this.fuzzy != fuzzy) {
         this.fuzzy = fuzzy;
         this.markForSave();
      }
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      long stored = this.getFreqStored();
      this.setActive(stored > 0L && stored >= this.count);
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      super.writeSustainedData(dataMap);
      if (this.itemType != null) {
         dataMap.m_128365_("singleItem", this.itemType.internalToNBT());
      }

      dataMap.m_128356_("amount", this.count);
      dataMap.m_128379_("fuzzyMode", this.fuzzy);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      super.readSustainedData(dataMap);
      NBTUtils.setItemStackIfPresent(dataMap, "singleItem", item -> this.itemType = HashedItem.create(item));
      NBTUtils.setLongIfPresent(dataMap, "amount", value -> this.count = value);
      NBTUtils.setBooleanIfPresent(dataMap, "fuzzyMode", value -> this.fuzzy = value);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = super.getTileDataRemap();
      remap.put("singleItem", "singleItem");
      remap.put("amount", "amount");
      remap.put("fuzzyMode", "fuzzyMode");
      return remap;
   }

   @ComputerMethod(
      nameOverride = "getTargetItem"
   )
   public ItemStack getItemType() {
      return this.itemType == null ? ItemStack.f_41583_ : this.itemType.getInternalStack();
   }

   @ComputerMethod(
      nameOverride = "getTriggerAmount"
   )
   public long getCount() {
      return this.count;
   }

   @ComputerMethod
   public boolean getFuzzyMode() {
      return this.fuzzy;
   }

   public long getStoredCount() {
      return this.clientStoredCount;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableItemStack.create(this::getItemType, value -> {
         if (value.m_41619_()) {
            this.itemType = null;
         } else {
            this.itemType = HashedItem.create(value);
         }
      }));
      container.track(SyncableLong.create(this::getCount, value -> this.count = value));
      container.track(SyncableBoolean.create(this::getFuzzyMode, value -> this.fuzzy = value));
      container.track(SyncableLong.create(this::getFreqStored, value -> this.clientStoredCount = value));
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void clearTargetItem() throws ComputerException {
      this.validateSecurityIsPublic();
      this.handleStackChange(ItemStack.f_41583_);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setTargetItem(ResourceLocation itemName) throws ComputerException {
      this.validateSecurityIsPublic();
      Item item = (Item)ForgeRegistries.ITEMS.getValue(itemName);
      if (item != null && item != Items.f_41852_) {
         this.handleStackChange(new ItemStack(item));
      } else {
         throw new ComputerException("Target item '%s' could not be found. If you are trying to clear it consider using clearTargetItem instead.", itemName);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setTriggerAmount(long amount) throws ComputerException {
      this.validateSecurityIsPublic();
      if (amount < 0L) {
         throw new ComputerException("Trigger amount cannot be negative. Received: %d", amount);
      } else {
         this.handleCountChange(amount);
      }
   }

   @ComputerMethod(
      nameOverride = "toggleFuzzyMode",
      requiresPublicSecurity = true
   )
   void computerToggleFuzzyMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.toggleFuzzyMode();
   }

   @ComputerMethod(
      nameOverride = "setFuzzyMode",
      requiresPublicSecurity = true
   )
   void computerSetFuzzyMode(boolean fuzzy) throws ComputerException {
      this.validateSecurityIsPublic();
      this.setFuzzyMode(fuzzy);
   }
}
