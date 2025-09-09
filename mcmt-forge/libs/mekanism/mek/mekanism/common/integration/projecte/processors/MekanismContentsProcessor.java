package mekanism.common.integration.projecte.processors;

import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.recipe.upgrade.ItemRecipeData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.UpgradeUtils;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NBTProcessor
public class MekanismContentsProcessor implements INBTProcessor {
   public String getName() {
      return "MekanismContentsProcessor";
   }

   public String getDescription() {
      return "Increases the EMC value of any Mekanism items by the value of the stored or installed contents.";
   }

   public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
      IEMCProxy emcProxy = IEMCProxy.INSTANCE;
      ItemStack stack = info.createStack();
      if (stack.m_41720_() instanceof IItemSustainedInventory sustainedInventory) {
         ListTag storedContents = sustainedInventory.getSustainedInventory(stack);

         for (IInventorySlot slot : ItemRecipeData.readContents(storedContents)) {
            if (!slot.isEmpty()) {
               currentEMC = addEmc(emcProxy, currentEMC, slot.getStack());
            }
         }
      }

      if (stack.m_41720_() instanceof BlockItem blockItem
         && Attribute.has(blockItem.m_40614_(), AttributeUpgradeSupport.class)
         && ItemDataUtils.hasData(stack, "componentUpgrade", 10)) {
         Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getCompound(stack, "componentUpgrade"));

         for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            currentEMC = addEmc(emcProxy, currentEMC, UpgradeUtils.getStack(entry.getKey(), entry.getValue()));
         }
      }

      if (stack.m_41720_() instanceof IModuleContainerItem moduleContainerItem) {
         for (Module<?> module : moduleContainerItem.getModules(stack)) {
            ItemStack moduleStack = module.getData().getItemProvider().getItemStack(module.getInstalledCount());
            currentEMC = addEmc(emcProxy, currentEMC, moduleStack);
         }
      }

      return currentEMC;
   }

   private static long addEmc(IEMCProxy emcProxy, long currentEMC, ItemStack stack) throws ArithmeticException {
      long itemEmc = emcProxy.getValue(stack);
      if (itemEmc > 0L) {
         long stackEmc = Math.multiplyExact(itemEmc, stack.m_41613_());
         currentEMC = Math.addExact(currentEMC, stackEmc);
      }

      return currentEMC;
   }
}
