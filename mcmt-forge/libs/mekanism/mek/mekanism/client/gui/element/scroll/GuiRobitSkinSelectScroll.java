package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.api.robit.RobitSkin;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class GuiRobitSkinSelectScroll extends GuiElement {
   private static final int SLOT_DIMENSIONS = 48;
   private static final int SLOT_COUNT = 3;
   private static final int INNER_DIMENSIONS = 144;
   private final GuiScrollBar scrollBar;
   private final Supplier<List<ResourceKey<RobitSkin>>> unlockedSkins;
   private final EntityRobit robit;
   private ResourceKey<RobitSkin> selectedSkin;
   private float rotation;
   private int ticks;

   public GuiRobitSkinSelectScroll(IGuiWrapper gui, int x, int y, EntityRobit robit, Supplier<List<ResourceKey<RobitSkin>>> unlockedSkins) {
      super(gui, x, y, 156, 144);
      this.robit = robit;
      this.selectedSkin = this.robit.getSkin();
      this.unlockedSkins = unlockedSkins;
      this.scrollBar = this.addChild(
         new GuiScrollBar(
            gui,
            this.relativeX + 144,
            this.relativeY,
            144,
            () -> this.getUnlockedSkins() == null ? 0 : (int)Math.ceil(this.getUnlockedSkins().size() / 3.0),
            () -> 3
         )
      );
   }

   private List<ResourceKey<RobitSkin>> getUnlockedSkins() {
      return this.unlockedSkins.get();
   }

   public ResourceKey<RobitSkin> getSelectedSkin() {
      return this.selectedSkin;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      List<ResourceKey<RobitSkin>> skins = this.getUnlockedSkins();
      if (skins != null) {
         Lighting.m_84930_();
         int index = this.ticks / 10;
         float oldRot = this.rotation;
         this.rotation = Mth.m_14177_(this.rotation - 0.5F);
         float rot = Mth.m_14189_(partialTicks, oldRot, this.rotation);
         QuadTransformation rotation = QuadTransformation.rotateY(rot);
         int slotStart = this.scrollBar.getCurrentSelection() * 3;
         int max = 9;

         for (int i = 0; i < max; i++) {
            int slotX = this.relativeX + i % 3 * 48;
            int slotY = this.relativeY + i / 3 * 48;
            int slot = slotStart + i;
            if (slot < skins.size()) {
               ResourceKey<RobitSkin> skin = skins.get(slot);
               if (skin == this.selectedSkin) {
                  renderSlotBackground(guiGraphics, slotX, slotY, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE);
               } else {
                  renderSlotBackground(guiGraphics, slotX, slotY, GuiElementHolder.HOLDER, 32);
               }

               this.renderRobit(guiGraphics, skins.get(slot), slotX, slotY, rotation, index);
            } else {
               renderSlotBackground(guiGraphics, slotX, slotY, GuiElementHolder.HOLDER, 32);
            }
         }

         Lighting.m_84931_();
      }
   }

   private static void renderSlotBackground(@NotNull GuiGraphics guiGraphics, int slotX, int slotY, ResourceLocation resource, int size) {
      GuiUtils.renderBackgroundTexture(guiGraphics, resource, size, size, slotX, slotY, 48, 48, 256, 256);
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      List<ResourceKey<RobitSkin>> skins = this.getUnlockedSkins();
      if (skins != null) {
         int xAxis = mouseX - this.getGuiLeft();
         int yAxis = mouseY - this.getGuiTop();
         int slotX = (xAxis - this.relativeX) / 48;
         int slotY = (yAxis - this.relativeY) / 48;
         if (slotX >= 0 && slotY >= 0 && slotX < 3 && slotY < 3) {
            int slotStartX = this.relativeX + slotX * 48;
            int slotStartY = this.relativeY + slotY * 48;
            if (xAxis >= slotStartX && xAxis < slotStartX + 48 && yAxis >= slotStartY && yAxis < slotStartY + 48) {
               int slot = (slotY + this.scrollBar.getCurrentSelection()) * 3 + slotX;
               if (this.checkWindows(mouseX, mouseY, slot < skins.size())) {
                  guiGraphics.m_285944_(RenderType.m_286086_(), slotStartX, slotStartY, slotStartX + 48, slotStartY + 48, 1895819776);
               }
            }
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      this.ticks++;
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      ResourceKey<RobitSkin> skin = this.getSkin(mouseX, mouseY);
      if (skin != null) {
         this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.ROBIT_SKIN.translate(new Object[]{RobitSkin.getTranslatedName(skin)})});
      }
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return this.scrollBar.adjustScroll(delta) || super.m_6050_(mouseX, mouseY, delta);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      ResourceKey<RobitSkin> skin = this.getSkin(mouseX, mouseY);
      if (skin != null) {
         this.selectedSkin = skin;
      }
   }

   private ResourceKey<RobitSkin> getSkin(double mouseX, double mouseY) {
      List<ResourceKey<RobitSkin>> skins = this.getUnlockedSkins();
      if (skins != null) {
         int slotX = (int)((mouseX - this.m_252754_()) / 48.0);
         int slotY = (int)((mouseY - this.m_252907_()) / 48.0);
         if (slotX >= 0 && slotY >= 0 && slotX < 3 && slotY < 3) {
            int slot = (slotY + this.scrollBar.getCurrentSelection()) * 3 + slotX;
            if (slot < skins.size()) {
               return skins.get(slot);
            }
         }
      }

      return null;
   }

   private void renderRobit(GuiGraphics guiGraphics, ResourceKey<RobitSkin> skinKey, int x, int y, QuadTransformation rotation, int index) {
      MekanismRobitSkins.SkinLookup skinLookup = MekanismRobitSkins.lookup(this.robit.m_9236_().m_9598_(), skinKey);
      List<ResourceLocation> textures = skinLookup.skin().textures();
      if (textures.isEmpty()) {
         Mekanism.logger.error("Failed to render skin: {}, as it has no textures.", skinLookup.location());
      } else {
         BakedModel model = MekanismModelCache.INSTANCE.getRobitSkin(skinLookup);
         if (model == null) {
            Mekanism.logger.warn("Failed to render skin: {} as it does not have a model.", skinLookup.location());
         } else {
            BufferSource buffer = guiGraphics.m_280091_();
            VertexConsumer builder = buffer.m_6299_(RobitSpriteUploader.RENDER_TYPE);
            PoseStack pose = guiGraphics.m_280168_();
            pose.m_85836_();
            pose.m_252880_(x + 48, y + 38, 0.0F);
            pose.m_85841_(48.0F, 48.0F, 48.0F);
            pose.m_252781_(Axis.f_252403_.m_252977_(180.0F));
            Pose matrixEntry = pose.m_85850_();
            ModelData modelData = ModelData.builder().with(EntityRobit.SKIN_TEXTURE_PROPERTY, MathUtils.getByIndexMod(textures, index)).build();
            List<BakedQuad> quads = model.getQuads(null, null, this.robit.m_9236_().f_46441_, modelData, null);

            for (BakedQuad quad : QuadUtils.transformBakedQuads(quads, rotation)) {
               builder.m_85987_(matrixEntry, quad, 1.0F, 1.0F, 1.0F, 15728880, OverlayTexture.f_118083_);
            }

            buffer.m_109912_(RobitSpriteUploader.RENDER_TYPE);
            pose.m_85849_();
         }
      }
   }
}
