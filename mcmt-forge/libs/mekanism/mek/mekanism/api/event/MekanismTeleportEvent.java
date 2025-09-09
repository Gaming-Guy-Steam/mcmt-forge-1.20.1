package mekanism.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class MekanismTeleportEvent extends EntityTeleportEvent {
   protected MekanismTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
      super(entity, targetX, targetY, targetZ);
   }

   @Cancelable
   public static class MekaTool extends MekanismTeleportEvent {
      private final BlockHitResult targetBlock;
      private final ItemStack mekaTool;

      public MekaTool(Player player, double targetX, double targetY, double targetZ, ItemStack mekaTool, BlockHitResult targetBlock) {
         super(player, targetX, targetY, targetZ);
         this.mekaTool = mekaTool;
         this.targetBlock = targetBlock;
      }

      public Player getEntity() {
         return (Player)super.getEntity();
      }

      public ItemStack getMekaTool() {
         return this.mekaTool;
      }

      public BlockHitResult getTargetBlock() {
         return this.targetBlock;
      }
   }
}
