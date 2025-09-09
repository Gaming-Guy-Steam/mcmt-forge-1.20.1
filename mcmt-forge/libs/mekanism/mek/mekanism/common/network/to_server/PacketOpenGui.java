package mekanism.common.network.to_server;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketOpenGui implements IMekanismPacket {
   private final PacketOpenGui.GuiType type;

   public PacketOpenGui(PacketOpenGui.GuiType type) {
      this.type = type;
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null && this.type.shouldOpenForPlayer.test(player)) {
         NetworkHooks.openScreen(player, this.type.containerSupplier.get());
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.type);
   }

   public static PacketOpenGui decode(FriendlyByteBuf buffer) {
      return new PacketOpenGui((PacketOpenGui.GuiType)buffer.m_130066_(PacketOpenGui.GuiType.class));
   }

   public static enum GuiType {
      MODULE_TWEAKER(
         () -> new ContainerProvider(MekanismLang.MODULE_TWEAKER, (id, inv, player) -> MekanismContainerTypes.MODULE_TWEAKER.get().m_39985_(id, inv)),
         ModuleTweakerContainer::hasTweakableItem
      );

      private final Supplier<MenuProvider> containerSupplier;
      private final Predicate<Player> shouldOpenForPlayer;

      private GuiType(Supplier<MenuProvider> containerSupplier) {
         this(containerSupplier, ConstantPredicates.alwaysTrue());
      }

      private GuiType(Supplier<MenuProvider> containerSupplier, Predicate<Player> shouldOpenForPlayer) {
         this.containerSupplier = containerSupplier;
         this.shouldOpenForPlayer = shouldOpenForPlayer;
      }
   }
}
