package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.IEventListener;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.data.EnergyData;
import mcp.mobius.waila.api.data.FluidData;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;

public class MekanismWTHITPlugin implements IWailaPlugin {
   static final ResourceLocation MEK_DATA = Mekanism.rl("wthit_data");

   public void register(IRegistrar registration) {
      registration.addBlockData(WTHITDataProvider.INSTANCE, BlockEntity.class);
      registration.addEntityData(WTHITEntityDataProvider.INSTANCE, EntityRobit.class);
      registration.addConfig(LookingAtUtils.ENERGY, true);
      registration.addConfig(LookingAtUtils.FLUID, true);
      registration.addConfig(LookingAtUtils.GAS, true);
      registration.addConfig(LookingAtUtils.INFUSE_TYPE, true);
      registration.addConfig(LookingAtUtils.PIGMENT, true);
      registration.addConfig(LookingAtUtils.SLURRY, true);
      registration.addComponent(WTHITTooltipRenderer.INSTANCE, TooltipPosition.BODY, EntityRobit.class);
      registration.addComponent(WTHITTooltipRenderer.INSTANCE, TooltipPosition.BODY, Block.class);
      registration.addDataType(MEK_DATA, WTHITLookingAtHelper.class, WTHITLookingAtHelper.SERIALIZER);
      registration.addEventListener(new IEventListener() {
         public void onHandleTooltip(ITooltip tooltip, ICommonAccessor accessor, IPluginConfig config) {
            if (tooltip.getLine(MekanismWTHITPlugin.MEK_DATA) != null) {
               if (tooltip.getLine(EnergyData.ID) != null) {
                  tooltip.setLine(EnergyData.ID);
               }

               if (tooltip.getLine(FluidData.ID) != null) {
                  tooltip.setLine(FluidData.ID);
               }
            }
         }
      });
      registration.addOverride(new IBlockComponentProvider() {
         @Nullable
         public BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
            if (accessor.getHitResult() instanceof BlockHitResult result && result.m_6662_() != Type.MISS) {
               Level level = accessor.getWorld();
               BlockPos mainPos = BlockBounding.getMainBlockPos(level, result.m_82425_());
               if (mainPos != null) {
                  return level.m_8055_(mainPos);
               }
            }

            return null;
         }
      }, BlockBounding.class);
   }
}
