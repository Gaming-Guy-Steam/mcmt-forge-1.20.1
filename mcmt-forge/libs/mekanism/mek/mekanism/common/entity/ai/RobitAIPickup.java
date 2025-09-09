package mekanism.common.entity.ai;

import java.util.function.Predicate;
import mekanism.common.entity.EntityRobit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public class RobitAIPickup extends RobitAIBase {
   private static final int SEARCH_RADIUS = 10;
   private static final int SEARCH_RADIUS_SQ = 100;
   private final Predicate<Entity> itemPredicate = entity -> !entity.m_5833_() && entity instanceof ItemEntity item && this.theRobit.isItemValid(item);
   private ItemEntity closest;

   public RobitAIPickup(EntityRobit entityRobit, float speed) {
      super(entityRobit, speed);
   }

   public boolean m_8036_() {
      if (!this.theRobit.getDropPickup()) {
         return false;
      } else {
         PathNavigation navigator = this.getNavigator();
         if (this.validateClosest() && navigator.m_6570_(this.closest, 0) != null) {
            return true;
         } else {
            this.closest = null;
            double closestDistance = -1.0;

            for (ItemEntity entity : this.theRobit
               .m_9236_()
               .m_6443_(
                  ItemEntity.class,
                  new AABB(
                     this.theRobit.m_20185_() - 10.0,
                     this.theRobit.m_20186_() - 10.0,
                     this.theRobit.m_20189_() - 10.0,
                     this.theRobit.m_20185_() + 10.0,
                     this.theRobit.m_20186_() + 10.0,
                     this.theRobit.m_20189_() + 10.0
                  ),
                  this.itemPredicate
               )) {
               double distance = this.theRobit.m_20280_(entity);
               if (distance <= 100.0 && (closestDistance == -1.0 || distance < closestDistance) && navigator.m_6570_(entity, 0) != null) {
                  this.closest = entity;
                  closestDistance = distance;
               }
            }

            return this.closest != null;
         }
      }
   }

   private boolean validateClosest() {
      return this.closest != null
         && this.theRobit.isItemValid(this.closest)
         && this.closest.m_9236_().m_46472_() == this.theRobit.m_9236_().m_46472_()
         && this.theRobit.m_20280_(this.closest) <= 100.0;
   }

   public boolean m_8045_() {
      return this.theRobit.getDropPickup() && this.validateClosest() && !this.getNavigator().m_26571_() && !this.theRobit.getEnergyContainer().isEmpty();
   }

   public void m_8037_() {
      if (this.theRobit.getDropPickup()) {
         this.updateTask(this.closest);
      }
   }
}
