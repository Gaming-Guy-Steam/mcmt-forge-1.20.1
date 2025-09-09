package mekanism.common;

import mekanism.api.text.EnumColor;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockMekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.capability.DefaultRadiationEntity;
import mekanism.common.network.to_client.PacketPlayerData;
import mekanism.common.network.to_client.PacketRadiationData;
import mekanism.common.network.to_client.PacketResetPlayerClient;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;

public class CommonPlayerTracker {
   private static final Component ALPHA_WARNING = MekanismLang.LOG_FORMAT
      .translateColored(
         EnumColor.RED,
         new Object[]{
            MekanismLang.MEKANISM,
            EnumColor.GRAY,
            MekanismLang.ALPHA_WARNING
               .translate(
                  new Object[]{
                     EnumColor.INDIGO,
                     ChatFormatting.UNDERLINE,
                     new ClickEvent(Action.OPEN_URL, "https://github.com/mekanism/Mekanism#alpha-status"),
                     MekanismLang.ALPHA_WARNING_HERE
                  }
               )
         }
      );

   public CommonPlayerTracker() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void onPlayerLoginEvent(PlayerLoggedInEvent event) {
      Player player = event.getEntity();
      if (!player.m_9236_().f_46443_) {
         ServerPlayer serverPlayer = (ServerPlayer)player;
         Mekanism.packetHandler().sendTo(new PacketSecurityUpdate(), serverPlayer);
         MekanismCriteriaTriggers.LOGGED_IN.m_222618_(serverPlayer);
      }
   }

   @SubscribeEvent
   public void onPlayerLogoutEvent(PlayerLoggedOutEvent event) {
      Player player = event.getEntity();
      Mekanism.playerState.clearPlayer(player.m_20148_(), false);
      Mekanism.playerState.clearPlayerServerSideOnly(player.m_20148_());
   }

   @SubscribeEvent
   public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event) {
      ServerPlayer player = (ServerPlayer)event.getEntity();
      Mekanism.playerState.clearPlayer(player.m_20148_(), false);
      Mekanism.playerState.reapplyServerSideOnly(player);
      player.getCapability(Capabilities.RADIATION_ENTITY)
         .ifPresent(c -> Mekanism.packetHandler().sendTo(PacketRadiationData.createPlayer(c.getRadiation()), player));
      RadiationManager.get().updateClientRadiation(player);
   }

   @SubscribeEvent
   public void onPlayerStartTrackingEvent(StartTracking event) {
      if (event.getTarget() instanceof Player player && event.getEntity() instanceof ServerPlayer serverPlayer) {
         Mekanism.packetHandler().sendTo(new PacketPlayerData(player.m_20148_()), serverPlayer);
      }
   }

   @SubscribeEvent
   public void attachCaps(AttachCapabilitiesEvent<Entity> event) {
      if (event.getObject() instanceof LivingEntity) {
         DefaultRadiationEntity.Provider radiationProvider = new DefaultRadiationEntity.Provider();
         event.addCapability(DefaultRadiationEntity.Provider.NAME, radiationProvider);
         event.addListener(radiationProvider::invalidate);
      }
   }

   @SubscribeEvent
   public void cloneEvent(Clone event) {
      event.getOriginal().reviveCaps();
      event.getOriginal()
         .getCapability(Capabilities.RADIATION_ENTITY)
         .ifPresent(cap -> event.getEntity().getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> c.deserializeNBT((CompoundTag)cap.serializeNBT())));
      event.getOriginal().invalidateCaps();
   }

   @SubscribeEvent
   public void respawnEvent(PlayerRespawnEvent event) {
      ServerPlayer player = (ServerPlayer)event.getEntity();
      player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> {
         if (!event.isEndConquered()) {
            c.set(1.0E-7);
         }

         Mekanism.packetHandler().sendTo(PacketRadiationData.createPlayer(c.getRadiation()), player);
      });
      RadiationManager.get().updateClientRadiation(player);
      Mekanism.packetHandler().sendToAll(new PacketResetPlayerClient(player.m_20148_()));
   }

   @SubscribeEvent
   public void rightClickEvent(RightClickBlock event) {
      ItemStack itemInHand = event.getEntity().m_21120_(event.getHand());
      if (itemInHand.m_204117_(MekanismTags.Items.CONFIGURATORS) && !itemInHand.m_150930_(MekanismItems.CONFIGURATOR.m_5456_())) {
         Block block = event.getLevel().m_8055_(event.getPos()).m_60734_();
         if (block instanceof BlockMekanism || block instanceof BlockBounding) {
            event.setUseBlock(Result.ALLOW);
         }
      } else if (event.getEntity().m_6144_() && event.getLevel().m_8055_(event.getPos()).m_60734_() instanceof BlockCardboardBox) {
         event.setUseBlock(Result.ALLOW);
         event.setUseItem(Result.DENY);
      }
   }
}
