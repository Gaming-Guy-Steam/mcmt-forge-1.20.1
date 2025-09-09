package mekanism.common.inventory.container.entity.robit;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.list.SyncableResourceKeyList;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;

public class MainRobitContainer extends RobitContainer implements MekanismContainer.ISpecificContainerTracker {
   private List<ResourceKey<RobitSkin>> unlockedSkins = Collections.emptyList();

   public MainRobitContainer(int id, Inventory inv, EntityRobit robit) {
      super(MekanismContainerTypes.MAIN_ROBIT, id, inv, robit);
   }

   public List<ResourceKey<RobitSkin>> getUnlockedSkins() {
      return this.unlockedSkins;
   }

   @Override
   public List<ISyncableData> getSpecificSyncableData() {
      ISyncableData data;
      if (this.isRemote()) {
         data = SyncableResourceKeyList.create(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, () -> this.unlockedSkins, value -> this.unlockedSkins = value);
      } else {
         Registry<RobitSkin> registry = this.inv.f_35978_.m_9236_().m_9598_().m_175515_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
         data = SyncableResourceKeyList.create(
            MekanismAPI.ROBIT_SKIN_REGISTRY_NAME,
            () -> registry.m_6579_().stream().filter(entry -> ((RobitSkin)entry.getValue()).isUnlocked(this.inv.f_35978_)).map(Entry::getKey).toList(),
            value -> this.unlockedSkins = value
         );
      }

      return Collections.singletonList(data);
   }
}
