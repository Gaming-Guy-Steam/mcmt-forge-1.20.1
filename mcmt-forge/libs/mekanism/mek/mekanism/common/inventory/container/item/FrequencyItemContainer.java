package mekanism.common.inventory.container.item;

import java.util.Collections;
import java.util.List;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public abstract class FrequencyItemContainer<FREQ extends Frequency> extends MekanismItemContainer {
   private List<FREQ> publicCache = Collections.emptyList();
   private List<FREQ> privateCache = Collections.emptyList();
   private FREQ selectedFrequency;

   protected FrequencyItemContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, InteractionHand hand, ItemStack stack) {
      super(type, id, inv, hand, stack);
   }

   public abstract FrequencyType<FREQ> getFrequencyType();

   public InteractionHand getHand() {
      return this.hand;
   }

   public FREQ getFrequency() {
      return this.selectedFrequency;
   }

   public List<FREQ> getPublicCache() {
      return this.publicCache;
   }

   public List<FREQ> getPrivateCache() {
      return this.privateCache;
   }

   @Override
   protected void addContainerTrackers() {
      super.addContainerTrackers();
      if (this.isRemote()) {
         this.track(SyncableFrequency.create(this::getFrequency, value -> this.selectedFrequency = value));
         this.track(SyncableFrequencyList.create(this::getPublicCache, value -> this.publicCache = value));
         this.track(SyncableFrequencyList.create(this::getPrivateCache, value -> this.privateCache = value));
      } else {
         this.track(SyncableFrequency.create(() -> {
            IFrequencyItem frequencyItem = (IFrequencyItem)this.stack.m_41720_();
            if (frequencyItem.hasFrequency(this.stack)) {
               this.selectedFrequency = (FREQ)frequencyItem.getFrequency(this.stack);
               if (this.selectedFrequency == null) {
                  frequencyItem.setFrequency(this.stack, null);
               }
            } else {
               this.selectedFrequency = null;
            }

            return this.selectedFrequency;
         }, value -> this.selectedFrequency = (FREQ)value));
         this.track(SyncableFrequencyList.create(() -> this.getFrequencyType().getManager(null).getFrequencies(), value -> this.publicCache = value));
         this.track(
            SyncableFrequencyList.create(() -> this.getFrequencyType().getManager(this.getPlayerUUID()).getFrequencies(), value -> this.privateCache = value)
         );
      }
   }
}
