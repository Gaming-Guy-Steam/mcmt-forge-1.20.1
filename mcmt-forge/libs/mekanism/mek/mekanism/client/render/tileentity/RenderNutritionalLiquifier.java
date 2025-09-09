package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderNutritionalLiquifier extends MekanismTileEntityRenderer<TileEntityNutritionalLiquifier> {
   private static final Int2ObjectMap<MekanismRenderer.Model3D> cachedModels = new Int2ObjectOpenHashMap();
   private static final Map<TileEntityNutritionalLiquifier, RenderNutritionalLiquifier.PseudoParticleData> particles = new WeakHashMap<>();
   private static final int stages = 40;
   private static final float BLADE_SPEED = 25.0F;
   private static final float ROTATE_SPEED = 10.0F;

   public static void resetCachedModels() {
      cachedModels.clear();
   }

   public RenderNutritionalLiquifier(Context context) {
      super(context);
   }

   protected void render(
      TileEntityNutritionalLiquifier tile,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      if (!tile.fluidTank.isEmpty()) {
         FluidStack paste = tile.fluidTank.getFluid();
         float fluidScale = (float)paste.getAmount() / tile.fluidTank.getCapacity();
         MekanismRenderer.renderObject(
            this.getPasteModel(paste, fluidScale),
            matrix,
            renderer.m_6299_(Sheets.m_110792_()),
            MekanismRenderer.getColorARGB(paste, fluidScale),
            light,
            overlayLight,
            RenderResizableCuboid.FaceDisplay.FRONT,
            this.getCamera(),
            tile.m_58899_()
         );
      }

      boolean active = tile.getActive();
      if (active) {
         matrix.m_85836_();
         matrix.m_85837_(0.5, 0.5, 0.5);
         matrix.m_252781_(Axis.f_252436_.m_252977_(((float)tile.m_58904_().m_46467_() + partialTick) * 25.0F % 360.0F));
         matrix.m_85837_(-0.5, -0.5, -0.5);
         Pose entry = matrix.m_85850_();
         VertexConsumer bladeBuffer = renderer.m_6299_(Sheets.m_110789_());

         for (BakedQuad quad : MekanismModelCache.INSTANCE.LIQUIFIER_BLADE.getQuads(tile.m_58904_().f_46441_)) {
            bladeBuffer.m_85987_(entry, quad, 1.0F, 1.0F, 1.0F, light, overlayLight);
         }

         matrix.m_85849_();
      }

      ItemStack stack = tile.getRenderStack();
      if (!stack.m_41619_()) {
         matrix.m_85836_();
         matrix.m_85837_(0.5, 0.6, 0.5);
         if (active) {
            matrix.m_252781_(Axis.f_252436_.m_252977_(((float)tile.m_58904_().m_46467_() + partialTick) * 10.0F % 360.0F));
         }

         Minecraft.m_91087_()
            .m_91291_()
            .m_269128_(
               stack, ItemDisplayContext.GROUND, light, overlayLight, matrix, renderer, tile.m_58904_(), MathUtils.clampToInt(tile.m_58899_().m_121878_())
            );
         matrix.m_85849_();
         if (active && Minecraft.m_91087_().f_91066_.m_231929_().m_231551_() != ParticleStatus.MINIMAL) {
            RenderNutritionalLiquifier.PseudoParticleData pseudoParticles = particles.computeIfAbsent(
               tile, t -> new RenderNutritionalLiquifier.PseudoParticleData()
            );
            if (!Minecraft.m_91087_().m_91104_()) {
               if (pseudoParticles.lastTick != tile.m_58904_().m_46467_()) {
                  pseudoParticles.lastTick = tile.m_58904_().m_46467_();
                  pseudoParticles.particles.removeIf(RenderNutritionalLiquifier.PseudoParticle::tick);
               }

               int rate = Minecraft.m_91087_().f_91066_.m_231929_().m_231551_() == ParticleStatus.DECREASED ? 12 : 4;
               if (tile.m_58904_().m_46467_() % rate == 0L) {
                  pseudoParticles.particles.add(new RenderNutritionalLiquifier.PseudoParticle(tile.m_58904_(), stack));
               }
            }

            VertexConsumer buffer = renderer.m_6299_(MekanismRenderType.NUTRITIONAL_PARTICLE);
            matrix.m_85836_();
            matrix.m_85837_(0.5, 0.55, 0.5);
            Matrix4f matrix4f = matrix.m_85850_().m_252922_();

            for (RenderNutritionalLiquifier.PseudoParticle particle : pseudoParticles.particles) {
               particle.render(matrix4f, buffer, partialTick, light);
            }

            matrix.m_85849_();
         } else {
            particles.remove(tile);
         }
      }
   }

   @Override
   protected String getProfilerSection() {
      return "nutritionalLiquifier";
   }

   private MekanismRenderer.Model3D getPasteModel(FluidStack paste, float fluidScale) {
      return (MekanismRenderer.Model3D)cachedModels.computeIfAbsent(
         ModelRenderer.getStage(paste, 40, fluidScale),
         stage -> new MekanismRenderer.Model3D()
            .setTexture(MekanismRenderer.getFluidTexture(paste, MekanismRenderer.FluidTextureType.STILL))
            .setSideRender(Direction.DOWN, false)
            .setSideRender(Direction.UP, stage < 40)
            .xBounds(0.001F, 0.999F)
            .yBounds(0.313F, 0.313F + 0.624F * (stage / 40.0F))
            .zBounds(0.001F, 0.999F)
      );
   }

   private static class PseudoParticle {
      private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
      private final TextureAtlasSprite sprite;
      private final float quadSize;
      private final float uo;
      private final float vo;
      protected double xo;
      protected double yo;
      protected double zo;
      protected double x;
      protected double y;
      protected double z;
      protected double xd;
      protected double yd;
      protected double zd;
      protected int lifetime;
      protected int age;
      protected float gravity;
      private AABB bb = INITIAL_AABB;
      protected float bbWidth = 0.6F;
      protected float bbHeight = 1.8F;

      protected PseudoParticle(Level world, ItemStack stack) {
         this.setSize(0.2F, 0.2F);
         this.x = (world.f_46441_.m_188501_() - 0.5) * 0.3;
         this.y = (world.f_46441_.m_188501_() - 0.5) * 0.3;
         this.z = (world.f_46441_.m_188501_() - 0.5) * 0.3;
         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         this.lifetime = (int)(4.0F / (world.f_46441_.m_188501_() * 0.9F + 0.1F));
         this.xd = (Math.random() * 2.0 - 1.0) * 0.4;
         this.yd = (Math.random() * 2.0 - 1.0) * 0.4;
         this.zd = (Math.random() * 2.0 - 1.0) * 0.4;
         float f = (float)(Math.random() + Math.random() + 1.0) * 0.15F;
         float f1 = (float)Mth.m_184648_(this.xd, this.yd, this.zd);
         this.xd = this.xd / f1 * f * 0.4;
         this.yd = this.yd / f1 * f * 0.4 + 0.1;
         this.zd = this.zd / f1 * f * 0.4;
         this.sprite = Minecraft.m_91087_().m_91291_().m_174264_(stack, world, null, 0).getParticleIcon(ModelData.EMPTY);
         this.gravity = 1.0F;
         this.quadSize = 0.1F * (world.f_46441_.m_188501_() * 0.5F + 0.5F);
         this.uo = world.f_46441_.m_188501_() * 3.0F;
         this.vo = world.f_46441_.m_188501_() * 3.0F;
         this.xd *= 0.1;
         this.yd *= 0.1;
         this.zd *= 0.1;
         this.xd = this.xd + (world.f_46441_.m_188501_() - 0.5) * 0.075;
         this.yd = this.yd + (Math.random() * 0.1 + 0.05);
         this.zd = this.zd + (world.f_46441_.m_188501_() - 0.5) * 0.075;
      }

      public boolean tick() {
         if (this.age++ < this.lifetime && !(this.y < -0.25)) {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            this.yd = this.yd - 0.04 * this.gravity;
            if (this.xd != 0.0 || this.yd != 0.0 || this.zd != 0.0) {
               this.bb = this.bb.m_82386_(this.xd, this.yd, this.zd);
               this.x = (this.bb.f_82288_ + this.bb.f_82291_) / 2.0;
               this.y = this.bb.f_82289_;
               this.z = (this.bb.f_82290_ + this.bb.f_82293_) / 2.0;
            }

            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            return false;
         } else {
            return true;
         }
      }

      public void render(Matrix4f matrix, VertexConsumer buffer, float partialTicks, int light) {
         float f = (float)Mth.m_14139_(partialTicks, this.xo, this.x);
         float f1 = (float)Mth.m_14139_(partialTicks, this.yo, this.y);
         float f2 = (float)Mth.m_14139_(partialTicks, this.zo, this.z);
         Quaternionf quaternion = Minecraft.m_91087_().m_91290_().f_114358_.m_253121_();
         Vector3f[] vectors = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
         };

         for (int i = 0; i < 4; i++) {
            Vector3f vector3f = vectors[i];
            quaternion.transform(vector3f);
            vector3f.mul(this.quadSize);
            vector3f.add(f, f1, f2);
         }

         float f7 = this.getU0();
         float f8 = this.getU1();
         float f5 = this.getV0();
         float f6 = this.getV1();
         buffer.m_252986_(matrix, vectors[0].x(), vectors[0].y(), vectors[0].z()).m_7421_(f8, f6).m_6122_(255, 255, 255, 255).m_85969_(light).m_5752_();
         buffer.m_252986_(matrix, vectors[1].x(), vectors[1].y(), vectors[1].z()).m_7421_(f8, f5).m_6122_(255, 255, 255, 255).m_85969_(light).m_5752_();
         buffer.m_252986_(matrix, vectors[2].x(), vectors[2].y(), vectors[2].z()).m_7421_(f7, f5).m_6122_(255, 255, 255, 255).m_85969_(light).m_5752_();
         buffer.m_252986_(matrix, vectors[3].x(), vectors[3].y(), vectors[3].z()).m_7421_(f7, f6).m_6122_(255, 255, 255, 255).m_85969_(light).m_5752_();
      }

      protected float getU0() {
         return this.sprite.m_118367_((this.uo + 1.0F) / 4.0F * 16.0F);
      }

      protected float getU1() {
         return this.sprite.m_118367_(this.uo / 4.0F * 16.0F);
      }

      protected float getV0() {
         return this.sprite.m_118393_(this.vo / 4.0F * 16.0F);
      }

      protected float getV1() {
         return this.sprite.m_118393_((this.vo + 1.0F) / 4.0F * 16.0F);
      }

      protected void setSize(float particleWidth, float particleHeight) {
         if (particleWidth != this.bbWidth || particleHeight != this.bbHeight) {
            this.bbWidth = particleWidth;
            this.bbHeight = particleHeight;
            double d0 = (this.bb.f_82288_ + this.bb.f_82291_ - particleWidth) / 2.0;
            double d1 = (this.bb.f_82290_ + this.bb.f_82293_ - particleWidth) / 2.0;
            this.bb = new AABB(d0, this.bb.f_82289_, d1, d0 + this.bbWidth, this.bb.f_82289_ + this.bbHeight, d1 + this.bbWidth);
         }
      }
   }

   private static class PseudoParticleData {
      private final List<RenderNutritionalLiquifier.PseudoParticle> particles = new ArrayList<>();
      private long lastTick;
   }
}
