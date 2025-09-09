package mekanism.common.integration.lookingat.jade;

import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class MekanismJadePlugin implements IWailaPlugin {
   public void register(IWailaCommonRegistration registration) {
      registration.registerBlockDataProvider(JadeDataProvider.INSTANCE, BlockEntity.class);
      registration.registerEntityDataProvider(JadeEntityDataProvider.INSTANCE, EntityRobit.class);
   }

   public void registerClient(IWailaClientRegistration registration) {
      registration.addConfig(LookingAtUtils.ENERGY, true);
      registration.addConfig(LookingAtUtils.FLUID, true);
      registration.addConfig(LookingAtUtils.GAS, true);
      registration.addConfig(LookingAtUtils.INFUSE_TYPE, true);
      registration.addConfig(LookingAtUtils.PIGMENT, true);
      registration.addConfig(LookingAtUtils.SLURRY, true);
      registration.registerEntityComponent(JadeTooltipRenderer.INSTANCE, EntityRobit.class);
      registration.registerBlockComponent(JadeTooltipRenderer.INSTANCE, Block.class);
      registration.registerBlockComponent(new IBlockComponentProvider() {
         public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getServerData().m_128425_("mekData", 9)) {
               tooltip.remove(Identifiers.UNIVERSAL_ENERGY_STORAGE);
               tooltip.remove(Identifiers.UNIVERSAL_FLUID_STORAGE);
            }
         }

         public ResourceLocation getUid() {
            return JadeConstants.REMOVE_BUILTIN;
         }

         public int getDefaultPriority() {
            return 10000;
         }
      }, Block.class);
      registration.addRayTraceCallback(
         (hitResult, accessor, originalAccessor) -> {
            if (accessor instanceof BlockAccessor target && target.getBlockState().m_60734_() instanceof BlockBounding) {
               Level level = target.getLevel();
               BlockHitResult blockHitResult = (BlockHitResult)target.getHitResult();
               BlockPos mainPos = BlockBounding.getMainBlockPos(level, blockHitResult.m_82425_());
               if (mainPos != null) {
                  return registration.blockAccessor()
                     .from(target)
                     .hit(blockHitResult.m_82430_(mainPos))
                     .blockState(level.m_8055_(mainPos))
                     .blockEntity(WorldUtils.getTileEntity(level, mainPos))
                     .build();
               }
            }

            return accessor;
         }
      );
   }
}
