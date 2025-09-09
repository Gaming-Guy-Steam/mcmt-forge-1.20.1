package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public class GuiQIOFrequencyTab extends GuiInsetElement<TileEntityMekanism> {
   private static final ResourceLocation FREQUENCY = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "frequency.png");
   private final InteractionHand currentHand;
   private boolean isItem;

   public GuiQIOFrequencyTab(IGuiWrapper gui, TileEntityMekanism tile) {
      super(FREQUENCY, gui, tile, -26, 6, 26, 18, true);
      this.currentHand = InteractionHand.MAIN_HAND;
   }

   public GuiQIOFrequencyTab(IGuiWrapper gui, InteractionHand hand) {
      super(FREQUENCY, gui, null, -26, 6, 26, 18, true);
      this.isItem = true;
      this.currentHand = hand;
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_QIO_FREQUENCY);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.SET_FREQUENCY.translate(new Object[0])});
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      if (this.isItem) {
         Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedItemButton.QIO_FREQUENCY_SELECT, this.currentHand));
      } else {
         Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedTileButton.QIO_FREQUENCY_SELECT, this.dataSource));
      }
   }
}
