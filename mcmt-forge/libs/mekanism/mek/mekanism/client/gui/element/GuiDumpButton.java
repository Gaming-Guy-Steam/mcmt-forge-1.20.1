package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class GuiDumpButton<TILE extends BlockEntity & IHasDumpButton> extends GuiTexturedElement {
   protected final TILE tile;

   public GuiDumpButton(IGuiWrapper gui, TILE tile, int x, int y) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "dump.png"), gui, x, y, 21, 10);
      this.tile = tile;
      this.clickSound = SoundEvents.f_12490_;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280163_(this.getResource(), this.relativeX, this.relativeY, 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.f_93618_, this.f_93619_);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.DUMP_BUTTON, this.tile));
   }
}
