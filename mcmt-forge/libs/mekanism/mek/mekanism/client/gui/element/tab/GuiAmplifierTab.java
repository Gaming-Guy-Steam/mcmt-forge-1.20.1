package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiAmplifierTab extends GuiInsetElement<TileEntityLaserAmplifier> {
   private static final ResourceLocation OFF = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "amplifier_off.png");
   private static final ResourceLocation ENTITY = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "amplifier_entity.png");
   private static final ResourceLocation CONTENTS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "amplifier_contents.png");

   public GuiAmplifierTab(IGuiWrapper gui, TileEntityLaserAmplifier tile) {
      super(OFF, gui, tile, -26, 138, 26, 18, true);
   }

   @Override
   protected ResourceLocation getOverlay() {
      return switch (this.dataSource.getOutputMode()) {
         case ENTITY_DETECTION -> ENTITY;
         case ENERGY_CONTENTS -> CONTENTS;
         default -> super.getOverlay();
      };
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.REDSTONE_OUTPUT.translate(new Object[]{this.dataSource.getOutputMode()})});
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
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_LASER_AMPLIFIER);
   }
}
