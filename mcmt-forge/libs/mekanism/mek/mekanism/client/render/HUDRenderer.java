package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Predicate;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiUtils;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.HUDElement;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HUDRenderer {
   private static final EquipmentSlot[] EQUIPMENT_ORDER = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
   };
   private static final ResourceLocation[] ARMOR_ICONS = new ResourceLocation[]{
      MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "hud_mekasuit_helmet.png"),
      MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "hud_mekasuit_chest.png"),
      MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "hud_mekasuit_leggings.png"),
      MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "hud_mekasuit_boots.png")
   };
   private static final ResourceLocation TOOL_ICON = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "hud_mekatool.png");
   private static final ResourceLocation COMPASS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "compass.png");
   private long lastTick = -1L;
   private float prevRotationYaw;
   private float prevRotationPitch;

   public void renderHUD(
      Minecraft minecraft, GuiGraphics guiGraphics, Font font, float partialTick, int screenWidth, int screenHeight, int maxTextHeight, boolean reverseHud
   ) {
      Player player = minecraft.f_91074_;
      this.update(minecraft.f_91073_, player);
      if (!(MekanismConfig.client.hudOpacity.get() < 0.05F)) {
         int color = HUDElement.HUDColor.REGULAR.getColorARGB();
         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         float yawJitter = -absSqrt(player.f_20885_ - this.prevRotationYaw);
         float pitchJitter = -absSqrt(player.m_146909_() - this.prevRotationPitch);
         pose.m_252880_(yawJitter, pitchJitter, 0.0F);
         if (MekanismConfig.client.hudCompassEnabled.get()) {
            this.renderCompass(player, font, guiGraphics, partialTick, screenWidth, screenHeight, maxTextHeight, reverseHud, color);
         }

         this.renderMekaSuitEnergyIcons(player, font, guiGraphics, color);
         this.renderMekaSuitModuleIcons(player, font, guiGraphics, screenWidth, screenHeight, reverseHud, color);
         pose.m_85849_();
      }
   }

   private void update(Level level, Player player) {
      if (this.lastTick == -1L || level.m_46467_() - this.lastTick > 1L) {
         this.prevRotationYaw = player.m_146908_();
         this.prevRotationPitch = player.m_146909_();
      }

      this.lastTick = level.m_46467_();
      float yawDiff = player.f_20885_ - this.prevRotationYaw;
      float pitchDiff = player.m_146909_() - this.prevRotationPitch;
      float jitter = MekanismConfig.client.hudJitter.get();
      this.prevRotationYaw += yawDiff / jitter;
      this.prevRotationPitch += pitchDiff / jitter;
   }

   private static float absSqrt(float val) {
      float ret = (float)Math.sqrt(Math.abs(val));
      return val < 0.0F ? -ret : ret;
   }

   private void renderMekaSuitEnergyIcons(Player player, Font font, GuiGraphics guiGraphics, int color) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(10.0F, 10.0F, 0.0F);
      int posX = 0;
      Predicate<Item> showArmorPercent = item -> item instanceof ItemMekaSuitArmor;

      for (int i = 0; i < EnumUtils.ARMOR_SLOTS.length; i++) {
         posX += this.renderEnergyIcon(player, font, guiGraphics, posX, color, ARMOR_ICONS[i], EnumUtils.ARMOR_SLOTS[i], showArmorPercent);
      }

      Predicate<Item> showToolPercent = item -> item instanceof ItemMekaTool;

      for (EquipmentSlot hand : EnumUtils.HAND_SLOTS) {
         posX += this.renderEnergyIcon(player, font, guiGraphics, posX, color, TOOL_ICON, hand, showToolPercent);
      }

      pose.m_85849_();
   }

   private int renderEnergyIcon(
      Player player, Font font, GuiGraphics guiGraphics, int posX, int color, ResourceLocation icon, EquipmentSlot slot, Predicate<Item> showPercent
   ) {
      ItemStack stack = player.m_6844_(slot);
      if (showPercent.test(stack.m_41720_())) {
         this.renderHUDElement(font, guiGraphics, posX, 0, IModuleHelper.INSTANCE.hudElementPercent(icon, StorageUtils.getEnergyRatio(stack)), color, false);
         return 48;
      } else {
         return 0;
      }
   }

   private void renderMekaSuitModuleIcons(Player player, Font font, GuiGraphics guiGraphics, int screenWidth, int screenHeight, boolean reverseHud, int color) {
      int startX = screenWidth - 10;
      int curY = screenHeight - 10;
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();

      for (EquipmentSlot type : EQUIPMENT_ORDER) {
         ItemStack stack = player.m_6844_(type);
         if (stack.m_41720_() instanceof IModuleContainerItem item) {
            for (IHUDElement element : item.getHUDElements(player, stack)) {
               curY -= 18;
               if (reverseHud) {
                  this.renderHUDElement(font, guiGraphics, 10, curY, element, color, false);
               } else {
                  int elementWidth = 24 + font.m_92852_(element.getText());
                  this.renderHUDElement(font, guiGraphics, startX - elementWidth, curY, element, color, true);
               }
            }
         }
      }

      pose.m_85849_();
   }

   private void renderHUDElement(Font font, GuiGraphics guiGraphics, int x, int y, IHUDElement element, int color, boolean iconRight) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      MekanismRenderer.color(guiGraphics, color);
      guiGraphics.m_280163_(element.getIcon(), iconRight ? x + font.m_92852_(element.getText()) + 2 : x, y, 0.0F, 0.0F, 16, 16, 16, 16);
      MekanismRenderer.resetColor(guiGraphics);
      guiGraphics.m_280614_(font, element.getText(), iconRight ? x : x + 18, y + 5, element.getColor(), false);
   }

   private void renderCompass(
      Player player, Font font, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight, int maxTextHeight, boolean reverseHud, int color
   ) {
      int posX = reverseHud ? screenWidth - 125 : 25;
      int posY = Math.min(screenHeight - 20, maxTextHeight) - 80;
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(posX + 50, posY + 50, 0.0F);
      pose.m_85836_();
      pose.m_85836_();
      pose.m_85841_(0.7F, 0.7F, 0.7F);
      Component coords = MekanismLang.GENERIC_BLOCK_POS.translate(new Object[]{player.m_146903_(), player.m_146904_(), player.m_146907_()});
      GuiUtils.drawString(guiGraphics, font, coords, -font.m_92852_(coords) / 2.0F, -4.0F, color, false);
      pose.m_85849_();
      float angle = 180.0F - player.m_5675_(partialTick);
      pose.m_252781_(Axis.f_252529_.m_252977_(-60.0F));
      pose.m_252781_(Axis.f_252403_.m_252977_(angle));
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      MekanismRenderer.color(guiGraphics, color);
      guiGraphics.m_280411_(COMPASS, -50, -50, 100, 100, 0.0F, 0.0F, 256, 256, 256, 256);
      this.rotateStr(font, guiGraphics, MekanismLang.NORTH_SHORT, angle, 0.0F, color);
      this.rotateStr(font, guiGraphics, MekanismLang.EAST_SHORT, angle, 90.0F, color);
      this.rotateStr(font, guiGraphics, MekanismLang.SOUTH_SHORT, angle, 180.0F, color);
      this.rotateStr(font, guiGraphics, MekanismLang.WEST_SHORT, angle, 270.0F, color);
      MekanismRenderer.resetColor(guiGraphics);
      pose.m_85849_();
      pose.m_85849_();
   }

   private void rotateStr(Font font, GuiGraphics guiGraphics, ILangEntry langEntry, float rotation, float shift, int color) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252781_(Axis.f_252403_.m_252977_(shift));
      pose.m_252880_(0.0F, -50.0F, 0.0F);
      pose.m_252781_(Axis.f_252403_.m_252977_(-rotation - shift));
      GuiUtils.drawString(guiGraphics, font, langEntry.translate(), -2.5F, -4.0F, color, false);
      pose.m_85849_();
   }
}
