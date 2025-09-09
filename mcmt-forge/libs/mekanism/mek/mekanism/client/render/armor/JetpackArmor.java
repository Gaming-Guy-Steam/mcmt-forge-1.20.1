package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class JetpackArmor implements ICustomArmor, ResourceManagerReloadListener {
   public static final JetpackArmor JETPACK = new JetpackArmor(false);
   public static final JetpackArmor ARMORED_JETPACK = new JetpackArmor(true);
   private final boolean armored;
   private ModelJetpack model;

   private JetpackArmor(boolean armored) {
      this.armored = armored;
   }

   public void m_6213_(@NotNull ResourceManager resourceManager) {
      if (this.armored) {
         this.model = new ModelArmoredJetpack(Minecraft.m_91087_().m_167973_());
      } else {
         this.model = new ModelJetpack(Minecraft.m_91087_().m_167973_());
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
      if (baseModel.f_102810_.f_104207_) {
         if (baseModel.f_102610_) {
            matrix.m_85836_();
            float f1 = 1.0F / baseModel.f_102011_;
            matrix.m_85841_(f1, f1, f1);
            matrix.m_85837_(0.0, baseModel.f_102012_ / 16.0F, 0.0);
            this.renderJetpack(baseModel, matrix, renderer, light, overlayLight, hasEffect);
            matrix.m_85849_();
         } else {
            this.renderJetpack(baseModel, matrix, renderer, light, overlayLight, hasEffect);
         }
      }
   }

   private void renderJetpack(
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
