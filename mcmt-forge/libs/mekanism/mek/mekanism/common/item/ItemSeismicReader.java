package mekanism.common.item;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemSeismicReader extends ItemEnergized {
   public ItemSeismicReader(Properties properties) {
      super(MekanismConfig.gear.seismicReaderChargeRate, MekanismConfig.gear.seismicReaderMaxEnergy, properties.m_41497_(Rarity.UNCOMMON));
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
         tooltip.add(MekanismLang.DESCRIPTION_SEISMIC_READER.translate(new Object[0]));
      } else if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         super.m_7373_(stack, world, tooltip, flag);
      } else {
         tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
         tooltip.add(
            MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, new Object[]{EnumColor.AQUA, MekanismKeyHandler.descriptionKey.m_90863_()})
         );
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (world.f_46443_) {
         return InteractionResultHolder.m_19090_(stack);
      } else {
         if (!WorldUtils.isChunkVibrated(new ChunkPos(player.m_20183_()), player.m_9236_())) {
            player.m_213846_(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_VIBRATIONS));
         } else {
            if (!player.m_7500_()) {
               IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
               FloatingLong energyUsage = MekanismConfig.gear.seismicReaderEnergyUsage.get();
               if (energyContainer == null || energyContainer.extract(energyUsage, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyUsage)) {
                  player.m_213846_(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NEEDS_ENERGY));
                  return InteractionResultHolder.m_19096_(stack);
               }

               energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.MANUAL);
            }

            ServerPlayer serverPlayer = (ServerPlayer)player;
            MekanismCriteriaTriggers.VIEW_VIBRATIONS.trigger(serverPlayer);
            MekanismContainerTypes.SEISMIC_READER.tryOpenGui(serverPlayer, hand, stack);
         }

         return InteractionResultHolder.m_19096_(stack);
      }
   }
}
