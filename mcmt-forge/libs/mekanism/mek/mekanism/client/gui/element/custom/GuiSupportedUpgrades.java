package mekanism.client.gui.element.custom;

import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiSupportedUpgrades extends GuiElement {
   private static final int ELEMENT_SIZE = 12;
   private static final int FIRST_ROW_ROOM = 5;
   private static final int ROW_ROOM = 10;
   private final Set<Upgrade> supportedUpgrades;

   public static int calculateNeededRows() {
      int count = EnumUtils.UPGRADES.length;
      if (count <= 5) {
         return 1;
      } else {
         count -= 5;
         return 2 + count / 10;
      }
   }

   public GuiSupportedUpgrades(IGuiWrapper gui, int x, int y, Set<Upgrade> supportedUpgrades) {
      super(gui, x, y, 125, 12 * calculateNeededRows() + 2);
      this.supportedUpgrades = supportedUpgrades;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderBackgroundTexture(guiGraphics, GuiElementHolder.HOLDER, 32, 32);
      int backgroundColor = Color.argb(GuiElementHolder.getBackgroundColor()).alpha(0.5).argb();

      for (int i = 0; i < EnumUtils.UPGRADES.length; i++) {
         Upgrade upgrade = EnumUtils.UPGRADES[i];
         GuiSupportedUpgrades.UpgradePos pos = this.getUpgradePos(i);
         int xPos = this.relativeX + 1 + pos.x;
         int yPos = this.relativeY + 1 + pos.y;
         this.gui().renderItem(guiGraphics, UpgradeUtils.getStack(upgrade), xPos, yPos, 0.75F);
         if (!this.supportedUpgrades.contains(upgrade)) {
            guiGraphics.m_285944_(MekanismRenderType.MEK_GUI_FADE, xPos, yPos, xPos + 12, yPos + 12, backgroundColor);
         }
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTextScaledBound(
         guiGraphics, MekanismLang.UPGRADES_SUPPORTED.translate(new Object[0]), this.relativeX + 2, this.relativeY + 3, this.titleTextColor(), 54.0F
      );
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);

      for (int i = 0; i < EnumUtils.UPGRADES.length; i++) {
         GuiSupportedUpgrades.UpgradePos pos = this.getUpgradePos(i);
         if (mouseX >= this.m_252754_() + 1 + pos.x
            && mouseX < this.m_252754_() + 1 + pos.x + 12
            && mouseY >= this.m_252907_() + 1 + pos.y
            && mouseY < this.m_252907_() + 1 + pos.y + 12) {
            Upgrade upgrade = EnumUtils.UPGRADES[i];
            Component upgradeName = MekanismLang.UPGRADE_TYPE.translateColored(EnumColor.YELLOW, new Object[]{upgrade});
            if (this.supportedUpgrades.contains(upgrade)) {
               this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{upgradeName, upgrade.getDescription()});
            } else {
               this.displayTooltips(
                  guiGraphics,
                  mouseX,
                  mouseY,
                  new Component[]{MekanismLang.UPGRADE_NOT_SUPPORTED.translateColored(EnumColor.RED, new Object[]{upgradeName}), upgrade.getDescription()}
               );
            }
            break;
         }
      }
   }

   private GuiSupportedUpgrades.UpgradePos getUpgradePos(int index) {
      int row = index < 5 ? 0 : 1 + (index - 5) / 10;
      if (row == 0) {
         return new GuiSupportedUpgrades.UpgradePos(55 + index % 5 * 12, 0);
      } else {
         index -= 5;
         return new GuiSupportedUpgrades.UpgradePos(index % 10 * 12, row * 12);
      }
   }

   private record UpgradePos(int x, int y) {
   }
}
