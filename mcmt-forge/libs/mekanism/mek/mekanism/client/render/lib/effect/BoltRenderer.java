package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.lib.effect.BoltEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class BoltRenderer {
   private static final float REFRESH_TIME = 3.0F;
   private static final double MAX_OWNER_TRACK_TIME = 100.0;
   private BoltRenderer.Timestamp refreshTimestamp = new BoltRenderer.Timestamp();
   private final Random random = new Random();
   private final Minecraft minecraft = Minecraft.m_91087_();
   private final Map<Object, BoltRenderer.BoltOwnerData> boltOwners = new Object2ObjectOpenHashMap();

   public boolean hasBoltsToRender() {
      synchronized (this.boltOwners) {
         return this.boltOwners.values().stream().anyMatch(data -> !data.bolts.isEmpty());
      }
   }

   public void render(float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn) {
      VertexConsumer buffer = bufferIn.m_6299_(MekanismRenderType.MEK_LIGHTNING);
      Matrix4f matrix = matrixStack.m_85850_().m_252922_();
      BoltRenderer.Timestamp timestamp = new BoltRenderer.Timestamp(this.minecraft.f_91073_.m_46467_(), partialTicks);
      boolean refresh = timestamp.isPassed(this.refreshTimestamp, 0.33333334F);
      if (refresh) {
         this.refreshTimestamp = timestamp;
      }

      synchronized (this.boltOwners) {
         Iterator<Entry<Object, BoltRenderer.BoltOwnerData>> iter = this.boltOwners.entrySet().iterator();

         while (iter.hasNext()) {
            Entry<Object, BoltRenderer.BoltOwnerData> entry = iter.next();
            BoltRenderer.BoltOwnerData data = entry.getValue();
            if (refresh) {
               data.bolts.removeIf(bolt -> bolt.tick(timestamp));
            }

            if (data.bolts.isEmpty() && data.lastBolt != null && data.lastBolt.getSpawnFunction().isConsecutive()) {
               data.addBolt(new BoltRenderer.BoltInstance(data.lastBolt, timestamp), timestamp);
            }

            data.bolts.forEach(bolt -> bolt.render(matrix, buffer, timestamp));
            if (data.bolts.isEmpty() && timestamp.isPassed(data.lastUpdateTimestamp, 100.0)) {
               iter.remove();
            }
         }
      }
   }

   public void update(Object owner, BoltEffect newBoltData, float partialTicks) {
      if (this.minecraft.f_91073_ != null) {
         synchronized (this.boltOwners) {
            BoltRenderer.BoltOwnerData data = this.boltOwners.computeIfAbsent(owner, o -> new BoltRenderer.BoltOwnerData());
            data.lastBolt = newBoltData;
            BoltRenderer.Timestamp timestamp = new BoltRenderer.Timestamp(this.minecraft.f_91073_.m_46467_(), partialTicks);
            if ((!data.lastBolt.getSpawnFunction().isConsecutive() || data.bolts.isEmpty()) && timestamp.isPassed(data.lastBoltTimestamp, data.lastBoltDelay)) {
               data.addBolt(new BoltRenderer.BoltInstance(newBoltData, timestamp), timestamp);
            }

            data.lastUpdateTimestamp = timestamp;
         }
      }
   }

   public static class BoltInstance {
      private final BoltEffect bolt;
      private final List<BoltEffect.BoltQuads> renderQuads;
      private final BoltRenderer.Timestamp createdTimestamp;

      public BoltInstance(BoltEffect bolt, BoltRenderer.Timestamp timestamp) {
         this.bolt = bolt;
         this.renderQuads = bolt.generate();
         this.createdTimestamp = timestamp;
      }

      public void render(Matrix4f matrix, VertexConsumer buffer, BoltRenderer.Timestamp timestamp) {
         float lifeScale = timestamp.subtract(this.createdTimestamp).value() / this.bolt.getLifespan();
         BoltEffect.FadeFunction.RenderBounds bounds = this.bolt.getFadeFunction().getRenderBounds(this.renderQuads.size(), lifeScale);

         for (int i = bounds.start(); i < bounds.end(); i++) {
            this.renderQuads
               .get(i)
               .getVecs()
               .forEach(
                  v -> buffer.m_252986_(matrix, (float)v.f_82479_, (float)v.f_82480_, (float)v.f_82481_)
                     .m_6122_(this.bolt.getColor().r(), this.bolt.getColor().g(), this.bolt.getColor().b(), this.bolt.getColor().a())
                     .m_5752_()
               );
         }
      }

      public boolean tick(BoltRenderer.Timestamp timestamp) {
         return timestamp.isPassed(this.createdTimestamp, this.bolt.getLifespan());
      }
   }

   public class BoltOwnerData {
      private final Set<BoltRenderer.BoltInstance> bolts = new ObjectOpenHashSet();
      private BoltEffect lastBolt;
      private BoltRenderer.Timestamp lastBoltTimestamp = new BoltRenderer.Timestamp();
      private BoltRenderer.Timestamp lastUpdateTimestamp = new BoltRenderer.Timestamp();
      private double lastBoltDelay;

      private void addBolt(BoltRenderer.BoltInstance instance, BoltRenderer.Timestamp timestamp) {
         this.bolts.add(instance);
         this.lastBoltDelay = instance.bolt.getSpawnFunction().getSpawnDelay(BoltRenderer.this.random);
         this.lastBoltTimestamp = timestamp;
      }
   }

   public static class Timestamp {
      private final long ticks;
      private final float partial;

      public Timestamp() {
         this(0L, 0.0F);
      }

      public Timestamp(long ticks, float partial) {
         this.ticks = ticks;
         this.partial = partial;
      }

      public BoltRenderer.Timestamp subtract(BoltRenderer.Timestamp other) {
         long newTicks = this.ticks - other.ticks;
         float newPartial = this.partial - other.partial;
         if (newPartial < 0.0F) {
            newPartial++;
            newTicks--;
         }

         return new BoltRenderer.Timestamp(newTicks, newPartial);
      }

      public float value() {
         return (float)this.ticks + this.partial;
      }

      public boolean isPassed(BoltRenderer.Timestamp prev, double duration) {
         long ticksPassed = this.ticks - prev.ticks;
         if (ticksPassed > duration) {
            return true;
         } else {
            duration -= ticksPassed;
            return duration >= 1.0 ? false : this.partial - prev.partial >= duration;
         }
      }
   }
}
