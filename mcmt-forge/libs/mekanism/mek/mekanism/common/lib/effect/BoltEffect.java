package mekanism.common.lib.effect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import mekanism.common.lib.Color;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BoltEffect {
   private final Random random = new Random();
   private final BoltEffect.BoltRenderInfo renderInfo;
   private final Vec3 start;
   private final Vec3 end;
   private final int segments;
   private int count = 1;
   private float size = 0.1F;
   private int lifespan = 30;
   private BoltEffect.SpawnFunction spawnFunction = BoltEffect.SpawnFunction.delay(60.0F);
   private BoltEffect.FadeFunction fadeFunction = BoltEffect.FadeFunction.fade(0.5F);

   public BoltEffect(Vec3 start, Vec3 end) {
      this(BoltEffect.BoltRenderInfo.DEFAULT, start, end, (int)Math.sqrt(start.m_82554_(end) * 100.0));
   }

   public BoltEffect(BoltEffect.BoltRenderInfo info, Vec3 start, Vec3 end, int segments) {
      this.renderInfo = info;
      this.start = start;
      this.end = end;
      this.segments = segments;
   }

   public BoltEffect count(int count) {
      this.count = count;
      return this;
   }

   public BoltEffect size(float size) {
      this.size = size;
      return this;
   }

   public BoltEffect spawn(BoltEffect.SpawnFunction spawnFunction) {
      this.spawnFunction = spawnFunction;
      return this;
   }

   public BoltEffect fade(BoltEffect.FadeFunction fadeFunction) {
      this.fadeFunction = fadeFunction;
      return this;
   }

   public BoltEffect lifespan(int lifespan) {
      this.lifespan = lifespan;
      return this;
   }

   public int getLifespan() {
      return this.lifespan;
   }

   public BoltEffect.SpawnFunction getSpawnFunction() {
      return this.spawnFunction;
   }

   public BoltEffect.FadeFunction getFadeFunction() {
      return this.fadeFunction;
   }

   public Color getColor() {
      return this.renderInfo.color;
   }

   public List<BoltEffect.BoltQuads> generate() {
      List<BoltEffect.BoltQuads> quads = new ArrayList<>();
      Vec3 diff = this.end.m_82546_(this.start);
      float totalDistance = (float)diff.m_82553_();

      for (int i = 0; i < this.count; i++) {
         Queue<BoltEffect.BoltInstructions> drawQueue = new LinkedList<>();
         drawQueue.add(new BoltEffect.BoltInstructions(this.start, 0.0F, new Vec3(0.0, 0.0, 0.0), null, false));

         while (!drawQueue.isEmpty()) {
            BoltEffect.BoltInstructions data = drawQueue.poll();
            Vec3 perpendicularDist = data.perpendicularDist;
            float progress = data.progress
               + 1.0F / this.segments * (1.0F - this.renderInfo.parallelNoise + this.random.nextFloat() * this.renderInfo.parallelNoise * 2.0F);
            float segmentDiffScale = this.renderInfo.spreadFunction.getMaxSpread(progress);
            Vec3 segmentEnd;
            if (progress >= 1.0F && segmentDiffScale <= 0.0F) {
               segmentEnd = this.end;
            } else {
               float maxDiff = this.renderInfo.spreadFactor * segmentDiffScale * totalDistance;
               Vec3 randVec = findRandomOrthogonalVector(diff, this.random);
               double rand = this.renderInfo.randomFunction.getRandom(this.random);
               perpendicularDist = this.renderInfo.segmentSpreader.getSegmentAdd(perpendicularDist, randVec, maxDiff, segmentDiffScale, progress, rand);
               segmentEnd = this.start.m_82549_(diff.m_82490_(progress)).m_82549_(perpendicularDist);
            }

            float boltSize = this.size * (0.5F + (1.0F - progress) * 0.5F);
            BoltEffect.BoltQuadData quadData = this.createQuads(data.cache, data.start, segmentEnd, boltSize);
            quads.add(quadData.quads());
            if (progress >= 1.0F) {
               break;
            }

            if (!data.isBranch) {
               drawQueue.add(new BoltEffect.BoltInstructions(segmentEnd, progress, perpendicularDist, quadData.cache(), false));
            } else if (this.random.nextFloat() < this.renderInfo.branchContinuationFactor) {
               drawQueue.add(new BoltEffect.BoltInstructions(segmentEnd, progress, perpendicularDist, quadData.cache(), true));
            }

            while (this.random.nextFloat() < this.renderInfo.branchInitiationFactor * (1.0F - progress)) {
               drawQueue.add(new BoltEffect.BoltInstructions(segmentEnd, progress, perpendicularDist, quadData.cache(), true));
            }
         }
      }

      return quads;
   }

   private static Vec3 findRandomOrthogonalVector(Vec3 vec, Random rand) {
      Vec3 newVec = new Vec3(-0.5 + rand.nextDouble(), -0.5 + rand.nextDouble(), -0.5 + rand.nextDouble());
      return vec.m_82537_(newVec).m_82541_();
   }

   private BoltEffect.BoltQuadData createQuads(BoltEffect.QuadCache cache, Vec3 startPos, Vec3 end, float size) {
      Vec3 diff = end.m_82546_(startPos);
      Vec3 rightAdd = diff.m_82537_(new Vec3(0.5, 0.5, 0.5)).m_82541_().m_82490_(size);
      Vec3 backAdd = diff.m_82537_(rightAdd).m_82541_().m_82490_(size);
      Vec3 rightAddSplit = rightAdd.m_82490_(0.5);
      Vec3 start = cache == null ? startPos : cache.prevEnd;
      Vec3 startRight = cache == null ? start.m_82549_(rightAdd) : cache.prevEndRight;
      Vec3 startBack = cache == null ? start.m_82549_(rightAddSplit).m_82549_(backAdd) : cache.prevEndBack;
      Vec3 endRight = end.m_82549_(rightAdd);
      Vec3 endBack = end.m_82549_(rightAddSplit).m_82549_(backAdd);
      BoltEffect.BoltQuads quads = new BoltEffect.BoltQuads();
      quads.addQuad(start, end, endRight, startRight);
      quads.addQuad(startRight, endRight, end, start);
      quads.addQuad(startRight, endRight, endBack, startBack);
      quads.addQuad(startBack, endBack, endRight, startRight);
      return new BoltEffect.BoltQuadData(quads, new BoltEffect.QuadCache(end, endRight, endBack));
   }

   protected record BoltInstructions(Vec3 start, float progress, Vec3 perpendicularDist, BoltEffect.QuadCache cache, boolean isBranch) {
   }

   private record BoltQuadData(BoltEffect.BoltQuads quads, BoltEffect.QuadCache cache) {
   }

   public static class BoltQuads {
      private final List<Vec3> vecs = new ArrayList<>();

      protected void addQuad(Vec3... quadVecs) {
         Collections.addAll(this.vecs, quadVecs);
      }

      public List<Vec3> getVecs() {
         return this.vecs;
      }
   }

   public static class BoltRenderInfo {
      public static final BoltEffect.BoltRenderInfo DEFAULT = new BoltEffect.BoltRenderInfo();
      public static final BoltEffect.BoltRenderInfo ELECTRICITY = electricity();
      private float parallelNoise = 0.1F;
      private float spreadFactor = 0.1F;
      private float branchInitiationFactor = 0.0F;
      private float branchContinuationFactor = 0.0F;
      private Color color = Color.rgbad(0.45F, 0.45F, 0.5, 0.8F);
      private BoltEffect.RandomFunction randomFunction = BoltEffect.RandomFunction.GAUSSIAN;
      private BoltEffect.SpreadFunction spreadFunction = BoltEffect.SpreadFunction.SINE;
      private BoltEffect.SegmentSpreader segmentSpreader = BoltEffect.SegmentSpreader.NO_MEMORY;

      public static BoltEffect.BoltRenderInfo electricity() {
         return new BoltEffect.BoltRenderInfo()
            .color(Color.rgbad(0.54F, 0.91F, 1.0, 0.8F))
            .noise(0.2F, 0.2F)
            .branching(0.1F, 0.6F)
            .spreader(BoltEffect.SegmentSpreader.memory(0.9F));
      }

      public BoltEffect.BoltRenderInfo noise(float parallelNoise, float spreadFactor) {
         this.parallelNoise = parallelNoise;
         this.spreadFactor = spreadFactor;
         return this;
      }

      public BoltEffect.BoltRenderInfo branching(float branchInitiationFactor, float branchContinuationFactor) {
         this.branchInitiationFactor = branchInitiationFactor;
         this.branchContinuationFactor = branchContinuationFactor;
         return this;
      }

      public BoltEffect.BoltRenderInfo spreader(BoltEffect.SegmentSpreader segmentSpreader) {
         this.segmentSpreader = segmentSpreader;
         return this;
      }

      public BoltEffect.BoltRenderInfo randomFunction(BoltEffect.RandomFunction randomFunction) {
         this.randomFunction = randomFunction;
         return this;
      }

      public BoltEffect.BoltRenderInfo spreadFunction(BoltEffect.SpreadFunction spreadFunction) {
         this.spreadFunction = spreadFunction;
         return this;
      }

      public BoltEffect.BoltRenderInfo color(Color color) {
         this.color = color;
         return this;
      }
   }

   public interface FadeFunction {
      BoltEffect.FadeFunction NONE = (totalBolts, lifeScale) -> new BoltEffect.FadeFunction.RenderBounds(0, totalBolts);

      static BoltEffect.FadeFunction fade(float fade) {
         return (totalBolts, lifeScale) -> {
            int start = lifeScale > 1.0F - fade ? (int)(totalBolts * (lifeScale - (1.0F - fade)) / fade) : 0;
            int end = lifeScale < fade ? (int)(totalBolts * (lifeScale / fade)) : totalBolts;
            return new BoltEffect.FadeFunction.RenderBounds(start, end);
         };
      }

      BoltEffect.FadeFunction.RenderBounds getRenderBounds(int totalBolts, float lifeScale);

      public record RenderBounds(int start, int end) {
      }
   }

   private record QuadCache(Vec3 prevEnd, Vec3 prevEndRight, Vec3 prevEndBack) {
   }

   public interface RandomFunction {
      BoltEffect.RandomFunction UNIFORM = Random::nextFloat;
      BoltEffect.RandomFunction GAUSSIAN = rand -> (float)rand.nextGaussian();

      float getRandom(Random rand);
   }

   public interface SegmentSpreader {
      BoltEffect.SegmentSpreader NO_MEMORY = (perpendicularDist, randVec, maxDiff, scale, progress, rand) -> randVec.m_82490_(maxDiff * rand);

      static BoltEffect.SegmentSpreader memory(float memoryFactor) {
         return (perpendicularDist, randVec, maxDiff, spreadScale, progress, rand) -> {
            double nextDiff = maxDiff * (1.0F - memoryFactor) * rand;
            Vec3 cur = randVec.m_82490_(nextDiff);
            perpendicularDist = perpendicularDist.m_82549_(cur);
            double length = perpendicularDist.m_82553_();
            if (length > maxDiff) {
               perpendicularDist = perpendicularDist.m_82490_(maxDiff / length);
            }

            return perpendicularDist.m_82549_(cur);
         };
      }

      Vec3 getSegmentAdd(Vec3 perpendicularDist, Vec3 randVec, float maxDiff, float scale, float progress, double rand);
   }

   public interface SpawnFunction {
      BoltEffect.SpawnFunction NO_DELAY = rand -> new BoltEffect.SpawnFunction.SpawnDelayBounds(0.0F, 0.0F);
      BoltEffect.SpawnFunction CONSECUTIVE = new BoltEffect.SpawnFunction() {
         @Override
         public BoltEffect.SpawnFunction.SpawnDelayBounds getSpawnDelayBounds(Random rand) {
            return new BoltEffect.SpawnFunction.SpawnDelayBounds(0.0F, 0.0F);
         }

         @Override
         public boolean isConsecutive() {
            return true;
         }
      };

      static BoltEffect.SpawnFunction delay(float delay) {
         return rand -> new BoltEffect.SpawnFunction.SpawnDelayBounds(delay, delay);
      }

      static BoltEffect.SpawnFunction noise(float delay, float noise) {
         return rand -> new BoltEffect.SpawnFunction.SpawnDelayBounds(delay - noise, delay + noise);
      }

      BoltEffect.SpawnFunction.SpawnDelayBounds getSpawnDelayBounds(Random rand);

      default float getSpawnDelay(Random rand) {
         BoltEffect.SpawnFunction.SpawnDelayBounds bounds = this.getSpawnDelayBounds(rand);
         return Mth.m_14179_(rand.nextFloat(), bounds.start(), bounds.end());
      }

      default boolean isConsecutive() {
         return false;
      }

      public record SpawnDelayBounds(float start, float end) {
      }
   }

   public interface SpreadFunction {
      BoltEffect.SpreadFunction LINEAR_ASCENT = progress -> progress;
      BoltEffect.SpreadFunction LINEAR_ASCENT_DESCENT = progress -> (progress - Math.max(0.0F, 2.0F * progress - 1.0F)) / 0.5F;
      BoltEffect.SpreadFunction SINE = progress -> (float)Math.sin(Math.PI * progress);

      float getMaxSpread(float progress);
   }
}
