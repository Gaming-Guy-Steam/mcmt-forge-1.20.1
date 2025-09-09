package mekanism.client.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ModelDataBakedModel extends BakedModelWrapper<BakedModel> {
   private final ModelData modelData;
   private final List<BakedModel> renderPasses;

   public ModelDataBakedModel(BakedModel original, ModelData data) {
      super(original);
      this.modelData = data;
      this.renderPasses = Collections.singletonList(this);
   }

   @Deprecated
   public List<BakedQuad> m_213637_(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      return this.getQuads(state, side, rand, this.modelData, null);
   }

   @Deprecated
   public TextureAtlasSprite m_6160_() {
      return this.getParticleIcon(this.modelData);
   }

   public ItemOverrides m_7343_() {
      return ItemOverrides.f_111734_;
   }

   public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack mat, boolean applyLeftHandTransform) {
      super.applyTransform(displayContext, mat, applyLeftHandTransform);
      return this;
   }

   public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
      return this.renderPasses;
   }
}
