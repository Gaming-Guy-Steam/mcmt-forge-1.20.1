package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.item.MekanismISTER;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RenderFluidTankItem extends MekanismISTER {
   public static final RenderFluidTankItem RENDERER = new RenderFluidTankItem();

   @Override
   public void m_6213_(@NotNull ResourceManager resourceManager) {
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
      FluidTankTier tier = ((ItemBlockFluidTank)stack.m_41720_()).getTier();
      FluidStack fluid = StorageUtils.getStoredFluidFromNBT(stack);
      if (!fluid.isEmpty()) {
         float fluidScale = (float)fluid.getAmount() / tier.getStorage();
         if (fluidScale > 0.0F) {
            MekanismRenderer.renderObject(
               RenderFluidTank.getFluidModel(fluid, fluidScale),
               matrix,
               renderer.m_6299_(Sheets.m_110792_()),
               MekanismRenderer.getColorARGB(fluid, fluidScale),
               MekanismRenderer.calculateGlowLight(light, fluid),
               overlayLight,
               RenderResizableCuboid.FaceDisplay.FRONT,
               this.getCamera()
            );
         }
      }

      this.renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, ModelData.EMPTY);
   }
}
