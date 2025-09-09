package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiContainerEditModeTab<TILE extends TileEntityMekanism & IFluidContainerManager> extends GuiInsetElement<TILE> {
   private static final ResourceLocation BOTH = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "container_edit_mode_both.png");
   private static final ResourceLocation FILL = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "container_edit_mode_fill.png");
   private static final ResourceLocation EMPTY = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "container_edit_mode_empty.png");

   public GuiContainerEditModeTab(IGuiWrapper gui, TILE tile) {
      super(BOTH, gui, tile, gui.getWidth(), 138, 26, 18, false);
   }

   @Override
   protected ResourceLocation getOverlay() {
      return switch (this.dataSource.getContainerEditMode()) {
         case FILL -> FILL;
         case EMPTY -> EMPTY;
         default -> super.getOverlay();
      };
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{this.dataSource.getContainerEditMode().getTextComponent()});
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      Mekanism.packetHandler()
         .sendToServer(
            new PacketGuiInteract(button == 0 ? PacketGuiInteract.GuiInteraction.NEXT_MODE : PacketGuiInteract.GuiInteraction.PREVIOUS_MODE, this.dataSource)
         );
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_CONTAINER_EDIT_MODE);
   }
}
