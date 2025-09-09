package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public abstract class GuiTabElementType<TILE extends BlockEntity, TAB extends Enum<?> & TabType<TILE>> extends GuiInsetElement<TILE> {
   private final TAB tabType;

   public GuiTabElementType(IGuiWrapper gui, TILE tile, TAB type) {
      super(type.getResource(), gui, tile, -26, type.getYPos(), 26, 18, true);
      this.tabType = type;
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      this.tabType.onClick(this.dataSource);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{this.tabType.getDescription()});
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, this.tabType.getTabColor());
   }
}
