package mekanism.common.entity.ai;

import mekanism.common.entity.EntityRobit;
import net.minecraft.world.entity.player.Player;

public class RobitAIFollow extends RobitAIBase {
   private Player theOwner;
   private final float maxDist;
   private final float minDist;

   public RobitAIFollow(EntityRobit entityRobit, float speed, float min, float max) {
      super(entityRobit, speed);
      this.minDist = min;
      this.maxDist = max;
   }

   public boolean m_8036_() {
      Player player = this.theRobit.getOwner();
      if (player != null && !player.m_5833_()) {
         if (this.theRobit.m_9236_().m_46472_() != player.m_9236_().m_46472_()) {
            return false;
         } else if (!this.theRobit.getFollowing()) {
            this.theRobit.m_21563_().m_24960_(player, 6.0F, this.theRobit.m_8132_() / 10.0F);
            return false;
         } else if (this.theRobit.m_20280_(player) < this.minDist * this.minDist) {
            return false;
         } else if (this.theRobit.getEnergyContainer().isEmpty()) {
            return false;
         } else {
            this.theOwner = player;
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean m_8045_() {
      return !this.getNavigator().m_26571_()
         && this.theRobit.m_20280_(this.theOwner) > this.maxDist * this.maxDist
         && this.theRobit.getFollowing()
         && !this.theRobit.getEnergyContainer().isEmpty()
         && this.theOwner.m_9236_().m_46472_() == this.theRobit.m_9236_().m_46472_();
   }

   @Override
   public void m_8041_() {
      this.theOwner = null;
      super.m_8041_();
   }

   public void m_8037_() {
      if (this.theRobit.getFollowing()) {
         this.updateTask(this.theOwner);
      }
   }
}
