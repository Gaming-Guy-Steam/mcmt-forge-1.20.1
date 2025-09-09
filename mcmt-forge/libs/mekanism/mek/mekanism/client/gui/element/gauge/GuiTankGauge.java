package mekanism.client.gui.element.gauge;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class GuiTankGauge<T, TANK> extends GuiGauge<T> implements IJEIIngredientHelper {
   private final GuiTankGauge.ITankInfoHandler<TANK> infoHandler;
   private final PacketDropperUse.TankType tankType;

   public GuiTankGauge(
      GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY, GuiTankGauge.ITankInfoHandler<TANK> infoHandler, PacketDropperUse.TankType tankType
   ) {
      super(type, gui, x, y, sizeX, sizeY);
      this.infoHandler = infoHandler;
      this.tankType = tankType;
   }

   public TANK getTank() {
      return this.infoHandler.getTank();
   }

   @Override
   protected GaugeInfo getGaugeColor() {
      if (this.gui() instanceof GuiMekanismTile<?, ?> gui) {
         TANK tank = this.getTank();
         if (tank != null && ((MekanismTileContainer)gui.m_6262_()).getTileEntity() instanceof ISideConfiguration config) {
            DataType dataType = config.getActiveDataType(tank);
            if (dataType != null) {
               return GaugeInfo.get(dataType);
            }
         }
      }

      return super.getGaugeColor();
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      ItemStack stack = this.gui().getCarriedItem();
      if (this.gui() instanceof GuiMekanismTile<?, ?> gui && !stack.m_41619_() && stack.m_41720_() instanceof ItemGaugeDropper) {
         int index = this.infoHandler.getTankIndex();
         if (index != -1) {
            PacketDropperUse.DropperAction action;
            if (button == 0) {
               action = Screen.m_96638_() ? PacketDropperUse.DropperAction.DUMP_TANK : PacketDropperUse.DropperAction.FILL_DROPPER;
            } else {
               action = PacketDropperUse.DropperAction.DRAIN_DROPPER;
            }

            Mekanism.packetHandler().sendToServer(new PacketDropperUse(gui.getTileEntity().m_58899_(), action, this.tankType, index));
         }
      }
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }

   public interface ITankInfoHandler<TANK> {
      @Nullable
      TANK getTank();

      int getTankIndex();
   }
}
