package mekanism.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismISTER extends BlockEntityWithoutLevelRenderer {
   protected MekanismISTER() {
      super(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_());
   }

   protected EntityModelSet getEntityModels() {
      return Minecraft.m_91087_().m_167973_();
   }

   protected BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
      return Minecraft.m_91087_().m_167982_();
   }

   protected Camera getCamera() {
      return this.getBlockEntityRenderDispatcher().f_112249_;
   }

   public abstract void m_6213_(@NotNull ResourceManager resourceManager);

   public abstract void m_108829_(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext displayContext,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight
   );

   protected void renderBlockItem(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext displayContext,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      ModelData modelData
   ) {
      if (stack.m_41720_() instanceof BlockItem blockItem) {
         Block var28 = blockItem.m_40614_();
         boolean fabulous;
         if (displayContext != ItemDisplayContext.GUI && !displayContext.m_269069_()) {
            fabulous = !(var28 instanceof HalfTransparentBlock) && !(var28 instanceof StainedGlassPaneBlock);
         } else {
            fabulous = true;
         }

         Minecraft minecraft = Minecraft.m_91087_();
         ItemRenderer itemRenderer = minecraft.m_91291_();
         BlockState defaultState = var28.m_49966_();
         BakedModel baseModel = minecraft.m_91304_().m_119430_().m_110893_(defaultState);
         long seed = 42L;
         RandomSource random = RandomSource.m_216327_();
         boolean hasEffect = stack.m_41790_();

         for (BakedModel model : baseModel.getRenderPasses(stack, fabulous)) {
            for (RenderType renderType : model.getRenderTypes(stack, fabulous)) {
               VertexConsumer buffer;
               if (fabulous) {
                  buffer = ItemRenderer.m_115222_(renderer, renderType, true, hasEffect);
               } else {
                  buffer = ItemRenderer.m_115211_(renderer, renderType, true, hasEffect);
               }

               for (Direction direction : EnumUtils.DIRECTIONS) {
                  random.m_188584_(seed);
                  itemRenderer.m_115162_(matrix, buffer, model.getQuads(defaultState, direction, random, modelData, renderType), stack, light, overlayLight);
               }

               random.m_188584_(seed);
               itemRenderer.m_115162_(matrix, buffer, model.getQuads(defaultState, null, random, modelData, renderType), stack, light, overlayLight);
            }
         }
      }
   }
}
