package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank> {
   private static final Map<FluidStack, Int2ObjectMap<MekanismRenderer.Model3D>> cachedCenterFluids = new HashMap<>();
   private static final Map<FluidStack, Int2ObjectMap<MekanismRenderer.Model3D>> cachedValveFluids = new HashMap<>();
   private static final int stages = 1400;

   public RenderFluidTank(Context context) {
      super(context);
   }

   public static void resetCachedModels() {
      cachedCenterFluids.clear();
      cachedValveFluids.clear();
   }

   protected void render(
      TileEntityFluidTank tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      FluidStack fluid = tile.fluidTank.getFluid();
      float fluidScale = tile.prevScale;
      Lazy<VertexConsumer> buffer = Lazy.of(() -> renderer.m_6299_(Sheets.m_110792_()));
      if (!fluid.isEmpty() && fluidScale > 0.0F) {
         MekanismRenderer.renderObject(
            getFluidModel(fluid, fluidScale),
            matrix,
            (VertexConsumer)buffer.get(),
            MekanismRenderer.getColorARGB(fluid, fluidScale),
            MekanismRenderer.calculateGlowLight(light, fluid),
            overlayLight,
            RenderResizableCuboid.FaceDisplay.FRONT,
            this.getCamera(),
            tile.m_58899_()
         );
      }

      if (!tile.valveFluid.isEmpty() && !MekanismUtils.lighterThanAirGas(tile.valveFluid)) {
         MekanismRenderer.renderObject(
            this.getValveModel(tile.valveFluid, fluidScale),
            matrix,
            (VertexConsumer)buffer.get(),
            MekanismRenderer.getColorARGB(tile.valveFluid),
            MekanismRenderer.calculateGlowLight(light, tile.valveFluid),
            overlayLight,
            RenderResizableCuboid.FaceDisplay.FRONT,
            this.getCamera(),
            tile.m_58899_()
         );
      }
   }

   @Override
   protected String getProfilerSection() {
      return "fluidTank";
   }

   private MekanismRenderer.Model3D getValveModel(@NotNull FluidStack fluid, float fluidScale) {
      return (MekanismRenderer.Model3D)cachedValveFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap())
         .computeIfAbsent(
            Math.min(1399, (int)(fluidScale * 1399.0F)),
            stage -> new MekanismRenderer.Model3D()
               .setSideRender(side -> side.m_122434_().m_122479_())
               .prepFlowing(fluid)
               .xBounds(0.3225F, 0.6775F)
               .yBounds(0.0625F + 0.875F * (stage / 1400.0F), 0.9375F)
               .zBounds(0.3225F, 0.6775F)
         );
   }

   public static MekanismRenderer.Model3D getFluidModel(@NotNull FluidStack fluid, float fluidScale) {
      return (MekanismRenderer.Model3D)cachedCenterFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap())
         .computeIfAbsent(
            ModelRenderer.getStage(fluid, 1400, fluidScale),
            stage -> new MekanismRenderer.Model3D()
               .setTexture(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL))
               .setSideRender(Direction.DOWN, false)
               .setSideRender(Direction.UP, stage < 1400)
               .xBounds(0.135F, 0.865F)
               .yBounds(0.0625F, 0.0625F + 0.875F * (stage / 1400.0F))
               .zBounds(0.135F, 0.865F)
         );
   }
}
