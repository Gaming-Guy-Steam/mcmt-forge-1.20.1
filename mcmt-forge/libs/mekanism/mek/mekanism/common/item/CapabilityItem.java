package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityItem extends Item {
   protected CapabilityItem(Properties properties) {
      super(properties);
   }

   protected boolean areCapabilityConfigsLoaded() {
      return true;
   }

   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
   }

   public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      if (!this.areCapabilityConfigsLoaded()) {
         return super.initCapabilities(stack, nbt);
      } else {
         List<ItemCapabilityWrapper.ItemCapability> capabilities = new ArrayList<>();
         this.gatherCapabilities(capabilities, stack, nbt);
         return (ICapabilityProvider)(capabilities.isEmpty()
            ? super.initCapabilities(stack, nbt)
            : new ItemCapabilityWrapper(stack, capabilities.toArray(ItemCapabilityWrapper.ItemCapability[]::new)));
      }
   }
}
