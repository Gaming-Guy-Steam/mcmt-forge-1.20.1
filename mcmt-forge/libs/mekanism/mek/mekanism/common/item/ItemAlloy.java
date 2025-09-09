package mekanism.common.item;

import mekanism.api.IAlloyInteraction;
import mekanism.api.tier.AlloyTier;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class ItemAlloy extends Item {
   private final AlloyTier tier;

   public ItemAlloy(AlloyTier tier, Properties properties) {
      super(properties);
      this.tier = tier;
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player != null && MekanismConfig.general.transmitterAlloyUpgrade.get()) {
         Level world = context.m_43725_();
         BlockPos pos = context.m_8083_();
         BlockEntity tile = WorldUtils.getTileEntity(world, pos);
         LazyOptional<IAlloyInteraction> capability = CapabilityUtils.getCapability(tile, Capabilities.ALLOY_INTERACTION, context.m_43719_());
         if (capability.isPresent()) {
            if (!world.f_46443_) {
               ((IAlloyInteraction)capability.orElseThrow(MekanismUtils.MISSING_CAP_ERROR)).onAlloyInteraction(player, context.m_43722_(), this.tier);
            }

            return InteractionResult.m_19078_(world.f_46443_);
         }
      }

      return InteractionResult.PASS;
   }

   public AlloyTier getTier() {
      return this.tier;
   }
}
