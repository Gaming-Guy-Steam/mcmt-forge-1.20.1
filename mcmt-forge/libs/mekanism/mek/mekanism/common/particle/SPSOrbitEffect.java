package mekanism.common.particle;

import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.CustomEffect;
import mekanism.common.lib.math.Quaternion;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class SPSOrbitEffect extends CustomEffect {
   private static final ResourceLocation TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "sps_orbit_effect.png");
   private static final Color COLOR = Color.rgbai(102, 215, 237, 240);
   private final Vec3 center;
   private final Vec3 start;
   private final Vec3 axis;
   private float speed = 0.5F;
   private SPSMultiblockData multiblock;

   public SPSOrbitEffect(SPSMultiblockData multiblock, Vec3 center) {
      super(TEXTURE, 1);
      this.multiblock = multiblock;
      this.center = center;
      float radius = 1.0F + (float)this.rand.nextDouble();
      this.start = this.randVec().m_82490_(radius);
      this.axis = this.randVec();
      this.setPos(this.center.m_82549_(this.start));
      this.setScale(0.01F + this.rand.nextFloat() * 0.04F);
      this.setColor(COLOR);
   }

   public void updateMultiblock(SPSMultiblockData multiblock) {
      this.multiblock = multiblock;
   }

   @Override
   public boolean tick() {
      if (!super.tick() && this.multiblock.isFormed()) {
         this.speed = (float)Math.log10(this.multiblock.lastReceivedEnergy.doubleValue());
         return false;
      } else {
         return true;
      }
   }

   @Override
   public Vec3 getPos(float partialTick) {
      return this.center.m_82549_(Quaternion.rotate(this.start, this.axis, (this.ticker + partialTick) * this.speed));
   }
}
