package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.lib.ColorAtlas;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiMatrixTab extends GuiTabElementType<TileEntityInductionCasing, GuiMatrixTab.MatrixTab> {
   public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tile, GuiMatrixTab.MatrixTab type) {
      super(gui, tile, type);
   }

   public static enum MatrixTab implements TabType<TileEntityInductionCasing> {
      MAIN("energy.png", MekanismLang.MAIN_TAB, PacketGuiButtonPress.ClickedTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
      STAT("stats.png", MekanismLang.MATRIX_STATS, PacketGuiButtonPress.ClickedTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

      private final ColorAtlas.ColorRegistryObject colorRO;
      private final PacketGuiButtonPress.ClickedTileButton button;
      private final ILangEntry description;
      private final String path;

      private MatrixTab(String path, ILangEntry description, PacketGuiButtonPress.ClickedTileButton button, ColorAtlas.ColorRegistryObject colorRO) {
         this.path = path;
         this.description = description;
         this.button = button;
         this.colorRO = colorRO;
      }

      @Override
      public ResourceLocation getResource() {
         return MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, this.path);
      }

      public void onClick(TileEntityInductionCasing tile) {
         Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(this.button, tile));
      }

      @Override
      public Component getDescription() {
         return this.description.translate();
      }

      @Override
      public ColorAtlas.ColorRegistryObject getTabColor() {
         return this.colorRO;
      }
   }
}
