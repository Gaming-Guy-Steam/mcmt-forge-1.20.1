package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelScubaTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ScubaTankArmor implements ICustomArmor, ResourceManagerReloadListener {
   public static final ScubaTankArmor SCUBA_TANK = new ScubaTankArmor();
   private ModelScubaTank model;

   private ScubaTankArmor() {
   }

   public void m_6213_(@NotNull ResourceManager resourceManager) {
      this.model = new ModelScubaTank(Minecraft.m_91087_().m_167973_());
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
      if (baseModel.f_102810_.f_104207_) {
         if (baseModel.f_102610_) {
            matrix.m_85836_();
            float f1 = 1.0F / baseModel.f_102011_;
            matrix.m_85841_(f1, f1, f1);
            matrix.m_85837_(0.0, baseModel.f_102012_ / 16.0F, 0.0);
            this.renderTank(baseModel, matrix, renderer, light, overlayLight, hasEffect);
            matrix.m_85849_();
         } else {
            this.renderTank(baseModel, matrix, renderer, light, overlayLight, hasEffect);
         }
      }
   }

   private void renderTank(
      HumanoidModel<? extends LivingEntity> baseModel,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      boolean hasEffect
   ) {
      matrix.m_85836_();
      baseModel.f_102810_.m_104299_(matrix);
      matrix.m_85837_(0.0, 0.0, 0.06);
      this.model.render(matrix, renderer, light, overlayLight, hasEffect);
      matrix.m_85849_();
   }
}
