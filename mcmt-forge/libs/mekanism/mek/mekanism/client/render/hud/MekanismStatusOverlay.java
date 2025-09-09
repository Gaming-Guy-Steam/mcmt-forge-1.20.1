package mekanism.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.GuiUtils;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class MekanismStatusOverlay implements IGuiOverlay {
   public static final MekanismStatusOverlay INSTANCE = new MekanismStatusOverlay();
   private int modeSwitchTimer = 0;
   private long lastTick;

   private MekanismStatusOverlay() {
   }

   public void setTimer() {
      this.modeSwitchTimer = 100;
   }

   public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
      Minecraft minecraft = gui.getMinecraft();
      if (!minecraft.f_91066_.f_92062_ && this.modeSwitchTimer > 1 && minecraft.f_91074_ != null) {
         ItemStack stack = minecraft.f_91074_.m_21205_();
         if (IModeItem.isModeItem(stack, EquipmentSlot.MAINHAND)) {
            Component scrollTextComponent = ((IModeItem)stack.m_41720_()).getScrollTextComponent(stack);
            if (scrollTextComponent != null) {
               Color color = Color.rgbad(1.0, 1.0, 1.0, this.modeSwitchTimer / 100.0F);
               Font font = gui.m_93082_();
               int componentWidth = font.m_92852_(scrollTextComponent);
               int targetShift = Math.max(59, Math.max(gui.leftHeight, gui.rightHeight));
               if (minecraft.f_91072_ != null && !minecraft.f_91072_.m_105205_()) {
                  targetShift -= 14;
               } else if (gui.f_92991_ > 0) {
                  targetShift += 14;
               }

               targetShift += 13;
               PoseStack pose = guiGraphics.m_280168_();
               pose.m_85836_();
               pose.m_252880_((screenWidth - componentWidth) / 2.0F, screenHeight - targetShift, 0.0F);
               GuiUtils.drawBackdrop(guiGraphics, minecraft, 0, 0, componentWidth, color.a());
               guiGraphics.m_280430_(font, scrollTextComponent, 0, 0, color.argb());
               pose.m_85849_();
            }
         }

         if (this.lastTick != minecraft.f_91074_.m_9236_().m_46467_()) {
            this.lastTick = minecraft.f_91074_.m_9236_().m_46467_();
            this.modeSwitchTimer--;
         }
      }
   }
}
