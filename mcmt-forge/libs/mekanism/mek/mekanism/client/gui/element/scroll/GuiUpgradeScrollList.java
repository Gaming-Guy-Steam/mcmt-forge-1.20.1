package mekanism.client.gui.element.scroll;

import java.util.Set;
import java.util.function.ObjIntConsumer;
import mekanism.api.Upgrade;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiUpgradeScrollList extends GuiScrollList {
   private static final ResourceLocation UPGRADE_SELECTION = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "upgrade_selection.png");
   private static final int TEXTURE_WIDTH = 58;
   private static final int TEXTURE_HEIGHT = 36;
   private final TileComponentUpgrade component;
   private final Runnable onSelectionChange;
   @Nullable
   private Upgrade selectedType;

   public GuiUpgradeScrollList(IGuiWrapper gui, int x, int y, int width, int height, TileComponentUpgrade component, Runnable onSelectionChange) {
      super(gui, x, y, width, height, 12, GuiElementHolder.HOLDER, 32);
      this.component = component;
      this.onSelectionChange = onSelectionChange;
   }

   private Set<Upgrade> getCurrentUpgrades() {
      return this.component.getInstalledTypes();
   }

   @Override
   protected int getMaxElements() {
      return this.getCurrentUpgrades().size();
   }

   @Override
   public boolean hasSelection() {
      return this.selectedType != null;
   }

   @Override
   protected void setSelected(int index) {
      Set<Upgrade> currentUpgrades = this.getCurrentUpgrades();
      if (index >= 0 && index < currentUpgrades.size()) {
         Upgrade newSelection = ((Upgrade[])currentUpgrades.toArray(new Upgrade[0]))[index];
         if (this.selectedType != newSelection) {
            this.selectedType = newSelection;
            this.onSelectionChange.run();
         }
      }
   }

   @Nullable
   public Upgrade getSelection() {
      return this.selectedType;
   }

   @Override
   public void clearSelection() {
      if (this.selectedType != null) {
         this.selectedType = null;
         this.onSelectionChange.run();
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.forEachUpgrade(
         (upgrade, multipliedElement) -> this.drawTextScaledBound(
            guiGraphics, TextComponentUtil.build(upgrade), this.relativeX + 13, this.relativeY + 3 + multipliedElement, this.titleTextColor(), 44.0F
         )
      );
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      if (mouseX >= this.m_252754_() + 1 && mouseX < this.m_252754_() + this.barXShift - 1) {
         this.forEachUpgrade((upgrade, multipliedElement) -> {
            if (mouseY >= this.m_252907_() + 1 + multipliedElement && mouseY < this.m_252907_() + 1 + multipliedElement + this.elementHeight) {
               this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{upgrade.getDescription()});
            }
         });
      }
   }

   @Override
   public void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.hasSelection() && this.component.getUpgrades(this.getSelection()) == 0) {
         this.clearSelection();
      }

      this.forEachUpgrade(
         (upgrade, multipliedElement) -> {
            int shiftedY = this.m_252907_() + 1 + multipliedElement;
            int j = 1;
            if (upgrade == this.getSelection()) {
               j = 2;
            } else if (mouseX >= this.m_252754_() + 1
               && mouseX < this.m_252754_() + this.barXShift - 1
               && mouseY >= shiftedY
               && mouseY < shiftedY + this.elementHeight) {
               j = 0;
            }

            MekanismRenderer.color(guiGraphics, upgrade.getColor());
            guiGraphics.m_280163_(
               UPGRADE_SELECTION, this.relativeX + 1, this.relativeY + 1 + multipliedElement, 0.0F, this.elementHeight * j, 58, this.elementHeight, 58, 36
            );
            MekanismRenderer.resetColor(guiGraphics);
         }
      );
      this.forEachUpgrade(
         (upgrade, multipliedElement) -> this.gui()
            .renderItem(guiGraphics, UpgradeUtils.getStack(upgrade), this.relativeX + 3, this.relativeY + 3 + multipliedElement, 0.5F)
      );
   }

   private void forEachUpgrade(ObjIntConsumer<Upgrade> consumer) {
      Upgrade[] upgrades = this.getCurrentUpgrades().toArray(new Upgrade[0]);
      int currentSelection = this.getCurrentSelection();

      for (int i = 0; i < this.getFocusedElements(); i++) {
         int index = currentSelection + i;
         if (index > upgrades.length - 1) {
            break;
         }

         consumer.accept(upgrades[index], this.elementHeight * i);
      }
   }

   @Override
   public void syncFrom(GuiElement element) {
      super.syncFrom(element);
      GuiUpgradeScrollList old = (GuiUpgradeScrollList)element;
      this.selectedType = old.selectedType;
      this.onSelectionChange.run();
   }
}
