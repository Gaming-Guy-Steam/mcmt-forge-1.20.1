package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.BasicColorButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiDimensionalStabilizer extends GuiMekanismTile<TileEntityDimensionalStabilizer, MekanismTileContainer<TileEntityDimensionalStabilizer>> {
   public GuiDimensionalStabilizer(MekanismTileContainer<TileEntityDimensionalStabilizer> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 164, 15))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, () -> {
            MachineEnergyContainer<TileEntityDimensionalStabilizer> energyContainer = this.tile.getEnergyContainer();
            return energyContainer.getEnergyPerTick().greaterThan(energyContainer.getEnergy());
         });
      this.addRenderableWidget(new GuiVisualsTab(this, this.tile));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
      int tileChunkX = SectionPos.m_123171_(this.tile.m_58899_().m_123341_());
      int tileChunkZ = SectionPos.m_123171_(this.tile.m_58899_().m_123343_());

      for (int x = -2; x <= 2; x++) {
         int shiftedX = x + 2;
         int chunkX = tileChunkX + x;

         for (int z = -2; z <= 2; z++) {
            int shiftedZ = z + 2;
            int chunkZ = tileChunkZ + z;
            if (x == 0 && z == 0) {
               this.addRenderableWidget(
                  BasicColorButton.renderActive(
                     this,
                     63 + 10 * shiftedX,
                     19 + 10 * shiftedZ,
                     10,
                     EnumColor.DARK_BLUE,
                     () -> {
                        for (int i = 1; i <= 2; i++) {
                           if (this.hasAtRadius(i, false)) {
                              Mekanism.packetHandler()
                                 .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.ENABLE_RADIUS_CHUNKLOAD, this.tile, i));
                              break;
                           }
                        }
                     },
                     () -> {
                        for (int i = 2; i > 0; i--) {
                           if (this.hasAtRadius(i, true)) {
                              Mekanism.packetHandler()
                                 .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.DISABLE_RADIUS_CHUNKLOAD, this.tile, i));
                              break;
                           }
                        }
                     },
                     (onHover, guiGraphics, mouseX, mouseY) -> {
                        List<Component> tooltips = new ArrayList<>();
                        tooltips.add(MekanismLang.STABILIZER_CENTER.translate(new Object[]{EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ}));

                        for (int i = 1; i <= 2; i++) {
                           if (this.hasAtRadius(i, false)) {
                              tooltips.add(Component.m_237113_(" "));
                              tooltips.add(
                                 MekanismLang.STABILIZER_ENABLE_RADIUS
                                    .translate(new Object[]{EnumColor.INDIGO, i, EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ})
                              );
                              break;
                           }
                        }

                        for (int ix = 2; ix > 0; ix--) {
                           if (this.hasAtRadius(ix, true)) {
                              tooltips.add(Component.m_237113_(" "));
                              tooltips.add(
                                 MekanismLang.STABILIZER_DISABLE_RADIUS
                                    .translate(new Object[]{EnumColor.INDIGO, ix, EnumColor.INDIGO, chunkX, EnumColor.INDIGO, chunkZ})
                              );
                              break;
                           }
                        }

                        this.displayTooltips(guiGraphics, mouseX, mouseY, tooltips);
                     }
                  )
               );
            } else {
               int packetTarget = shiftedX * 5 + shiftedZ;
               this.addRenderableWidget(
                  BasicColorButton.toggle(
                     this,
                     63 + 10 * shiftedX,
                     19 + 10 * shiftedZ,
                     10,
                     EnumColor.DARK_BLUE,
                     () -> this.tile.isChunkLoadingAt(shiftedX, shiftedZ),
                     () -> Mekanism.packetHandler()
                        .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.TOGGLE_CHUNKLOAD, this.tile, packetTarget)),
                     this.getOnHover(
                        () -> MekanismLang.STABILIZER_TOGGLE_LOADING
                           .translate(
                              new Object[]{
                                 BooleanStateDisplay.OnOff.of(this.tile.isChunkLoadingAt(shiftedX, shiftedZ), true),
                                 EnumColor.INDIGO,
                                 chunkX,
                                 EnumColor.INDIGO,
                                 chunkZ
                              }
                           )
                     )
                  )
               );
            }
         }
      }

      this.addRenderableWidget(new GuiUpArrow(this, 52, 28));
   }

   private boolean hasAtRadius(int radius, boolean state) {
      for (int x = -radius; x <= radius; x++) {
         boolean skipInner = x > -radius && x < radius;
         int actualX = x + 2;

         for (int z = -radius; z <= radius; z += skipInner ? 2 * radius : 1) {
            if (this.tile.isChunkLoadingAt(actualX, z + 2) == state) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      this.drawTextExact(guiGraphics, MekanismLang.NORTH_SHORT.translate(new Object[0]), 53.5F, 41.0F, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
