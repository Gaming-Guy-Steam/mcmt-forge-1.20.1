package mekanism.client.gui.element.tab;

import mekanism.api.text.EnumColor;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiVisualsTab extends GuiInsetElement<IHasVisualization> {
   public GuiVisualsTab(IGuiWrapper gui, IHasVisualization hasVisualization) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "visuals.png"), gui, hasVisualization, -26, 6, 26, 18, true);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      Component visualsComponent = MekanismLang.VISUALS.translate(new Object[]{BooleanStateDisplay.OnOff.of(this.dataSource.isClientRendering())});
      if (this.dataSource.canDisplayVisuals()) {
         this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{visualsComponent});
      } else {
         this.displayTooltips(
            guiGraphics, mouseX, mouseY, new Component[]{visualsComponent, MekanismLang.VISUALS_TOO_BIG.translateColored(EnumColor.RED, new Object[0])}
         );
      }
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_VISUALS);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      this.dataSource.toggleClientRendering();
   }
}
