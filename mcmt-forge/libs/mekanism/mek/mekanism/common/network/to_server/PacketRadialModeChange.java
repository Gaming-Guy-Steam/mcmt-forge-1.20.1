package mekanism.common.network.to_server;

import java.util.List;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.Mekanism;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketRadialModeChange implements IMekanismPacket {
   private final List<ResourceLocation> path;
   private final EquipmentSlot slot;
   private final int networkRepresentation;

   public PacketRadialModeChange(EquipmentSlot slot, List<ResourceLocation> path, int networkRepresentation) {
      this.slot = slot;
      this.path = path;
      this.networkRepresentation = networkRepresentation;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         ItemStack stack = player.m_6844_(this.slot);
         if (!stack.m_41619_() && stack.m_41720_() instanceof IGenericRadialModeItem radialModeItem) {
            RadialData<?> radialData = radialModeItem.getRadialData(stack);
            if (radialData != null) {
               for (ResourceLocation path : this.path) {
                  INestedRadialMode nestedData = radialData.fromIdentifier(path);
                  if (nestedData == null || !nestedData.hasNestedData()) {
                     Mekanism.logger.warn("Could not find path ({}) in current radial data.", path);
                     return;
                  }

                  radialData = nestedData.nestedData();
               }

               this.setMode(player, stack, radialModeItem, radialData);
            }
         }
      }
   }

   private <MODE extends IRadialMode> void setMode(Player player, ItemStack stack, IGenericRadialModeItem item, RadialData<MODE> radialData) {
      MODE newMode = radialData.fromNetworkRepresentation(this.networkRepresentation);
      if (newMode != null) {
         item.setMode(stack, player, radialData, newMode);
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.slot);
      buffer.m_236828_(this.path, FriendlyByteBuf::m_130085_);
      buffer.m_130130_(this.networkRepresentation);
   }

   public static PacketRadialModeChange decode(FriendlyByteBuf buffer) {
      EquipmentSlot slot = (EquipmentSlot)buffer.m_130066_(EquipmentSlot.class);
      List<ResourceLocation> path = buffer.m_236845_(FriendlyByteBuf::m_130281_);
      int networkRepresentation = buffer.m_130242_();
      return new PacketRadialModeChange(slot, path, networkRepresentation);
   }
}
