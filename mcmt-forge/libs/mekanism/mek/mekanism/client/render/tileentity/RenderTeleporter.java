package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter> {
   private static final Map<Direction, MekanismRenderer.Model3D> modelCache = new EnumMap<>(Direction.class);
   private static final Map<Direction, MekanismRenderer.Model3D> rotatedModelCache = new EnumMap<>(Direction.class);

   public static void resetCachedModels() {
      modelCache.clear();
      rotatedModelCache.clear();
   }

   public RenderTeleporter(Context context) {
      super(context);
   }

   protected void render(
      TileEntityTeleporter tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      MekanismRenderer.renderObject(
         this.getOverlayModel(tile.frameDirection(), tile.frameRotated()),
         matrix,
         renderer.m_6299_(Sheets.m_110792_()),
         MekanismRenderer.getColorARGB(tile.getColor(), 0.75F),
         15728880,
         overlayLight,
         RenderResizableCuboid.FaceDisplay.FRONT,
         this.getCamera(),
         tile.m_58899_()
      );
   }

   @Override
   protected String getProfilerSection() {
      return "teleporter";
   }

   private MekanismRenderer.Model3D getOverlayModel(@Nullable Direction direction, boolean rotated) {
      if (direction == null) {
         direction = Direction.UP;
      }

      Map<Direction, MekanismRenderer.Model3D> cache = rotated ? rotatedModelCache : modelCache;
      return cache.computeIfAbsent(
         direction,
         dir -> {
            Axis renderAxis = dir.m_122434_().m_122479_() ? Axis.Y : (rotated ? Axis.X : Axis.Z);
            MekanismRenderer.Model3D model = new MekanismRenderer.Model3D()
               .setTexture(MekanismRenderer.teleporterPortal)
               .setSideRender(side -> side.m_122434_() == renderAxis);
            int min = dir.m_122421_() == AxisDirection.POSITIVE ? 1 : -2;
            int max = dir.m_122421_() == AxisDirection.POSITIVE ? 3 : 0;

            return switch (dir.m_122434_()) {
               case X -> {
                  this.setDimensions(rotated, model::zBounds, model::yBounds);
                  yield model.xBounds(min, max);
               }
               case Y -> {
                  this.setDimensions(rotated, model::zBounds, model::xBounds);
                  yield model.yBounds(min, max);
               }
               case Z -> {
                  this.setDimensions(rotated, model::xBounds, model::yBounds);
                  yield model.zBounds(min, max);
               }
               default -> throw new IncompatibleClassChangeError();
            };
         }
      );
   }

   private void setDimensions(boolean rotated, MekanismRenderer.Model3D.ModelBoundsSetter setter1, MekanismRenderer.Model3D.ModelBoundsSetter setter2) {
      if (rotated) {
         this.setDimensions(false, setter2, setter1);
      } else {
         setter1.set(0.46F, 0.54F);
         setter2.set(0.0F, 1.0F);
      }
   }

   public boolean shouldRenderOffScreen(TileEntityTeleporter tile) {
      return true;
   }

   public boolean shouldRender(TileEntityTeleporter tile, Vec3 camera) {
      return tile.shouldRender && tile.m_58904_() != null && super.m_142756_(tile, camera);
   }
}
