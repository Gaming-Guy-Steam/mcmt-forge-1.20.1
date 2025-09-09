package mekanism.common.item;

import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemGeigerCounter extends Item {
   public ItemGeigerCounter(Properties props) {
      super(props.m_41487_(1).m_41497_(Rarity.UNCOMMON));
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!player.m_6144_()) {
         if (!world.m_5776_()) {
            RadiationManager.LevelAndMaxMagnitude levelAndMaxMagnitude = RadiationManager.get().getRadiationLevelAndMaxMagnitude(player);
            double magnitude = levelAndMaxMagnitude.level();
            EnumColor severityColor = RadiationManager.RadiationScale.getSeverityColor(magnitude);
            player.m_213846_(
               MekanismLang.RADIATION_EXPOSURE
                  .translateColored(
                     EnumColor.GRAY, new Object[]{severityColor, UnitDisplayUtils.getDisplayShort(magnitude, UnitDisplayUtils.RadiationUnit.SVH, 3)}
                  )
            );
            if (MekanismConfig.common.enableDecayTimers.get() && magnitude > 1.0E-7) {
               player.m_213846_(
                  MekanismLang.RADIATION_DECAY_TIME
                     .translateColored(
                        EnumColor.GRAY,
                        new Object[]{severityColor, TextUtils.getHoursMinutes(RadiationManager.get().getDecayTime(levelAndMaxMagnitude.maxMagnitude(), true))}
                     )
               );
            }

            CriteriaTriggers.f_145090_.m_163865_((ServerPlayer)player, stack);
         }

         return InteractionResultHolder.m_19092_(stack, world.f_46443_);
      } else {
         return InteractionResultHolder.m_19098_(stack);
      }
   }
}
