package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.armor.ICustomArmor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public record MekanismCurioRenderer(ICustomArmor model) implements ICurioRenderer {
   public <T extends LivingEntity, M extends EntityModel<T>> void render(
      ItemStack stack,
      SlotContext slotContext,
      PoseStack matrixStack,
      RenderLayerParent<T, M> renderLayerParent,
      MultiBufferSource renderTypeBuffer,
      int light,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (renderLayerParent.m_7200_() instanceof HumanoidModel<?> humanoidModel) {
         this.model
            .render(
               (HumanoidModel<? extends LivingEntity>)humanoidModel,
               matrixStack,
               renderTypeBuffer,
               light,
               OverlayTexture.f_118083_,
               partialTicks,
               stack.m_41790_(),
               slotContext.entity(),
               stack
            );
      }
   }
}
