package mekanism.common.item;

import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemDosimeter extends Item {
   public ItemDosimeter(Properties properties) {
      super(properties.m_41487_(1).m_41497_(Rarity.UNCOMMON));
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!player.m_6144_()) {
         if (!world.f_46443_) {
            player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(cap -> {
               this.sendDosimeterLevel(cap, player, MekanismLang.RADIATION_EXPOSURE);
               CriteriaTriggers.f_145090_.m_163865_((ServerPlayer)player, stack);
            });
         }

         return InteractionResultHolder.m_19092_(stack, world.f_46443_);
      } else {
         return InteractionResultHolder.m_19098_(stack);
      }
   }

   @NotNull
   public InteractionResult m_6880_(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
      if (!player.m_6144_()) {
         if (!player.m_9236_().f_46443_) {
            entity.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(cap -> this.sendDosimeterLevel(cap, player, MekanismLang.RADIATION_EXPOSURE_ENTITY));
         }

         return InteractionResult.m_19078_(player.m_9236_().f_46443_);
      } else {
         return InteractionResult.PASS;
      }
   }

   private void sendDosimeterLevel(IRadiationEntity cap, Player player, ILangEntry doseLangEntry) {
      double radiation = IRadiationManager.INSTANCE.isRadiationEnabled() ? cap.getRadiation() : 0.0;
      EnumColor severityColor = RadiationManager.RadiationScale.getSeverityColor(radiation);
      player.m_213846_(
         doseLangEntry.translateColored(EnumColor.GRAY, severityColor, UnitDisplayUtils.getDisplayShort(radiation, UnitDisplayUtils.RadiationUnit.SV, 3))
      );
      if (MekanismConfig.common.enableDecayTimers.get() && radiation > 1.0E-5) {
         player.m_213846_(
            MekanismLang.RADIATION_DECAY_TIME
               .translateColored(EnumColor.GRAY, new Object[]{severityColor, TextUtils.getHoursMinutes(RadiationManager.get().getDecayTime(radiation, false))})
         );
      }
   }
}
