package mekanism.client.gui.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiDigitalSwitch;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner, MekanismTileContainer<TileEntityDigitalMiner>> {
   private static final ResourceLocation EJECT = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "switch/eject.png");
   private static final ResourceLocation INPUT = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "switch/input.png");
   private static final ResourceLocation SILK = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "switch/silk.png");
   private MekanismButton startButton;
   private MekanismButton stopButton;
   private MekanismButton configButton;

   public GuiDigitalMiner(MekanismTileContainer<TileEntityDigitalMiner> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 76;
      this.f_97731_ = this.f_97727_ - 94;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiInnerScreen(this, 7, 19, 77, 69, () -> {
         List<Component> list = new ArrayList<>();
         ILangEntry runningType;
         if (this.tile.getEnergyContainer().getEnergyPerTick().greaterThan(this.tile.getEnergyContainer().getMaxEnergy())) {
            runningType = MekanismLang.MINER_LOW_POWER;
         } else if (this.tile.isRunning()) {
            runningType = MekanismLang.MINER_RUNNING;
         } else {
            runningType = MekanismLang.IDLE;
         }

         list.add(runningType.translate());
         list.add(this.tile.searcher.state.getTextComponent());
         list.add(MekanismLang.MINER_TO_MINE.translate(new Object[]{TextUtils.format((long)this.tile.getToMine())}));
         return list;
      }).spacing(1).clearFormat());
      this.addRenderableWidget(
         new GuiDigitalSwitch(
            this,
            19,
            56,
            EJECT,
            this.tile::getDoEject,
            MekanismLang.AUTO_EJECT.translate(new Object[0]),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.AUTO_EJECT_BUTTON, this.tile)),
            GuiDigitalSwitch.SwitchType.LOWER_ICON
         )
      );
      this.addRenderableWidget(
         new GuiDigitalSwitch(
            this,
            38,
            56,
            INPUT,
            this.tile::getDoPull,
            MekanismLang.AUTO_PULL.translate(new Object[0]),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.AUTO_PULL_BUTTON, this.tile)),
            GuiDigitalSwitch.SwitchType.LOWER_ICON
         )
      );
      this.addRenderableWidget(
         new GuiDigitalSwitch(
            this,
            57,
            56,
            SILK,
            this.tile::getSilkTouch,
            MekanismLang.MINER_SILK.translate(new Object[0]),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.SILK_TOUCH_BUTTON, this.tile)),
            GuiDigitalSwitch.SwitchType.LOWER_ICON
         )
      );
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 157, 39, 47))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, () -> {
            MinerEnergyContainer energyContainer = this.tile.getEnergyContainer();
            return energyContainer.getEnergyPerTick().greaterThan(energyContainer.getEnergy());
         });
      this.addRenderableWidget(new GuiVisualsTab(this, this.tile));
      this.addRenderableWidget(
         new GuiSlot(SlotType.DIGITAL, this, 64, 21)
            .setRenderAboveSlots()
            .validity(() -> this.tile.missingStack)
            .with(() -> this.tile.missingStack.m_41619_() ? SlotOverlay.CHECK : null)
            .hover(
               this.getOnHover(
                  () -> this.tile.missingStack.m_41619_()
                     ? MekanismLang.MINER_WELL.translate(new Object[0])
                     : MekanismLang.MINER_MISSING_BLOCK.translate(new Object[0])
               )
            )
      );
      this.addRenderableWidget(
         new GuiEnergyTab(
            this,
            () -> {
               MinerEnergyContainer energyContainer = this.tile.getEnergyContainer();
               return List.of(
                  MekanismLang.MINER_ENERGY_CAPACITY.translate(new Object[]{EnergyDisplay.of(energyContainer.getMaxEnergy())}),
                  MekanismLang.NEEDED_PER_TICK.translate(new Object[]{EnergyDisplay.of(energyContainer.getEnergyPerTick())}),
                  MekanismLang.MINER_BUFFER_FREE.translate(new Object[]{EnergyDisplay.of(energyContainer.getNeeded())})
               );
            }
         )
      );
      int buttonStart = 19;
      this.startButton = this.addRenderableWidget(
         new TranslationButton(
            this,
            87,
            buttonStart,
            61,
            18,
            MekanismLang.BUTTON_START,
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.START_BUTTON, this.tile))
         )
      );
      this.stopButton = this.addRenderableWidget(
         new TranslationButton(
            this,
            87,
            buttonStart + 17,
            61,
            18,
            MekanismLang.BUTTON_STOP,
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.STOP_BUTTON, this.tile))
         )
      );
      this.configButton = this.addRenderableWidget(
         new TranslationButton(
            this,
            87,
            buttonStart + 34,
            61,
            18,
            MekanismLang.BUTTON_CONFIG,
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedTileButton.DIGITAL_MINER_CONFIG, this.tile))
         )
      );
      this.addRenderableWidget(
         new TranslationButton(
            this,
            87,
            buttonStart + 51,
            61,
            18,
            MekanismLang.MINER_RESET,
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.RESET_BUTTON, this.tile))
         )
      );
      this.updateEnabledButtons();
      this.trackWarning(
         WarningTracker.WarningType.FILTER_HAS_BLACKLISTED_ELEMENT, () -> this.tile.getFilterManager().anyEnabledMatch(MinerFilter::hasBlacklistedElement)
      );
      this.trackWarning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT_OVERFLOW, this.tile::hasOverflow);
   }

   @Override
   public void m_181908_() {
      super.m_181908_();
      this.updateEnabledButtons();
   }

   private void updateEnabledButtons() {
      this.startButton.f_93623_ = this.tile.searcher.state == ThreadMinerSearch.State.IDLE || !this.tile.isRunning();
      this.stopButton.f_93623_ = this.tile.searcher.state != ThreadMinerSearch.State.IDLE && this.tile.isRunning();
      this.configButton.f_93623_ = this.tile.searcher.state == ThreadMinerSearch.State.IDLE;
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
