package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.api.RelativeSide;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.MekanismISTER;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class RenderEnergyCubeItem extends MekanismISTER {
   public static final RenderEnergyCubeItem RENDERER = new RenderEnergyCubeItem();
   private ModelEnergyCore core;

   @Override
   public void m_6213_(@NotNull ResourceManager resourceManager) {
      this.core = new ModelEnergyCore(this.getEntityModels());
   }

   @Override
   public void m_108829_(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext displayContext,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight
   ) {
      EnergyCubeTier tier = ((ItemBlockEnergyCube)stack.m_41720_()).getTier();
      TileEntityEnergyCube.CubeSideState[] sideStates = new TileEntityEnergyCube.CubeSideState[EnumUtils.SIDES.length];
      CompoundTag configData = ItemDataUtils.getDataMapIfPresent(stack);
      if (configData != null && configData.m_128425_("componentConfig", 10)) {
         CompoundTag sideConfig = configData.m_128469_("componentConfig").m_128469_("config" + TransmissionType.ENERGY.ordinal());

         for (RelativeSide side : EnumUtils.SIDES) {
            DataType dataType = DataType.byIndexStatic(sideConfig.m_128451_("side" + side.ordinal()));
            TileEntityEnergyCube.CubeSideState state = TileEntityEnergyCube.CubeSideState.INACTIVE;
            if (dataType != DataType.NONE) {
               state = dataType.canOutput() ? TileEntityEnergyCube.CubeSideState.ACTIVE_LIT : TileEntityEnergyCube.CubeSideState.ACTIVE_UNLIT;
            }

            sideStates[side.ordinal()] = state;
         }
      } else {
         for (RelativeSide side : EnumUtils.SIDES) {
            sideStates[side.ordinal()] = tier != EnergyCubeTier.CREATIVE && side != RelativeSide.FRONT
               ? TileEntityEnergyCube.CubeSideState.ACTIVE_UNLIT
               : TileEntityEnergyCube.CubeSideState.ACTIVE_LIT;
         }
      }

      ModelData modelData = ModelData.builder().with(TileEntityEnergyCube.SIDE_STATE_PROPERTY, sideStates).build();
      this.renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, modelData);
      double energyPercentage = StorageUtils.getStoredEnergyFromNBT(stack).divideToLevel(tier.getMaxEnergy());
      if (energyPercentage > 0.0) {
         float ticks = Minecraft.m_91087_().f_91060_.f_109477_ + MekanismRenderer.getPartialTick();
         float scaledTicks = 4.0F * ticks;
         matrix.m_85836_();
         matrix.m_85837_(0.5, 0.5, 0.5);
         matrix.m_85841_(0.4F, 0.4F, 0.4F);
         matrix.m_85837_(0.0, Math.sin(Math.toRadians(3.0F * ticks)) / 7.0, 0.0);
         matrix.m_252781_(Axis.f_252436_.m_252977_(scaledTicks));
         matrix.m_252781_(RenderEnergyCube.coreVec.m_252977_(36.0F + scaledTicks));
         this.core.render(matrix, renderer, 15728880, overlayLight, tier.getBaseTier(), (float)energyPercentage);
         matrix.m_85849_();
      }
   }
}
