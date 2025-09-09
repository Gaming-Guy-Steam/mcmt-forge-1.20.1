package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class UpgradesRecipeData implements RecipeUpgradeData<UpgradesRecipeData> {
   private final Map<Upgrade, Integer> upgrades;
   private final List<IInventorySlot> slots;

   @Nullable
   static UpgradesRecipeData tryCreate(CompoundTag componentUpgrade) {
      if (componentUpgrade.m_128456_()) {
         return null;
      } else {
         Map<Upgrade, Integer> upgrades = Upgrade.buildMap(componentUpgrade);
         List<IInventorySlot> slots;
         if (componentUpgrade.m_128425_("Items", 9)) {
            slots = ItemRecipeData.readContents(componentUpgrade.m_128437_("Items", 10));
         } else {
            slots = Collections.emptyList();
         }

         return upgrades.isEmpty() && slots.isEmpty() ? null : new UpgradesRecipeData(upgrades, slots);
      }
   }

   private UpgradesRecipeData(Map<Upgrade, Integer> upgrades, List<IInventorySlot> slots) {
      this.upgrades = upgrades;
      this.slots = slots;
   }

   @Nullable
   public UpgradesRecipeData merge(UpgradesRecipeData other) {
      Map<Upgrade, Integer> smallerUpgrades = other.upgrades;
      Map<Upgrade, Integer> largerUpgrades = this.upgrades;
      if (largerUpgrades.size() < smallerUpgrades.size()) {
         smallerUpgrades = this.upgrades;
         largerUpgrades = other.upgrades;
      }

      Map<Upgrade, Integer> upgrades;
      if (smallerUpgrades.isEmpty()) {
         upgrades = largerUpgrades;
      } else {
         upgrades = new EnumMap<>(largerUpgrades);

         for (Entry<Upgrade, Integer> entry : smallerUpgrades.entrySet()) {
            Upgrade upgrade = entry.getKey();
            int total = upgrades.merge(upgrade, entry.getValue(), Integer::sum);
            if (total > upgrade.getMax()) {
               return null;
            }
         }
      }

      List<IInventorySlot> allSlots = new ArrayList<>(this.slots);
      allSlots.addAll(other.slots);
      return new UpgradesRecipeData(upgrades, allSlots);
   }

   @Override
   public boolean applyToStack(ItemStack stack) {
      if (this.upgrades.isEmpty() && this.slots.isEmpty()) {
         return true;
      } else {
         AttributeUpgradeSupport upgradeSupport = Attribute.get(((BlockItem)stack.m_41720_()).m_40614_(), AttributeUpgradeSupport.class);
         if (upgradeSupport == null) {
            return false;
         } else {
            Set<Upgrade> supportedUpgrades = upgradeSupport.supportedUpgrades();
            if (!supportedUpgrades.containsAll(this.upgrades.keySet())) {
               return false;
            } else {
               List<IInventorySlot> stackSlots = List.of(UpgradeInventorySlot.input(null, supportedUpgrades), UpgradeInventorySlot.output(null));
               CompoundTag nbt = new CompoundTag();
               if (!this.upgrades.isEmpty()) {
                  Upgrade.saveMap(this.upgrades, nbt);
               }

               if (ItemRecipeData.applyToStack(this.slots, stackSlots, toWrite -> nbt.m_128365_("Items", toWrite))) {
                  ItemDataUtils.setCompound(stack, "componentUpgrade", nbt);
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }
}
