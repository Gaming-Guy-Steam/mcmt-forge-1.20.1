package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.DataHandlerUtils;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TileComponentUpgrade implements ITileComponent, MekanismContainer.ISpecificContainerTracker {
   private static final int UPGRADE_TICKS_REQUIRED = 20;
   private int upgradeTicks;
   private final TileEntityMekanism tile;
   @SyntheticComputerMethod(
      getter = "getInstalledUpgrades"
   )
   private final Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
   private final Set<Upgrade> supported;
   private final UpgradeInventorySlot upgradeSlot;
   private final UpgradeInventorySlot upgradeOutputSlot;

   public TileComponentUpgrade(TileEntityMekanism tile) {
      this.tile = tile;
      this.supported = EnumSet.copyOf(this.tile.getSupportedUpgrade());
      this.upgradeSlot = UpgradeInventorySlot.input(this.tile, this.supported);
      this.upgradeOutputSlot = UpgradeInventorySlot.output(this.tile);
      this.tile.addComponent(this);
   }

   public void tickServer() {
      ItemStack stack = this.upgradeSlot.getStack();
      if (!stack.m_41619_() && stack.m_41720_() instanceof IUpgradeItem upgradeItem) {
         Upgrade type = upgradeItem.getUpgradeType(stack);
         if (this.supports(type) && this.getUpgrades(type) < type.getMax()) {
            if (this.upgradeTicks < 20) {
               this.upgradeTicks++;
               return;
            }

            if (this.upgradeTicks == 20) {
               int added = this.addUpgrades(type, this.upgradeSlot.getCount());
               if (added > 0) {
                  MekanismUtils.logMismatchedStackSize(this.upgradeSlot.shrinkStack(added, Action.EXECUTE), added);
               }
            }
         }
      }

      this.upgradeTicks = 0;
   }

   public UpgradeInventorySlot getUpgradeSlot() {
      return this.upgradeSlot;
   }

   public UpgradeInventorySlot getUpgradeOutputSlot() {
      return this.upgradeOutputSlot;
   }

   public double getScaledUpgradeProgress() {
      return this.upgradeTicks / 20.0;
   }

   public int getUpgrades(Upgrade upgrade) {
      return this.upgrades.getOrDefault(upgrade, 0);
   }

   public int addUpgrades(Upgrade upgrade, int maxAvailable) {
      int installed = this.getUpgrades(upgrade);
      if (installed < upgrade.getMax()) {
         int toAdd = Math.min(upgrade.getMax() - installed, maxAvailable);
         if (toAdd > 0) {
            this.upgrades.put(upgrade, installed + toAdd);
            this.tile.recalculateUpgrades(upgrade);
            if (upgrade == Upgrade.MUFFLING) {
               this.tile.sendUpdatePacket();
            }

            this.tile.markForSave();
            return toAdd;
         }
      }

      return 0;
   }

   public void removeUpgrade(Upgrade upgrade, boolean removeAll) {
      int installed = this.getUpgrades(upgrade);
      if (installed > 0) {
         int toRemove = removeAll ? installed : 1;
         ItemStack simulatedRemainder = this.upgradeOutputSlot.insertItem(UpgradeUtils.getStack(upgrade, toRemove), Action.SIMULATE, AutomationType.INTERNAL);
         if (simulatedRemainder.m_41613_() < toRemove) {
            toRemove -= simulatedRemainder.m_41613_();
            if (installed == toRemove) {
               this.upgrades.remove(upgrade);
            } else {
               this.upgrades.put(upgrade, installed - toRemove);
            }

            this.tile.recalculateUpgrades(upgrade);
            this.upgradeOutputSlot.insertItem(UpgradeUtils.getStack(upgrade, toRemove), Action.EXECUTE, AutomationType.INTERNAL);
         }
      }
   }

   public void setSupported(Upgrade upgrade) {
      this.setSupported(upgrade, true);
   }

   public void setSupported(Upgrade upgrade, boolean isSupported) {
      if (isSupported) {
         this.supported.add(upgrade);
      } else {
         this.supported.remove(upgrade);
      }
   }

   public boolean supports(Upgrade upgrade) {
      return this.supported.contains(upgrade);
   }

   public boolean isUpgradeInstalled(Upgrade upgrade) {
      return this.upgrades.containsKey(upgrade);
   }

   public Set<Upgrade> getInstalledTypes() {
      return this.upgrades.keySet();
   }

   @ComputerMethod(
      nameOverride = "getSupportedUpgrades"
   )
   public Set<Upgrade> getSupportedTypes() {
      return this.supported;
   }

   private List<IInventorySlot> getSlots() {
      return List.of(this.upgradeSlot, this.upgradeOutputSlot);
   }

   @Override
   public void read(CompoundTag nbtTags) {
      NBTUtils.setCompoundIfPresent(nbtTags, "componentUpgrade", upgradeNBT -> {
         this.upgrades.clear();
         this.upgrades.putAll(Upgrade.buildMap(upgradeNBT));

         for (Upgrade upgrade : this.getSupportedTypes()) {
            this.tile.recalculateUpgrades(upgrade);
         }

         NBTUtils.setListIfPresent(upgradeNBT, "Items", 10, list -> DataHandlerUtils.readContainers(this.getSlots(), list));
      });
   }

   @Override
   public void write(CompoundTag nbtTags) {
      CompoundTag upgradeNBT = new CompoundTag();
      Upgrade.saveMap(this.upgrades, upgradeNBT);
      upgradeNBT.m_128365_("Items", DataHandlerUtils.writeContainers(this.getSlots()));
      nbtTags.m_128365_("componentUpgrade", upgradeNBT);
   }

   @Override
   public void addToUpdateTag(CompoundTag updateTag) {
      if (this.supports(Upgrade.MUFFLING)) {
         updateTag.m_128405_("muffling", this.upgrades.getOrDefault(Upgrade.MUFFLING, 0));
      }
   }

   @Override
   public void readFromUpdateTag(CompoundTag updateTag) {
      if (this.supports(Upgrade.MUFFLING)) {
         NBTUtils.setIntIfPresent(updateTag, "muffling", amount -> {
            if (amount == 0) {
               this.upgrades.remove(Upgrade.MUFFLING);
            } else {
               this.upgrades.put(Upgrade.MUFFLING, amount);
            }
         });
      }
   }

   @Override
   public List<ISyncableData> getSpecificSyncableData() {
      List<ISyncableData> list = new ArrayList<>();
      list.add(SyncableInt.create(() -> this.upgradeTicks, value -> this.upgradeTicks = value));

      for (Upgrade upgrade : EnumUtils.UPGRADES) {
         if (this.supports(upgrade)) {
            list.add(SyncableInt.create(() -> this.upgrades.getOrDefault(upgrade, 0), value -> {
               if (value == 0) {
                  this.upgrades.remove(upgrade);
               } else if (value > 0) {
                  this.upgrades.put(upgrade, value);
               }
            }));
         }
      }

      return list;
   }
}
