package mekanism.client.gui.machine;

import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSeismicVibrator extends GuiMekanismTile<TileEntitySeismicVibrator, MekanismTileContainer<TileEntitySeismicVibrator>> {
   public GuiSeismicVibrator(MekanismTileContainer<TileEntitySeismicVibrator> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new GuiInnerScreen(
            this,
            16,
            23,
            112,
            40,
            () -> List.of(
               this.tile.getActive() ? MekanismLang.VIBRATING.translate(new Object[0]) : MekanismLang.IDLE.translate(new Object[0]),
               MekanismLang.CHUNK
                  .translate(new Object[]{SectionPos.m_123171_(this.tile.m_58899_().m_123341_()), SectionPos.m_123171_(this.tile.m_58899_().m_123343_())})
            )
         )
      );
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 164, 15))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, () -> {
            MachineEnergyContainer<TileEntitySeismicVibrator> energyContainer = this.tile.getEnergyContainer();
            return energyContainer.getEnergyPerTick().greaterThan(energyContainer.getEnergy());
         });
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
