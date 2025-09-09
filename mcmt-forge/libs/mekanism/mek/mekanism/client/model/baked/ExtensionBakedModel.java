package mekanism.client.model.baked;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ExtensionBakedModel<T> extends BakedModelWrapper<BakedModel> {
   private final LoadingCache<ExtensionBakedModel.QuadsKey<T>, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      .build(new CacheLoader<ExtensionBakedModel.QuadsKey<T>, List<BakedQuad>>() {
         public List<BakedQuad> load(ExtensionBakedModel.QuadsKey<T> key) {
            return ExtensionBakedModel.this.createQuads(key);
         }
      });
   private final Map<List<BakedModel>, List<BakedModel>> cachedRenderPasses = new Object2ObjectOpenHashMap();

   public ExtensionBakedModel(BakedModel original) {
      super(original);
   }

   @Nullable
   protected ExtensionBakedModel.QuadsKey<T> createKey(ExtensionBakedModel.QuadsKey<T> key, ModelData data) {
      return key;
   }

   protected List<BakedQuad> createQuads(ExtensionBakedModel.QuadsKey<T> key) {
      List<BakedQuad> ret = key.getQuads();
      if (key.getTransformation() != null) {
         ret = QuadUtils.transformBakedQuads(ret, key.getTransformation());
      }

      return ret;
   }

   @NotNull
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
      List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
      ExtensionBakedModel.QuadsKey<T> key = this.createKey(new ExtensionBakedModel.QuadsKey<>(state, side, rand, renderType, quads), data);
      return key == null ? quads : (List)this.cache.getUnchecked(key);
   }

   public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
      return this.cachedRenderPasses.computeIfAbsent(super.getRenderPasses(stack, fabulous), original -> original.stream().map(this::wrapModel).toList());
   }

   protected ExtensionBakedModel<T> wrapModel(BakedModel model) {
      return new ExtensionBakedModel<>(model);
   }

   public static class LightedBakedModel extends ExtensionBakedModel.TransformedBakedModel<Void> {
      public LightedBakedModel(BakedModel original) {
         super(original, QuadTransformation.filtered_fullbright);
      }

      protected ExtensionBakedModel.LightedBakedModel wrapModel(BakedModel model) {
         return new ExtensionBakedModel.LightedBakedModel(model);
      }
   }

   public static class QuadsKey<T> {
      @Nullable
      private final BlockState state;
      @Nullable
      private final Direction side;
      private final RandomSource random;
      @Nullable
      private final RenderType layer;
      private final List<BakedQuad> quads;
      @Nullable
      private QuadTransformation transformation;
      @Nullable
      private T data;
      private int dataHash;
      @Nullable
      private BiPredicate<T, T> equality;

      public QuadsKey(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable RenderType layer, List<BakedQuad> quads) {
         this.state = state;
         this.side = side;
         this.random = random;
         this.layer = layer;
         this.quads = quads;
      }

      public ExtensionBakedModel.QuadsKey<T> transform(Supplier<? extends QuadTransformation> transformation) {
         return this.transform(transformation.get());
      }

      public ExtensionBakedModel.QuadsKey<T> transform(QuadTransformation transformation) {
         this.transformation = transformation;
         return this;
      }

      public ExtensionBakedModel.QuadsKey<T> data(T data, int dataHash, BiPredicate<T, T> equality) {
         this.data = data;
         this.dataHash = dataHash;
         this.equality = equality;
         return this;
      }

      @Nullable
      public BlockState getBlockState() {
         return this.state;
      }

      @Nullable
      public Direction getSide() {
         return this.side;
      }

      public RandomSource getRandom() {
         return this.random;
      }

      @Nullable
      public RenderType getLayer() {
         return this.layer;
      }

      public List<BakedQuad> getQuads() {
         return this.quads;
      }

      @Nullable
      public QuadTransformation getTransformation() {
         return this.transformation;
      }

      @Nullable
      public T getData() {
         return this.data;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.state, this.side, this.layer, this.transformation, this.dataHash);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof ExtensionBakedModel.QuadsKey<?> other)) {
            return false;
         } else if (this.side != other.side || this.layer != other.layer || !Objects.equals(this.state, other.state)) {
            return false;
         } else {
            return this.transformation != null && !this.transformation.equals(other.transformation)
               ? false
               : this.data == null || this.equality != null && this.equality.test(this.data, (T)other.getData());
         }
      }
   }

   public static class TransformedBakedModel<T> extends ExtensionBakedModel<T> {
      private final QuadTransformation transform;

      public TransformedBakedModel(BakedModel original, QuadTransformation transform) {
         super(original);
         this.transform = transform;
      }

      @Deprecated
      public List<BakedQuad> m_213637_(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
         return QuadUtils.transformBakedQuads(super.m_213637_(state, side, rand), this.transform);
      }

      public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack mat, boolean applyLeftHandTransform) {
         super.applyTransform(displayContext, mat, applyLeftHandTransform);
         return this;
      }

      @Nullable
      @Override
      protected ExtensionBakedModel.QuadsKey<T> createKey(ExtensionBakedModel.QuadsKey<T> key, ModelData data) {
         return key.transform(this.transform);
      }

      protected ExtensionBakedModel.TransformedBakedModel<T> wrapModel(BakedModel model) {
         return new ExtensionBakedModel.TransformedBakedModel<>(model, this.transform);
      }
   }
}
