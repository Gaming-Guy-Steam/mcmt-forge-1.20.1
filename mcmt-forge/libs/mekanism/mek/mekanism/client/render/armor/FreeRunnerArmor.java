package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FreeRunnerArmor implements ICustomArmor, ResourceManagerReloadListener {
   public static final FreeRunnerArmor FREE_RUNNERS = new FreeRunnerArmor(false);
   public static final FreeRunnerArmor ARMORED_FREE_RUNNERS = new FreeRunnerArmor(true);
   private final boolean armored;
   private ModelFreeRunners model;

   private FreeRunnerArmor(boolean armored) {
      this.armored = armored;
   }

   public void m_6213_(@NotNull ResourceManager resourceManager) {
      if (this.armored) {
         this.model = new ModelArmoredFreeRunners(Minecraft.m_91087_().m_167973_());
      } else {
         this.model = new ModelFreeRunners(Minecraft.m_91087_().m_167973_());
      }
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
      if (baseModel.f_102610_) {
         matrix.m_85836_();
         float f1 = 1.0F / baseModel.f_102011_;
         matrix.m_85841_(f1, f1, f1);
         matrix.m_85837_(0.0, baseModel.f_102012_ / 16.0F, 0.0);
         this.renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, true);
         this.renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, false);
         matrix.m_85849_();
      } else {
         this.renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, true);
         this.renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, false);
      }
   }

   private void renderLeg(
      HumanoidModel<? extends LivingEntity> baseModel,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      boolean hasEffect,
      boolean left
   ) {
      if ((!left || baseModel.f_102814_.f_104207_) && (left || baseModel.f_102813_.f_104207_)) {
         matrix.m_85836_();
         if (left) {
            baseModel.f_102814_.m_104299_(matrix);
         } else {
            baseModel.f_102813_.m_104299_(matrix);
         }

         matrix.m_85837_(0.0, 0.0, 0.06);
         matrix.m_85841_(1.02F, 1.02F, 1.02F);
         matrix.m_85837_(left ? -0.1375 : 0.1375, -0.75, -0.0625);
         this.model.renderLeg(matrix, renderer, light, overlayLight, hasEffect, left);
         matrix.m_85849_();
      }
   }
}
