package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiRedstoneControlTab extends GuiInsetElement<TileEntityMekanism> {
   private static final ResourceLocation DISABLED = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "redstone_control_disabled.png");
   private static final ResourceLocation HIGH = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "redstone_control_high.png");
   private static final ResourceLocation LOW = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "redstone_control_low.png");

   public GuiRedstoneControlTab(IGuiWrapper gui, TileEntityMekanism tile) {
      super(DISABLED, gui, tile, gui.getWidth(), 137, 26, 18, false);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{this.dataSource.getControlType().getTextComponent()});
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      Mekanism.packetHandler()
         .sendToServer(
            new PacketGuiInteract(
               button == 0 ? PacketGuiInteract.GuiInteraction.NEXT_REDSTONE_CONTROL : PacketGuiInteract.GuiInteraction.PREVIOUS_REDSTONE_CONTROL,
               this.dataSource
            )
         );
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }

   @Override
   protected ResourceLocation getOverlay() {
      return switch (this.dataSource.getControlType()) {
         case HIGH -> HIGH;
         case LOW -> LOW;
         default -> super.getOverlay();
      };
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_REDSTONE_CONTROL);
   }

   @Override
   protected void drawBackgroundOverlay(@NotNull GuiGraphics guiGraphics) {
      if (this.dataSource.getControlType() == IRedstoneControl.RedstoneControl.PULSE) {
         guiGraphics.m_280159_(this.getButtonX() + 1, this.getButtonY() + 1, 0, this.innerWidth - 2, this.innerHeight - 2, MekanismRenderer.redstonePulse);
      } else {
         super.drawBackgroundOverlay(guiGraphics);
      }
   }
}
