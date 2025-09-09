package mekanism.common.inventory.container.item;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PortableTeleporterContainer extends FrequencyItemContainer<TeleporterFrequency> implements IEmptyContainer {
   private byte status;

   public PortableTeleporterContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
      super(MekanismContainerTypes.PORTABLE_TELEPORTER, id, inv, hand, stack);
   }

   public ItemStack getStack() {
      return this.stack;
   }

   @Override
   public FrequencyType<TeleporterFrequency> getFrequencyType() {
      return FrequencyType.TELEPORTER;
   }

   public byte getStatus() {
      return this.status;
   }

   @Override
   protected void addContainerTrackers() {
      super.addContainerTrackers();
      if (this.isRemote()) {
         this.track(SyncableByte.create(() -> this.status, value -> this.status = value));
      } else {
         this.track(SyncableByte.create(() -> {
            byte status = 3;
            TeleporterFrequency freq = this.getFrequency();
            if (freq != null && !freq.getActiveCoords().isEmpty()) {
               status = 1;
               if (!this.inv.f_35978_.m_7500_()) {
                  IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(this.stack, 0);
                  if (energyContainer == null) {
                     status = 4;
                  } else {
                     Coord4D coords = freq.getClosestCoords(new Coord4D(this.inv.f_35978_));
                     if (coords != null) {
                        FloatingLong energyNeeded = TileEntityTeleporter.calculateEnergyCost(this.inv.f_35978_, coords);
                        if (energyNeeded != null && energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
                           status = 4;
                        }
                     }
                  }
               }
            }

            return status;
         }, value -> this.status = value));
      }
   }
}
