package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.item.interfaces.IDroppableContents;
import mekanism.common.lib.inventory.TileTransitRequest;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtils {
   private InventoryUtils() {
   }

   public static void dropItemContents(ItemEntity entity, DamageSource source) {
      ItemStack stack = entity.m_32055_();
      if (!entity.m_9236_().f_46443_ && !stack.m_41619_() && stack.m_41720_() instanceof IDroppableContents inventory && inventory.canContentsDrop(stack)) {
         boolean shouldDrop;
         if (source.m_7639_() instanceof Player player) {
            shouldDrop = ISecurityUtils.INSTANCE.canAccess(player, stack);
         } else {
            shouldDrop = ISecurityUtils.INSTANCE.canAccess(null, stack, false);
         }

         if (shouldDrop) {
            for (IInventorySlot slot : inventory.getDroppedSlots(stack)) {
               if (!slot.isEmpty()) {
                  dropStack(
                     slot.getStack().m_41777_(),
                     slotStack -> entity.m_9236_()
                        .m_7967_(new ItemEntity(entity.m_9236_(), entity.m_20185_(), entity.m_20186_(), entity.m_20189_(), slotStack))
                  );
               }
            }
         }
      }
   }

   public static void dropStack(ItemStack stack, Consumer<ItemStack> dropper) {
      int count = stack.m_41613_();
      int max = stack.m_41741_();
      if (count <= max) {
         dropper.accept(stack);
      } else {
         while (count > max) {
            dropper.accept(stack.m_255036_(max));
            count -= max;
         }

         if (count > 0) {
            dropper.accept(stack.m_255036_(count));
         }
      }
   }

   public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
      return !toInsert.m_41619_() && !inSlot.m_41619_() ? ItemHandlerHelper.canItemStacksStack(inSlot, toInsert) : true;
   }

   @Nullable
   public static IItemHandler assertItemHandler(String desc, BlockEntity tile, Direction side) {
      Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, ForgeCapabilities.ITEM_HANDLER, side).resolve();
      if (capability.isPresent()) {
         return capability.get();
      } else {
         Mekanism.logger.warn("'{}' was wrapped around a non-IItemHandler inventory. This should not happen!", desc, new Exception());
         if (tile == null) {
            Mekanism.logger.warn(" - null tile");
         } else {
            Mekanism.logger.warn(" - details: {} {}", tile, tile.m_58899_());
         }

         return null;
      }
   }

   public static boolean isItemHandler(BlockEntity tile, Direction side) {
      return CapabilityUtils.getCapability(tile, ForgeCapabilities.ITEM_HANDLER, side).isPresent();
   }

   public static TileTransitRequest getEjectItemMap(BlockEntity tile, Direction side, List<IInventorySlot> slots) {
      return getEjectItemMap(new TileTransitRequest(tile, side), slots);
   }

   @Contract("_, _ -> param1")
   public static <REQUEST extends TileTransitRequest> REQUEST getEjectItemMap(REQUEST request, List<IInventorySlot> slots) {
      List<IInventorySlot> shuffled = new ArrayList<>(slots);
      Collections.shuffle(shuffled);

      for (IInventorySlot slot : shuffled) {
         ItemStack simulatedExtraction = slot.extractItem(slot.getCount(), Action.SIMULATE, AutomationType.EXTERNAL);
         if (!simulatedExtraction.m_41619_()) {
            request.addItem(simulatedExtraction, slots.indexOf(slot));
         }
      }

      return request;
   }

   public static ItemStack insertItem(List<? extends IInventorySlot> slots, @NotNull ItemStack stack, Action action, AutomationType automationType) {
      stack = insertItem(slots, stack, true, false, action, automationType);
      return insertItem(slots, stack, false, false, action, automationType);
   }

   @NotNull
   public static ItemStack insertItem(
      List<? extends IInventorySlot> slots, @NotNull ItemStack stack, boolean ignoreEmpty, boolean checkAll, Action action, AutomationType automationType
   ) {
      if (stack.m_41619_()) {
         return stack;
      } else {
         for (IInventorySlot slot : slots) {
            if (checkAll || ignoreEmpty != slot.isEmpty()) {
               stack = slot.insertItem(stack, action, automationType);
               if (stack.m_41619_()) {
                  break;
               }
            }
         }

         return stack;
      }
   }
}
