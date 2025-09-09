package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketConfigurationUpdate;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SideDataButton extends BasicColorButton {
   private final Supplier<DataType> dataTypeSupplier;
   private final ItemStack otherBlockItem;

   public SideDataButton(
      IGuiWrapper gui,
      int x,
      int y,
      RelativeSide slotPos,
      Supplier<DataType> dataTypeSupplier,
      Supplier<EnumColor> colorSupplier,
      TileEntityMekanism tile,
      Supplier<TransmissionType> transmissionType,
      PacketConfigurationUpdate.ConfigurationPacket packetType,
      @Nullable GuiElement.IHoverable onHover
   ) {
      super(
         gui,
         x,
         y,
         22,
         () -> {
            DataType dataType = dataTypeSupplier.get();
            return dataType == null ? null : colorSupplier.get();
         },
         () -> Mekanism.packetHandler()
            .sendToServer(new PacketConfigurationUpdate(packetType, tile.m_58899_(), Screen.m_96638_() ? 2 : 0, slotPos, transmissionType.get())),
         () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(packetType, tile.m_58899_(), 1, slotPos, transmissionType.get())),
         onHover
      );
      this.dataTypeSupplier = dataTypeSupplier;
      Level tileWorld = tile.getTileWorld();
      if (tileWorld != null) {
         Direction globalSide = slotPos.getDirection(tile.getDirection());
         BlockPos otherBlockPos = tile.getTilePos().m_121945_(globalSide);
         BlockState blockOnSide = tileWorld.m_8055_(otherBlockPos);
         if (!blockOnSide.m_60795_()) {
            this.otherBlockItem = blockOnSide.getCloneItemStack(
               new BlockHitResult(Vec3.m_82512_(otherBlockPos).m_231075_(globalSide.m_122424_(), 0.5), globalSide.m_122424_(), otherBlockPos, false),
               tileWorld,
               otherBlockPos,
               Minecraft.m_91087_().f_91074_
            );
         } else {
            this.otherBlockItem = ItemStack.f_41583_;
         }
      } else {
         this.otherBlockItem = ItemStack.f_41583_;
      }
   }

   public DataType getDataType() {
      return this.dataTypeSupplier.get();
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      if (!this.otherBlockItem.m_41619_()) {
         GuiUtils.renderItem(guiGraphics, this.otherBlockItem, this.getRelativeX() + 3, this.getRelativeY() + 3, 1.0F, this.getFont(), null, true);
      }
   }
}
