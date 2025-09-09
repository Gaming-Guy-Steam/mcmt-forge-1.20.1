package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StackUtils {
   private StackUtils() {
   }

   public static ItemStack size(ItemStack stack, int size) {
      return size <= 0 ? ItemStack.f_41583_ : stack.m_255036_(size);
   }

   public static List<ItemStack> merge(@NotNull List<IInventorySlot> orig, @NotNull List<IInventorySlot> toAdd) {
      List<ItemStack> rejects = new ArrayList<>();
      merge(orig, toAdd, rejects);
      return rejects;
   }

   public static void merge(@NotNull List<IInventorySlot> orig, @NotNull List<IInventorySlot> toAdd, List<ItemStack> rejects) {
      StorageUtils.validateSizeMatches(orig, toAdd, "slot");

      for (int i = 0; i < toAdd.size(); i++) {
         IInventorySlot toAddSlot = toAdd.get(i);
         if (!toAddSlot.isEmpty()) {
            IInventorySlot origSlot = orig.get(i);
            ItemStack toAddStack = toAddSlot.getStack();
            if (origSlot.isEmpty()) {
               int max = origSlot.getLimit(toAddStack);
               if (toAddStack.m_41613_() <= max) {
                  origSlot.setStack(toAddStack);
               } else {
                  origSlot.setStack(toAddStack.m_255036_(max));
                  addStack(rejects, toAddStack.m_255036_(toAddStack.m_41613_() - max));
               }
            } else if (ItemHandlerHelper.canItemStacksStack(origSlot.getStack(), toAddStack)) {
               int added = origSlot.growStack(toAddStack.m_41613_(), Action.EXECUTE);
               addStack(rejects, toAddStack.m_255036_(toAddStack.m_41613_() - added));
            } else {
               addStack(rejects, toAddStack.m_41777_());
            }
         }
      }
   }

   private static void addStack(List<ItemStack> stacks, ItemStack stack) {
      if (!stack.m_41619_()) {
         for (ItemStack existingStack : stacks) {
            int needed = existingStack.m_41741_() - existingStack.m_41613_();
            if (needed > 0 && ItemHandlerHelper.canItemStacksStack(existingStack, stack)) {
               int toAdd = Math.min(needed, stack.m_41613_());
               existingStack.m_41769_(toAdd);
               stack.m_41774_(toAdd);
               break;
            }
         }

         if (!stack.m_41619_()) {
            int count = stack.m_41613_();
            int max = stack.m_41741_();
            if (count > max) {
               int excess = count % max;
               int stacksToAdd = count / max;
               if (excess > 0) {
                  stacks.add(stack.m_255036_(excess));
               }

               ItemStack maxSize = stack.m_255036_(max);
               stacks.add(maxSize);

               for (int i = 1; i < stacksToAdd; i++) {
                  stacks.add(maxSize.m_41777_());
               }
            } else {
               stacks.add(stack);
            }
         }
      }
   }

   @Nullable
   public static BlockState getStateForPlacement(ItemStack stack, BlockPos pos, Player player) {
      return Block.m_49814_(stack.m_41720_())
         .m_5573_(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.f_82478_, Direction.UP, pos, false))));
   }
}
