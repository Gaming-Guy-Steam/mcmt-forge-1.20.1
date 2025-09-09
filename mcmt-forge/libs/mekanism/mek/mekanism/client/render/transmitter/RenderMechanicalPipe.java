package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe> {
   private static final int stages = 100;
   private static final float height = 0.45F;
   private static final float offset = 0.02F;
   private static final Int2ObjectMap<Map<FluidStack, Int2ObjectMap<MekanismRenderer.Model3D>>> cachedLiquids = new Int2ObjectArrayMap(8);

   public RenderMechanicalPipe(Context context) {
      super(context);
   }

   public static void onStitch() {
      cachedLiquids.clear();
   }

   protected void render(
      TileEntityMechanicalPipe tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      MechanicalPipe pipe = tile.getTransmitter();
      FluidNetwork network = pipe.getTransmitterNetwork();
      if (network != null) {
         FluidStack fluidStack = network.lastFluid;
         if (!fluidStack.isEmpty()) {
            float fluidScale = network.currentScale;
            int stage = Math.max(3, ModelRenderer.getStage(fluidStack, 100, fluidScale));
            int glow = MekanismRenderer.calculateGlowLight(light, fluidStack);
            int color = MekanismRenderer.getColorARGB(fluidStack, fluidScale);
            List<String> connectionContents = new ArrayList<>();
            boolean[] renderSides = new boolean[6];
            boolean hasHorizontalSide = false;
            int verticalSides = 0;
            VertexConsumer buffer = renderer.m_6299_(Sheets.m_110792_());
            Camera camera = this.getCamera();

            for (Direction side : EnumUtils.DIRECTIONS) {
               ConnectionType connectionType = pipe.getConnectionType(side);
               if (connectionType == ConnectionType.NORMAL) {
                  MekanismRenderer.renderObject(
                     this.getModel(side, fluidStack, stage),
                     matrix,
                     buffer,
                     color,
                     glow,
                     overlayLight,
                     RenderResizableCuboid.FaceDisplay.FRONT,
                     camera,
                     tile.m_58899_()
                  );
               } else if (connectionType != ConnectionType.NONE) {
                  connectionContents.add(side.m_7912_() + connectionType.m_7912_().toUpperCase(Locale.ROOT));
               }

               renderSides[side.ordinal()] = connectionType != ConnectionType.NORMAL;
               if (connectionType != ConnectionType.NONE) {
                  if (side.m_122434_().m_122479_()) {
                     hasHorizontalSide = true;
                  } else {
                     verticalSides++;
                  }
               }
            }

            boolean renderBase = hasHorizontalSide || verticalSides < 2;
            MekanismRenderer.Model3D model = this.getModel(fluidStack, stage, renderBase);

            for (Direction side : EnumUtils.DIRECTIONS) {
               model.setSideRender(side, renderSides[side.ordinal()] || side.m_122434_().m_122478_() && renderBase && stage != 99);
            }

            MekanismRenderer.renderObject(model, matrix, buffer, color, glow, overlayLight, RenderResizableCuboid.FaceDisplay.FRONT, camera, tile.m_58899_());
            if (!connectionContents.isEmpty()) {
               matrix.m_85836_();
               matrix.m_85837_(0.5, 0.5, 0.5);
               this.renderModel(
                  tile,
                  matrix,
                  buffer,
                  MekanismRenderer.getRed(color),
                  MekanismRenderer.getGreen(color),
                  MekanismRenderer.getBlue(color),
                  MekanismRenderer.getAlpha(color),
                  glow,
                  overlayLight,
                  MekanismRenderer.getFluidTexture(fluidStack, MekanismRenderer.FluidTextureType.STILL),
                  connectionContents
               );
               matrix.m_85849_();
            }
         }
      }
   }

   @Override
   protected String getProfilerSection() {
      return "mechanicalPipe";
   }

   protected boolean shouldRenderTransmitter(TileEntityMechanicalPipe tile, Vec3 camera) {
      if (super.shouldRenderTransmitter(tile, camera)) {
         MechanicalPipe pipe = tile.getTransmitter();
         if (pipe.hasTransmitterNetwork()) {
            FluidNetwork network = pipe.getTransmitterNetwork();
            return !network.lastFluid.isEmpty() && !network.fluidTank.isEmpty() && network.currentScale > 0.0F;
         }
      }

      return false;
   }

   private MekanismRenderer.Model3D getModel(FluidStack fluid, int stage, boolean hasSides) {
      return this.getModel(null, fluid, stage, hasSides);
   }

   private MekanismRenderer.Model3D getModel(Direction side, FluidStack fluid, int stage) {
      return this.getModel(side, fluid, stage, false);
   }

   private MekanismRenderer.Model3D getModel(@Nullable Direction side, FluidStack fluid, int stage, boolean renderBase) {
      int sideOrdinal;
      if (side == null) {
         sideOrdinal = renderBase ? 7 : 6;
      } else {
         sideOrdinal = side.ordinal();
      }

      return (MekanismRenderer.Model3D)((Map)cachedLiquids.computeIfAbsent(sideOrdinal, s -> new HashMap()))
         .computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap())
         .computeIfAbsent(
            stage,
            s -> {
               float stageRatio = s / 100.0F * 0.45F;
               MekanismRenderer.Model3D model = new MekanismRenderer.Model3D()
                  .setTexture(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
               if (side == null) {
                  float min;
                  float max;
                  if (renderBase) {
                     min = 0.27F;
                     max = 0.73F;
                  } else {
                     min = 0.5F - stageRatio / 2.0F;
                     max = 0.5F + stageRatio / 2.0F;
                  }

                  return model.xBounds(min, max).yBounds(0.27F, 0.27F + stageRatio).zBounds(min, max);
               } else {
                  model.setSideRender(side, false).setSideRender(side.m_122424_(), false);
                  if (side.m_122434_().m_122479_()) {
                     model.yBounds(0.27F, 0.27F + stageRatio);
                     return side.m_122434_() == Axis.Z
                        ? setHorizontalBounds(side, model::xBounds, model::zBounds)
                        : setHorizontalBounds(side, model::zBounds, model::xBounds);
                  } else {
                     float min = 0.5F - stageRatio / 2.0F;
                     float max = 0.5F + stageRatio / 2.0F;
                     model.xBounds(min, max).zBounds(min, max);
                     return side == Direction.DOWN ? model.yBounds(0.0F, 0.27F) : model.yBounds(0.27F + stageRatio, 1.0F);
                  }
               }
            }
         );
   }

   private static MekanismRenderer.Model3D setHorizontalBounds(
      Direction horizontal, MekanismRenderer.Model3D.ModelBoundsSetter axisBased, MekanismRenderer.Model3D.ModelBoundsSetter directionBased
   ) {
      axisBased.set(0.27F, 0.73F);
      return horizontal.m_122421_() == AxisDirection.POSITIVE ? directionBased.set(0.73F, 1.0F) : directionBased.set(0.0F, 0.27F);
   }
}
