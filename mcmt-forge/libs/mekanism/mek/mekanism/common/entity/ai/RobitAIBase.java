package mekanism.common.entity.ai;

import java.util.EnumSet;
import mekanism.common.entity.EntityRobit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public abstract class RobitAIBase extends Goal {
   protected final EntityRobit theRobit;
   protected final float moveSpeed;
   private int timeToRecalcPath;
   private float oldWaterCost;

   protected RobitAIBase(EntityRobit entityRobit, float speed) {
      this.theRobit = entityRobit;
      this.moveSpeed = speed;
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   protected PathNavigation getNavigator() {
      return this.theRobit.m_21573_();
   }

   protected Level getWorld() {
      return this.theRobit.m_9236_();
   }

   public void m_8056_() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.theRobit.m_21439_(BlockPathTypes.WATER);
      this.theRobit.m_21441_(BlockPathTypes.WATER, 0.0F);
   }

   public void m_8041_() {
      this.getNavigator().m_26573_();
      this.theRobit.m_21441_(BlockPathTypes.WATER, this.oldWaterCost);
   }

   protected void updateTask(Entity target) {
      this.theRobit.m_21563_().m_24960_(target, 6.0F, this.theRobit.m_8132_() / 10.0F);
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         if (!this.theRobit.m_20159_()) {
            if (this.theRobit.m_20280_(target) >= 144.0) {
               BlockPos targetPos = target.m_20183_();

               for (int i = 0; i < 10; i++) {
                  if (this.tryPathTo(
                     target,
                     targetPos.m_123341_() + this.randomize(-3, 3),
                     targetPos.m_123342_() + this.randomize(-1, 1),
                     targetPos.m_123343_() + this.randomize(-3, 3)
                  )) {
                     return;
                  }
               }
            } else {
               this.getNavigator().m_5624_(target, this.moveSpeed);
            }
         }
      }
   }

   private int randomize(int min, int max) {
      return this.theRobit.m_217043_().m_188503_(max - min + 1) + min;
   }

   private boolean tryPathTo(Entity target, int x, int y, int z) {
      if ((!(Math.abs(x - target.m_20185_()) < 2.0) || !(Math.abs(z - target.m_20189_()) < 2.0)) && this.canNavigate(new BlockPos(x, y, z))) {
         this.theRobit.m_7678_(x + 0.5, y, z + 0.5, this.theRobit.m_146908_(), this.theRobit.m_146909_());
         this.getNavigator().m_26573_();
         return true;
      } else {
         return false;
      }
   }

   private boolean canNavigate(BlockPos pos) {
      Level world = this.getWorld();
      BlockPathTypes pathnodetype = WalkNodeEvaluator.m_77604_(world, pos.m_122032_());
      if (pathnodetype == BlockPathTypes.WALKABLE) {
         BlockPos blockpos = pos.m_121996_(this.theRobit.m_20183_());
         return world.m_45756_(this.theRobit, this.theRobit.m_20191_().m_82338_(blockpos));
      } else {
         return false;
      }
   }
}
