package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.MekanismModelCache;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderRobit extends MobRenderer<EntityRobit, RenderRobit.RobitModelWrapper> {
   public RenderRobit(Context context) {
      super(context, new RenderRobit.RobitModelWrapper(), 0.5F);
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityRobit robit) {
      return RobitSpriteUploader.ATLAS_LOCATION;
   }

   public static class RobitModelWrapper extends EntityModel<EntityRobit> {
      @Nullable
      private EntityRobit robit;

      RobitModelWrapper() {
      }

      public void setupAnim(@NotNull EntityRobit robit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.robit = robit;
      }

      public void m_7695_(
         @NotNull PoseStack matrix, @NotNull VertexConsumer builder, int light, int overlayLight, float red, float green, float blue, float alpha
      ) {
         if (this.robit != null) {
            MekanismRobitSkins.SkinLookup skinLookup = MekanismRobitSkins.lookup(this.robit.m_9236_().m_9598_(), this.robit.getSkin());
            BakedModel model = MekanismModelCache.INSTANCE.getRobitSkin(skinLookup);
            if (model == null) {
               Mekanism.logger
                  .warn("Robit with skin: {} does not have a model. If this happened during a resource reload this can be ignored.", skinLookup.location());
            } else {
               matrix.m_85836_();
               matrix.m_252781_(Axis.f_252529_.m_252977_(180.0F));
               matrix.m_85837_(-0.5, -1.5, -0.5);
               Pose last = matrix.m_85850_();

               for (BakedQuad quad : model.getQuads(null, null, this.robit.m_9236_().f_46441_, this.robit.getModelData(), null)) {
                  builder.putBulkData(last, quad, red, green, blue, alpha, light, overlayLight, false);
               }

               matrix.m_85849_();
            }

            this.robit = null;
         }
      }
   }
}
