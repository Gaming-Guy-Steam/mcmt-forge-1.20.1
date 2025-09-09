package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderDimensionalStabilizer extends MekanismTileEntityRenderer<TileEntityDimensionalStabilizer> {
   private static final MekanismRenderer.LazyModel model = new MekanismRenderer.LazyModel(
      () -> new MekanismRenderer.Model3D()
         .setTexture(MekanismRenderer.whiteIcon)
         .bounds(0.0F, 1.0F)
         .setSideRender(direction -> direction.m_122434_().m_122479_())
   );
   private static final int[] colors = new int[EnumUtils.DIRECTIONS.length];

   public static void resetCachedVisuals() {
      model.reset();
   }

   public RenderDimensionalStabilizer(Context context) {
      super(context);
   }

   protected void render(
      TileEntityDimensionalStabilizer stabilizer,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      boolean[][][] allRenderSides = new boolean[5][5][5];

      for (int x = 0; x < allRenderSides.length; x++) {
         boolean[][] rowRenderSides = allRenderSides[x];

         for (int z = 0; z < rowRenderSides.length; z++) {
            if (stabilizer.isChunkLoadingAt(x, z)) {
               boolean[] renderSides = rowRenderSides[z];
               Arrays.fill(renderSides, true);
               if (x > 0) {
                  boolean[] previousRenderSides = allRenderSides[x - 1][z];
                  if (previousRenderSides[Direction.EAST.m_122416_()]) {
                     renderSides[Direction.WEST.m_122416_()] = false;
                     previousRenderSides[Direction.EAST.m_122416_()] = false;
                  }
               }

               if (z > 0) {
                  boolean[] previousRenderSides = rowRenderSides[z - 1];
                  if (previousRenderSides[Direction.SOUTH.m_122416_()]) {
                     renderSides[Direction.NORTH.m_122416_()] = false;
                     previousRenderSides[Direction.SOUTH.m_122416_()] = false;
                  }
               }
            }
         }
      }

      Level level = stabilizer.m_58904_();
      int minY = level.m_141937_();
      int height = level.m_151558_() - minY;
      BlockPos pos = stabilizer.m_58899_();
      int chunkX = SectionPos.m_123171_(pos.m_123341_());
      int chunkZ = SectionPos.m_123171_(pos.m_123343_());
      MekanismRenderer.Model3D model = RenderDimensionalStabilizer.model.get();
      VertexConsumer buffer = renderer.m_6299_(Sheets.m_110792_());
      Camera camera = this.getCamera();

      for (RenderDimensionalStabilizer.RenderPiece piece : this.calculateRenderPieces(allRenderSides)) {
         model.setSideRender(Direction.NORTH, piece.renderNorth)
            .setSideRender(Direction.EAST, piece.renderEast)
            .setSideRender(Direction.SOUTH, piece.renderSouth)
            .setSideRender(Direction.WEST, piece.renderWest);
         int xChunkOffset = piece.x - 2;
         int zChunkOffset = piece.z - 2;
         ChunkPos startChunk = new ChunkPos(chunkX + xChunkOffset, chunkZ + zChunkOffset);
         ChunkPos endChunk = new ChunkPos(startChunk.f_45578_ + piece.xLength - 1, startChunk.f_45579_ + piece.zLength - 1);
         double xShift = 0.01;
         double zShift = 0.01;
         float xScaleShift = 0.02F;
         float zScaleShift = 0.02F;
         if (piece.renderEast && piece.renderWest && !piece.renderNorth) {
            zShift = -0.01;
            zScaleShift = piece.renderSouth ? 0.0F : -0.02F;
         } else if (piece.renderNorth && !piece.renderSouth) {
            zScaleShift = 0.0F;
         } else if (piece.renderNorth && piece.renderWest && !piece.renderEast) {
            xScaleShift = 0.0F;
         } else if (piece.renderNorth && !piece.renderWest) {
            xShift = -0.01;
            xScaleShift = piece.renderEast ? 0.0F : -0.02F;
         } else if (piece.renderSouth && piece.renderEast != piece.renderWest) {
            zShift = -0.01;
            zScaleShift = 0.0F;
         }

         matrix.m_85836_();
         matrix.m_85837_(startChunk.m_45604_() - pos.m_123341_() + xShift, minY - pos.m_123342_(), startChunk.m_45605_() - pos.m_123343_() + zShift);
         matrix.m_85841_(16 * piece.xLength - xScaleShift, height, 16 * piece.zLength - zScaleShift);
         RenderResizableCuboid.FaceDisplay faceDisplay = this.isInsideBounds(
               startChunk.m_45604_(),
               Double.NEGATIVE_INFINITY,
               startChunk.m_45605_(),
               endChunk.m_45608_() + 1,
               Double.POSITIVE_INFINITY,
               endChunk.m_45609_() + 1
            )
            ? RenderResizableCuboid.FaceDisplay.BACK
            : RenderResizableCuboid.FaceDisplay.BOTH;
         MekanismRenderer.renderObject(model, matrix, buffer, colors, 15728880, overlayLight, faceDisplay, camera);
         matrix.m_85849_();
      }
   }

   @Override
   protected String getProfilerSection() {
      return "dimensionalStabilizer";
   }

   public boolean shouldRenderOffScreen(TileEntityDimensionalStabilizer tile) {
      return true;
   }

   public boolean shouldRender(TileEntityDimensionalStabilizer tile, Vec3 camera) {
      return tile.isClientRendering() && tile.canDisplayVisuals() && super.m_142756_(tile, camera);
   }

   private List<RenderDimensionalStabilizer.RenderPiece> calculateRenderPieces(boolean[][][] allRenderSides) {
      record MinimalColumnPieceData(int z, int zLength, boolean renderNorth, boolean renderSouth) {
      }


      record MinimalRowPieceData(int x, boolean renderEast, boolean renderWest) {
      }

      Map<MinimalColumnPieceData, List<MinimalRowPieceData>> columnData = new HashMap<>();

      for (int x = 0; x < allRenderSides.length; x++) {
         boolean[][] rowRenderSides = allRenderSides[x];
         int z = 0;

         while (z < rowRenderSides.length) {
            int zLength = 1;
            boolean[] renderSides = rowRenderSides[z];
            if (renderSides[4]) {
               boolean renderNorth = renderSides[Direction.NORTH.m_122416_()];
               boolean renderSouth = renderSides[Direction.SOUTH.m_122416_()];
               boolean renderEast = renderSides[Direction.EAST.m_122416_()];
               boolean renderWest = renderSides[Direction.WEST.m_122416_()];

               while (!renderSouth && z + zLength < rowRenderSides.length) {
                  boolean[] nextColumnRenderSides = rowRenderSides[z + zLength];
                  if (renderEast != nextColumnRenderSides[Direction.EAST.m_122416_()] || renderWest != nextColumnRenderSides[Direction.WEST.m_122416_()]) {
                     break;
                  }

                  zLength++;
                  renderSouth = nextColumnRenderSides[Direction.SOUTH.m_122416_()];
               }

               columnData.computeIfAbsent(new MinimalColumnPieceData(z, zLength, renderNorth, renderSouth), piece -> new ArrayList<>(5))
                  .add(new MinimalRowPieceData(x, renderEast, renderWest));
            }

            z += zLength;
         }
      }

      List<RenderDimensionalStabilizer.RenderPiece> pieces = new ArrayList<>();

      for (Entry<MinimalColumnPieceData, List<MinimalRowPieceData>> entry : columnData.entrySet()) {
         MinimalColumnPieceData minimalColumnPiece = entry.getKey();
         List<MinimalRowPieceData> rows = entry.getValue();
         int row = 0;

         while (row < rows.size()) {
            int xLength = 1;
            MinimalRowPieceData minimalRowPiece = rows.get(row);
            boolean renderEast = minimalRowPiece.renderEast;

            while (!renderEast && row + xLength < rows.size()) {
               MinimalRowPieceData nextRowPiece = rows.get(row + xLength);
               if (minimalRowPiece.x + xLength != nextRowPiece.x) {
                  break;
               }

               xLength++;
               renderEast = nextRowPiece.renderEast;
            }

            pieces.add(
               new RenderDimensionalStabilizer.RenderPiece(
                  minimalRowPiece.x,
                  xLength,
                  minimalColumnPiece.z,
                  minimalColumnPiece.zLength,
                  minimalColumnPiece.renderNorth,
                  minimalColumnPiece.renderSouth,
                  renderEast,
                  minimalRowPiece.renderWest
               )
            );
            row += xLength;
         }
      }

      return pieces;
   }

   static {
      colors[Direction.NORTH.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.82F);
      colors[Direction.SOUTH.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.82F);
      colors[Direction.WEST.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.78F);
      colors[Direction.EAST.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.78F);
   }

   private record RenderPiece(int x, int xLength, int z, int zLength, boolean renderNorth, boolean renderSouth, boolean renderEast, boolean renderWest) {
   }
}
