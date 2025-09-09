package mekanism.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@ParametersAreNotNullByDefault
public class MekanismArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A> {
   public MekanismArmorLayer(RenderLayerParent<T, M> entityRenderer, A modelLeggings, A modelArmor, ModelManager manager) {
      super(entityRenderer, modelLeggings, modelArmor, manager);
   }

   public void m_6494_(
      PoseStack matrix,
      MultiBufferSource renderer,
      int packedLightIn,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      this.renderArmorPart(matrix, renderer, entity, EquipmentSlot.CHEST, packedLightIn, partialTicks);
      this.renderArmorPart(matrix, renderer, entity, EquipmentSlot.LEGS, packedLightIn, partialTicks);
      this.renderArmorPart(matrix, renderer, entity, EquipmentSlot.FEET, packedLightIn, partialTicks);
      this.renderArmorPart(matrix, renderer, entity, EquipmentSlot.HEAD, packedLightIn, partialTicks);
   }

   private void renderArmorPart(PoseStack matrix, MultiBufferSource renderer, T entity, EquipmentSlot slot, int light, float partialTicks) {
      ItemStack stack = entity.m_6844_(slot);
      Item item = stack.m_41720_();
      if (item instanceof ArmorItem armorItem && armorItem.m_40402_() == slot && IClientItemExtensions.of(item) instanceof ISpecialGear specialGear) {
         ICustomArmor model = specialGear.getGearModel(armorItem.m_266204_());
         A coreModel = (A)(slot == EquipmentSlot.LEGS ? this.f_117071_ : this.f_117072_);
         ((HumanoidModel)this.m_117386_()).m_102872_(coreModel);
         this.m_117125_(coreModel, slot);
         model.render(coreModel, matrix, renderer, light, OverlayTexture.f_118083_, partialTicks, stack.m_41790_(), entity, stack);
      }
   }
}
