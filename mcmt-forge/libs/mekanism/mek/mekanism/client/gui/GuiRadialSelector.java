package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.client.render.lib.ScrollIncrementer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.network.to_server.PacketRadialModeChange;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiRadialSelector extends Screen {
   private static final ResourceLocation BACK_BUTTON = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, "back.png");
   private static final float DRAWS = 300.0F;
   private static final float INNER = 40.0F;
   private static final float OUTER = 100.0F;
   private static final float MIDDLE_DISTANCE = 70.0F;
   private static final float SELECT_RADIUS = 10.0F;
   private static final float SELECT_RADIUS_WITH_PARENT = 20.0F;
   private final ScrollIncrementer scrollIncrementer = new ScrollIncrementer(true);
   private final Deque<RadialData<?>> parents = new ArrayDeque<>();
   private final Supplier<Player> playerSupplier;
   private final EquipmentSlot slot;
   @NotNull
   private RadialData<?> radialData;
   private IRadialMode selection = null;
   private boolean overBackButton = false;
   private boolean updateOnClose = true;

   public GuiRadialSelector(EquipmentSlot slot, @NotNull RadialData<?> radialData, Supplier<Player> playerSupplier) {
      super(MekanismLang.RADIAL_SCREEN.translate(new Object[0]));
      this.slot = slot;
      this.radialData = radialData;
      this.playerSupplier = playerSupplier;
   }

   public void m_88315_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      float centerX = guiGraphics.m_280182_() / 2.0F;
      float centerY = guiGraphics.m_280206_() / 2.0F;
      this.render(guiGraphics, mouseX, mouseY, centerX, centerY, this.radialData);
   }

   private <MODE extends IRadialMode> void render(
      @NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float centerX, float centerY, RadialData<MODE> radialData
   ) {
      List<MODE> modes = radialData.getModes();
      int activeModes = modes.size();
      if (activeModes == 0) {
         RadialData<?> parent = this.parents.pollLast();
         if (parent == null) {
            this.m_7379_();
         } else {
            this.radialData = parent;
         }
      } else {
         float angleSize = 360.0F / activeModes;
         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         pose.m_252880_(centerX, centerY, 0.0F);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         this.drawTorus(guiGraphics, 0.0F, 360.0F, 0.3F, 0.3F, 0.3F, 0.5F);
         MODE current = this.getCurrent(radialData);
         if (current == null) {
            current = radialData.getDefaultMode(modes);
         }

         int section = radialData.indexNullable(modes, current);
         if (current != null && section != -1) {
            float startAngle = -90.0F + 360.0F * (-0.5F + section) / activeModes;
            EnumColor color = current.color();
            if (color == null) {
               this.drawTorus(guiGraphics, startAngle, angleSize, 0.4F, 0.4F, 0.4F, 0.7F);
            } else {
               this.drawTorus(guiGraphics, startAngle, angleSize, color.getColor(0), color.getColor(1), color.getColor(2), 0.3F);
            }
         }

         double xDiff = mouseX - centerX;
         double yDiff = mouseY - centerY;
         double distanceFromCenter = Mth.m_184645_(xDiff, yDiff);
         if (distanceFromCenter > (this.parents.isEmpty() ? 10.0F : 20.0F)) {
            float angle = (float)(180.0F / (float)Math.PI * Mth.m_14136_(yDiff, xDiff));
            float modeSize = 180.0F / activeModes;
            this.drawTorus(guiGraphics, angle - modeSize, angleSize, 0.8F, 0.8F, 0.8F, 0.3F);
            float selectionAngle = StatUtils.wrapDegrees(angle + modeSize + 90.0F);
            int selectionDrawnPos = (int)(selectionAngle * (activeModes / 360.0F));
            this.selection = modes.get(selectionDrawnPos);
            this.drawTorus(guiGraphics, -90.0F + 360.0F * (-0.5F + selectionDrawnPos) / activeModes, angleSize, 0.6F, 0.6F, 0.6F, 0.7F);
         } else {
            this.selection = null;
         }

         record PositionedText(float x, float y, Component text) {
         }

         List<PositionedText> textToDraw = new ArrayList<>(this.parents.isEmpty() ? activeModes : activeModes + 1);
         if (!this.parents.isEmpty()) {
            this.overBackButton = distanceFromCenter <= 20.0;
            if (this.overBackButton) {
               this.drawTorus(guiGraphics, 0.0F, 360.0F, 0.0F, 20.0F, 0.8F, 0.8F, 0.8F, 0.3F);
            } else {
               this.drawTorus(guiGraphics, 0.0F, 360.0F, 0.0F, 20.0F, 0.3F, 0.3F, 0.3F, 0.5F);
            }

            guiGraphics.m_280411_(BACK_BUTTON, -12, -18, 24, 24, 0.0F, 0.0F, 18, 18, 18, 18);
            textToDraw.add(new PositionedText(0.0F, 0.0F, MekanismLang.BACK.translate(new Object[0])));
         } else {
            this.overBackButton = false;
         }

         int position = 0;

         for (MODE mode : modes) {
            float degrees = 270.0F + 360.0F * ((float)(position++) / activeModes);
            float angle = (float) (Math.PI / 180.0) * degrees;
            float x = Mth.m_14089_(angle) * 70.0F;
            float y = Mth.m_14031_(angle) * 70.0F;
            guiGraphics.m_280411_(mode.icon(), Math.round(x - 12.0F), Math.round(y - 20.0F), 24, 24, 0.0F, 0.0F, 18, 18, 18, 18);
            textToDraw.add(new PositionedText(x, y, mode.sliceName()));
         }

         boolean whiteRadialText = MekanismConfig.client.whiteRadialText.get();

         for (PositionedText toDraw : textToDraw) {
            pose.m_85836_();
            pose.m_252880_(toDraw.x, toDraw.y, 0.0F);
            pose.m_85841_(0.6F, 0.6F, 0.6F);
            Component text = toDraw.text;
            if (whiteRadialText) {
               text = text.m_6881_().m_130940_(ChatFormatting.RESET);
            }

            GuiUtils.drawString(guiGraphics, this.f_96547_, text, -this.f_96547_.m_92852_(text) / 2.0F, 8.0F, -855638017, true);
            pose.m_85849_();
         }

         pose.m_85849_();
      }
   }

   public void m_7861_() {
      if (this.updateOnClose) {
         this.updateSelection(this.radialData);
      }

      super.m_7861_();
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      return true;
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return delta != 0.0 && this.mouseScrolled(this.radialData, this.scrollIncrementer.scroll(delta)) || super.m_6050_(mouseX, mouseY, delta);
   }

   private <MODE extends IRadialMode> boolean mouseScrolled(RadialData<MODE> radialData, int shift) {
      if (shift == 0) {
         return true;
      } else {
         List<MODE> modes = radialData.getModes();
         if (!modes.isEmpty()) {
            MODE current = this.getCurrent(radialData);
            int index = radialData.indexNullable(modes, current);
            if (index != -1) {
               this.selection = MathUtils.getByIndexMod(modes, index + shift);
               this.updateSelection(radialData);
               return true;
            }
         }

         return false;
      }
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      this.updateSelection(this.radialData);
      return true;
   }

   public boolean m_7043_() {
      return false;
   }

   private void drawTorus(GuiGraphics guiGraphics, float startAngle, float sizeAngle, float red, float green, float blue, float alpha) {
      this.drawTorus(guiGraphics, startAngle, sizeAngle, 40.0F, 100.0F, red, green, blue, alpha);
   }

   private void drawTorus(GuiGraphics guiGraphics, float startAngle, float sizeAngle, float inner, float outer, float red, float green, float blue, float alpha) {
      RenderSystem.setShader(GameRenderer::m_172811_);
      BufferBuilder vertexBuffer = Tesselator.m_85913_().m_85915_();
      vertexBuffer.m_166779_(Mode.TRIANGLE_STRIP, DefaultVertexFormat.f_85815_);
      Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
      float draws = 300.0F * (sizeAngle / 360.0F);

      for (int i = 0; i <= draws; i++) {
         float degrees = startAngle + i / 300.0F * 360.0F;
         float angle = (float) (Math.PI / 180.0) * degrees;
         float cos = Mth.m_14089_(angle);
         float sin = Mth.m_14031_(angle);
         vertexBuffer.m_252986_(matrix4f, outer * cos, outer * sin, 0.0F).m_85950_(red, green, blue, alpha).m_5752_();
         vertexBuffer.m_252986_(matrix4f, inner * cos, inner * sin, 0.0F).m_85950_(red, green, blue, alpha).m_5752_();
      }

      BufferUploader.m_231202_(vertexBuffer.m_231175_());
   }

   @Nullable
   private <MODE extends IRadialMode> MODE getCurrent(RadialData<MODE> radialData) {
      Player player = this.playerSupplier.get();
      if (player != null) {
         ItemStack stack = player.m_6844_(this.slot);
         if (stack.m_41720_() instanceof IGenericRadialModeItem item) {
            return item.getMode(stack, radialData);
         }
      }

      return null;
   }

   private <MODE extends IRadialMode> void updateSelection(final RadialData<MODE> radialData) {
      if (this.selection != null && this.playerSupplier.get() != null) {
         if (this.selection instanceof INestedRadialMode nested && nested.hasNestedData()) {
            this.parents.push(radialData);
            this.radialData = nested.nestedData();
            this.selection = null;
         } else if (!this.selection.equals(this.getCurrent(radialData))) {
            List<ResourceLocation> path = new ArrayList<>(this.parents.size());
            RadialData<?> previousParent = null;

            for (RadialData<?> parent : this.parents) {
               if (previousParent != null) {
                  path.add(parent.getIdentifier());
               }

               previousParent = parent;
            }

            if (previousParent != null) {
               path.add(radialData.getIdentifier());
            }

            int networkRepresentation = radialData.tryGetNetworkRepresentation(this.selection);
            if (networkRepresentation != -1) {
               Mekanism.packetHandler().sendToServer(new PacketRadialModeChange(this.slot, path, networkRepresentation));
            }
         }
      } else if (this.overBackButton) {
         this.overBackButton = false;
         RadialData<?> parent = this.parents.pollLast();
         if (parent != null) {
            this.radialData = parent;
         }
      }
   }

   public boolean hasMatchingData(EquipmentSlot slot, RadialData<?> data) {
      if (this.slot == slot) {
         RadialData<?> firstData = this.parents.peekFirst();
         return firstData == null ? this.radialData.equals(data) : firstData.equals(data);
      } else {
         return false;
      }
   }

   public void tryInheritCurrentPath(@Nullable Screen screen) {
      if (screen instanceof GuiRadialSelector old) {
         RadialData<?> previousParent = null;

         for (RadialData<?> parent : old.parents) {
            if (previousParent != null && this.radialData.getIdentifier().equals(previousParent.getIdentifier())) {
               INestedRadialMode nestedMode = this.radialData.fromIdentifier(parent.getIdentifier());
               if (nestedMode == null || !nestedMode.hasNestedData()) {
                  return;
               }

               this.parents.push(this.radialData);
               this.radialData = nestedMode.nestedData();
            }

            previousParent = parent;
         }

         if (previousParent != null && this.radialData.getIdentifier().equals(previousParent.getIdentifier())) {
            INestedRadialMode nestedMode = this.radialData.fromIdentifier(old.radialData.getIdentifier());
            if (nestedMode != null && nestedMode.hasNestedData()) {
               this.parents.push(this.radialData);
               this.radialData = nestedMode.nestedData();
               old.updateOnClose = false;
            }
         }
      }
   }

   public boolean shouldHideCrosshair() {
      return !this.parents.isEmpty();
   }
}
