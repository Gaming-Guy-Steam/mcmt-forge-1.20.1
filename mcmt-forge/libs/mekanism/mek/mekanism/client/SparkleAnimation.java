package mekanism.client;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class SparkleAnimation {
   private final BlockEntity tile;
   private final BlockPos corner1;
   private final BlockPos corner2;

   public SparkleAnimation(BlockEntity tile, BlockPos corner1, BlockPos corner2) {
      this.tile = tile;
      this.corner1 = corner1;
      this.corner2 = corner2;
   }

   public SparkleAnimation(BlockEntity tile, BlockPos renderLoc, int length, int width, int height) {
      this(
         tile,
         new BlockPos(renderLoc.m_123341_(), renderLoc.m_123342_() - 1, renderLoc.m_123343_()),
         new BlockPos(renderLoc.m_123341_() + length, renderLoc.m_123342_() + height - 1, renderLoc.m_123343_() + width)
      );
   }

   public void run() {
      Level world = this.tile.m_58904_();
      ThreadLocalRandom random = ThreadLocalRandom.current();
      int xSize = this.corner2.m_123341_() - this.corner1.m_123341_() + 1;
      int ySize = this.corner2.m_123342_() - this.corner1.m_123342_() + 1;
      int zSize = this.corner2.m_123343_() - this.corner1.m_123343_() + 1;
      Vec3 origin = new Vec3(xSize / 2.0, ySize / 2.0, zSize / 2.0);
      this.sparkleSide(world, random, origin, origin, xSize, ySize, 0.0F, 0.0F);
      this.sparkleSide(world, random, origin, origin, xSize, ySize, (float) Math.PI, 0.0F);
      Vec3 displacement = new Vec3(origin.f_82481_, origin.f_82480_, origin.f_82479_);
      this.sparkleSide(world, random, origin, displacement, zSize, ySize, (float) (Math.PI / 2), 0.0F);
      this.sparkleSide(world, random, origin, displacement, zSize, ySize, (float) (Math.PI * 3.0 / 2.0), 0.0F);
      displacement = new Vec3(origin.f_82479_, origin.f_82481_, origin.f_82480_);
      this.sparkleSide(world, random, origin, displacement, xSize, zSize, 0.0F, (float) (Math.PI / 2));
      this.sparkleSide(world, random, origin, displacement, xSize, zSize, 0.0F, (float) (Math.PI * 3.0 / 2.0));
   }

   private void sparkleSide(Level world, Random random, Vec3 origin, Vec3 displacement, int width, int height, float rotationYaw, float rotationPitch) {
      for (int i = 0; i < 100; i++) {
         Vec3 pos = new Vec3(width * random.nextDouble(), height * random.nextDouble(), -0.01).m_82546_(displacement);
         pos = pos.m_82524_(rotationYaw).m_82496_(rotationPitch);
         pos = pos.m_82549_(origin).m_82520_(this.corner1.m_123341_(), this.corner1.m_123342_(), this.corner1.m_123343_());
         world.m_7106_(DustParticleOptions.f_123656_, pos.m_7096_(), pos.m_7098_(), pos.m_7094_(), 0.0, 0.0, 0.0);
      }
   }
}
