package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiSortingTab extends GuiInsetElement<TileEntityFactory<?>> {
   public GuiSortingTab(IGuiWrapper gui, TileEntityFactory<?> tile) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "sorting.png"), gui, tile, -26, 62, 35, 18, true);
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.drawTextScaledBound(
         guiGraphics,
         BooleanStateDisplay.OnOff.of(this.dataSource.isSorting()).getTextComponent(),
         this.relativeX + 3,
         this.relativeY + 24,
         this.titleTextColor(),
         21.0F
      );
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.AUTO_SORT.translate(new Object[0])});
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_FACTORY_SORT);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.AUTO_SORT_BUTTON, this.dataSource));
   }
}
