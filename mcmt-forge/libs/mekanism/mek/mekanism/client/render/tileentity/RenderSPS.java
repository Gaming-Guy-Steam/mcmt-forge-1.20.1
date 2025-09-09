package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.effect.BillboardingEffectRenderer;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.CustomEffect;
import mekanism.common.lib.math.Plane;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.particle.SPSOrbitEffect;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderSPS extends MultiblockTileEntityRenderer<SPSMultiblockData, TileEntitySPSCasing> {
   private static final CustomEffect CORE = new CustomEffect(MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "energy_effect.png"));
   private static final Map<UUID, BoltRenderer> boltRendererMap = new HashMap<>();
   private static final float MIN_SCALE = 0.1F;
   private static final float MAX_SCALE = 4.0F;
   private static final Random rand = new Random();
   private final Minecraft minecraft = Minecraft.m_91087_();

   public static void clearBoltRenderers() {
      boltRendererMap.clear();
   }

   public RenderSPS(Context context) {
      super(context);
   }

   protected void render(
      TileEntitySPSCasing tile,
      SPSMultiblockData multiblock,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      BoltRenderer bolts = boltRendererMap.computeIfAbsent(multiblock.inventoryID, mb -> new BoltRenderer());
      Vec3 center = Vec3.m_82528_(multiblock.getMinPos()).m_82549_(Vec3.m_82528_(multiblock.getMaxPos())).m_82549_(new Vec3(1.0, 1.0, 1.0)).m_82490_(0.5);
      Vec3 renderCenter = center.m_82492_(tile.m_58899_().m_123341_(), tile.m_58899_().m_123342_(), tile.m_58899_().m_123343_());
      if (!this.minecraft.m_91104_()) {
         for (SPSMultiblockData.CoilData data : multiblock.coilData.coilMap.values()) {
            if (data.prevLevel > 0) {
               bolts.update(data.coilPos.hashCode(), getBoltFromData(data, tile.m_58899_(), renderCenter), partialTick);
            }
         }
      }

      float energyScale = getEnergyScale(multiblock.lastProcessed);
      int targetEffectCount = 0;
      if (!this.minecraft.m_91104_() && !multiblock.lastReceivedEnergy.isZero()) {
         if (rand.nextDouble() < getBoundedScale(energyScale, 0.01F, 0.4F)) {
            VoxelCuboid.CuboidSide side = VoxelCuboid.CuboidSide.SIDES[rand.nextInt(6)];
            Plane plane = Plane.getInnerCuboidPlane(multiblock.getBounds(), side);
            Vec3 endPos = plane.getRandomPoint(rand).m_82492_(tile.m_58899_().m_123341_(), tile.m_58899_().m_123342_(), tile.m_58899_().m_123343_());
            BoltEffect bolt = new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, renderCenter, endPos, 15)
               .size(0.01F * getBoundedScale(energyScale, 0.5F, 5.0F))
               .lifespan(8)
               .spawn(BoltEffect.SpawnFunction.NO_DELAY);
            bolts.update(Objects.hash(side, endPos), bolt, partialTick);
         }

         targetEffectCount = (int)getBoundedScale(energyScale, 10.0F, 120.0F);
      }

      if (tile.orbitEffects.size() > targetEffectCount) {
         tile.orbitEffects.poll();
      } else if (tile.orbitEffects.size() < targetEffectCount && rand.nextDouble() < 0.5) {
         tile.orbitEffects.add(new SPSOrbitEffect(multiblock, center));
      }

      bolts.render(partialTick, matrix, renderer);
      if (multiblock.lastProcessed > 0.0) {
         float scale = getBoundedScale(energyScale, 0.1F, 4.0F);
         BillboardingEffectRenderer.render(CORE.getTexture(), "supercriticalPhaseShifter.core", () -> {
            CORE.setPos(center);
            CORE.setScale(scale);
            return CORE;
         });
      }

      tile.orbitEffects.forEach(effect -> BillboardingEffectRenderer.render(effect, "supercriticalPhaseShifter.orbitEffect"));
   }

   private static float getEnergyScale(double lastProcessed) {
      return (float)Math.min(1.0, Math.max(0.0, (Math.log10(lastProcessed) + 2.0) / 4.0));
   }

   private static float getBoundedScale(float scale, float min, float max) {
      return min + scale * (max - min);
   }

   private static BoltEffect getBoltFromData(SPSMultiblockData.CoilData data, BlockPos pos, Vec3 center) {
      Vec3 start = Vec3.m_82512_(data.coilPos.m_121945_(data.side));
      start = start.m_82549_(Vec3.m_82528_(data.side.m_122436_()).m_82490_(0.5));
      int count = 1 + (data.prevLevel - 1) / 2;
      float size = 0.01F * data.prevLevel;
      return new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, start.m_82492_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_()), center, 15)
         .count(count)
         .size(size)
         .lifespan(8)
         .spawn(BoltEffect.SpawnFunction.delay(4.0F));
   }

   @Override
   protected String getProfilerSection() {
      return "supercriticalPhaseShifter";
   }

   protected boolean shouldRender(TileEntitySPSCasing tile, SPSMultiblockData multiblock, Vec3 camera) {
      return super.shouldRender(tile, multiblock, camera) && multiblock.getBounds() != null;
   }

   static {
      CORE.setColor(Color.rgbai(255, 255, 255, 240));
   }
}
