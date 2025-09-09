package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelScubaMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ScubaMaskArmor implements ICustomArmor, ResourceManagerReloadListener {
   public static final ScubaMaskArmor SCUBA_MASK = new ScubaMaskArmor();
   private ModelScubaMask model;

   private ScubaMaskArmor() {
   }

   public void m_6213_(@NotNull ResourceManager resourceManager) {
      this.model = new ModelScubaMask(Minecraft.m_91087_().m_167973_());
   }

   @Override
   public void render(
      HumanoidModel<? extends LivingEntity> baseModel,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      float partialTicks,
      boolean hasEffect,
      LivingEntity entity,
      ItemStack stack
   ) {
      if (baseModel.f_102808_.f_104207_) {
         if (baseModel.f_102610_) {
            matrix.m_85836_();
            if (baseModel.f_102007_) {
               float f = 1.5F / baseModel.f_102010_;
               matrix.m_85841_(f, f, f);
            }

            matrix.m_85837_(0.0, baseModel.f_170338_ / 16.0F, baseModel.f_170339_ / 16.0F);
            this.renderMask(baseModel, matrix, renderer, light, overlayLight, hasEffect);
            matrix.m_85849_();
         } else {
            this.renderMask(baseModel, matrix, renderer, light, overlayLight, hasEffect);
         }
      }
   }

   private void renderMask(
      HumanoidModel<? extends LivingEntity> baseModel,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      boolean hasEffect
   ) {
      matrix.m_85836_();
      baseModel.f_102808_.m_104299_(matrix);
      matrix.m_85837_(0.0, 0.0, 0.01);
      this.model.render(matrix, renderer, light, overlayLight, hasEffect);
      matrix.m_85849_();
   }
}
