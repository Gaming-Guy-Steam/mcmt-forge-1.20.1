package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.Mekanism;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiGasMode extends MekanismImageButton {
   private static final ResourceLocation IDLE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "gas_mode_idle.png");
   private static final ResourceLocation EXCESS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "gas_mode_excess.png");
   private static final ResourceLocation DUMP = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "gas_mode_dump.png");
   private final boolean left;
   private final Supplier<TileEntityChemicalTank.GasMode> gasModeSupplier;

   public GuiGasMode(IGuiWrapper gui, int x, int y, boolean left, Supplier<TileEntityChemicalTank.GasMode> gasModeSupplier, BlockPos pos, int tank) {
      this(gui, x, y, left, gasModeSupplier, pos, tank, null);
   }

   public GuiGasMode(
      IGuiWrapper gui,
      int x,
      int y,
      boolean left,
      Supplier<TileEntityChemicalTank.GasMode> gasModeSupplier,
      BlockPos pos,
      int tank,
      GuiElement.IHoverable onHover
   ) {
      super(
         gui,
         x,
         y,
         10,
         IDLE,
         () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.GAS_MODE_BUTTON, pos, tank)),
         onHover
      );
      this.left = left;
      this.gasModeSupplier = gasModeSupplier;
   }

   @Override
   protected ResourceLocation getResource() {
      return switch ((TileEntityChemicalTank.GasMode)this.gasModeSupplier.get()) {
         case DUMPING_EXCESS -> EXCESS;
         case DUMPING -> DUMP;
         default -> super.getResource();
      };
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      Component component = this.gasModeSupplier.get().getTextComponent();
      if (this.left) {
         this.drawTextScaledBound(
            guiGraphics,
            component,
            this.relativeX - 3 - (int)(this.getStringWidth(component) * this.getNeededScale(component, 66.0F)),
            this.relativeY + 1,
            this.titleTextColor(),
            66.0F
         );
      } else {
         this.drawTextScaledBound(guiGraphics, component, this.relativeX + this.f_93618_ + 5, this.relativeY + 1, this.titleTextColor(), 66.0F);
      }

      super.renderForeground(guiGraphics, mouseX, mouseY);
   }
}
