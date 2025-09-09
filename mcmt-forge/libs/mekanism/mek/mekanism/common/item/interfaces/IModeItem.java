package mekanism.common.item.interfaces;

import java.util.function.Supplier;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.common.Mekanism;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.network.to_client.PacketShowModeChange;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModeItem {
   void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange);

   default boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
      return slotType == EquipmentSlot.MAINHAND || slotType == EquipmentSlot.OFFHAND;
   }

   @Nullable
   default Component getScrollTextComponent(@NotNull ItemStack stack) {
      return null;
   }

   static boolean isModeItem(@NotNull Player player, @NotNull EquipmentSlot slotType) {
      return isModeItem(player, slotType, true);
   }

   static boolean isModeItem(@NotNull Player player, @NotNull EquipmentSlot slotType, boolean allowRadial) {
      return isModeItem(player.m_6844_(slotType), slotType, allowRadial);
   }

   static boolean isModeItem(@NotNull ItemStack stack, @NotNull EquipmentSlot slotType) {
      return isModeItem(stack, slotType, true);
   }

   static boolean isModeItem(@NotNull ItemStack stack, @NotNull EquipmentSlot slotType, boolean allowRadial) {
      return !stack.m_41619_() && stack.m_41720_() instanceof IModeItem modeItem && modeItem.supportsSlotType(stack, slotType)
         ? !(!allowRadial && modeItem instanceof IGenericRadialModeItem radialModeItem) || radialModeItem.getRadialData(stack) == null
         : false;
   }

   static void displayModeChange(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         Mekanism.packetHandler().sendTo(PacketShowModeChange.INSTANCE, serverPlayer);
      } else {
         MekanismStatusOverlay.INSTANCE.setTimer();
      }
   }

   public static enum DisplayChange {
      NONE,
      MAIN_HAND,
      OTHER;

      public void sendMessage(Player player, Supplier<Component> message) {
         if (this == MAIN_HAND) {
            IModeItem.displayModeChange(player);
         } else if (this == OTHER) {
            player.m_213846_(MekanismUtils.logFormat(message.get()));
         }
      }
   }
}
