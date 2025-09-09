package mekanism.client.gui.machine;

import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiFormulaicAssemblicator extends GuiConfigurableTile<TileEntityFormulaicAssemblicator, MekanismTileContainer<TileEntityFormulaicAssemblicator>> {
   private MekanismButton encodeFormulaButton;
   private MekanismButton stockControlButton;
   private MekanismButton fillEmptyButton;
   private MekanismButton craftSingleButton;
   private MekanismButton craftAvailableButton;
   private MekanismButton autoModeButton;

   public GuiFormulaicAssemblicator(MekanismTileContainer<TileEntityFormulaicAssemblicator> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 64;
      this.f_97731_ = this.f_97727_ - 94;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 159, 15))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, () -> {
            if (this.tile.getAutoMode() && this.tile.hasRecipe()) {
               MachineEnergyContainer<TileEntityFormulaicAssemblicator> energyContainer = this.tile.getEnergyContainer();
               return energyContainer.getEnergyPerTick().greaterThan(energyContainer.getEnergy());
            } else {
               return false;
            }
         });
      this.addRenderableWidget(new GuiSlot(SlotType.OUTPUT_LARGE, this, 115, 16));
      this.addRenderableWidget(
         new GuiProgress(() -> (double)this.tile.getOperatingTicks() / this.tile.getTicksRequired(), ProgressType.TALL_RIGHT, this, 86, 43).jeiCrafting()
      );
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::usedEnergy));
      this.encodeFormulaButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            7,
            45,
            14,
            this.getButtonLocation("encode_formula"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.ENCODE_FORMULA, this.tile)),
            this.getOnHover(MekanismLang.ENCODE_FORMULA)
         )
      );
      this.stockControlButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            26,
            75,
            16,
            this.getButtonLocation("stock_control"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.STOCK_CONTROL_BUTTON, this.tile)),
            this.getOnHover(() -> MekanismLang.STOCK_CONTROL.translate(new Object[]{BooleanStateDisplay.OnOff.of(this.tile.getStockControl())}))
         )
      );
      this.fillEmptyButton = this.addRenderableWidget(
         new ToggleButton(
            this,
            44,
            75,
            16,
            16,
            this.getButtonLocation("empty"),
            this.getButtonLocation("fill"),
            () -> this.tile.formula == null,
            () -> {
               PacketGuiInteract.GuiInteraction interaction = this.tile.formula == null
                  ? PacketGuiInteract.GuiInteraction.EMPTY_GRID
                  : PacketGuiInteract.GuiInteraction.FILL_GRID;
               Mekanism.packetHandler().sendToServer(new PacketGuiInteract(interaction, this.tile));
            },
            this.getOnHover(() -> {
               ILangEntry langEntry = this.tile.formula == null ? MekanismLang.EMPTY_ASSEMBLICATOR : MekanismLang.FILL_ASSEMBLICATOR;
               return langEntry.translate();
            })
         )
      );
      this.craftSingleButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            71,
            75,
            16,
            this.getButtonLocation("craft_single"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CRAFT_SINGLE, this.tile)),
            this.getOnHover(MekanismLang.CRAFT_SINGLE)
         )
      );
      this.craftAvailableButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            89,
            75,
            16,
            this.getButtonLocation("craft_available"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CRAFT_ALL, this.tile)),
            this.getOnHover(MekanismLang.CRAFT_AVAILABLE)
         )
      );
      this.autoModeButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            107,
            75,
            16,
            this.getButtonLocation("auto_toggle"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.NEXT_MODE, this.tile)),
            this.getOnHover(() -> MekanismLang.AUTO_MODE.translate(new Object[]{BooleanStateDisplay.OnOff.of(this.tile.getAutoMode())}))
         )
      );
      this.updateEnabledButtons();
   }

   @Override
   public void m_181908_() {
      super.m_181908_();
      this.updateEnabledButtons();
   }

   private void updateEnabledButtons() {
      this.encodeFormulaButton.f_93623_ = !this.tile.getAutoMode() && this.tile.hasRecipe() && this.canEncode();
      this.stockControlButton.f_93623_ = this.tile.formula != null && this.tile.formula.isValidFormula();
      this.fillEmptyButton.f_93623_ = !this.tile.getAutoMode();
      this.craftSingleButton.f_93623_ = !this.tile.getAutoMode() && this.tile.hasRecipe();
      this.craftAvailableButton.f_93623_ = !this.tile.getAutoMode() && this.tile.hasRecipe();
      this.autoModeButton.f_93623_ = this.tile.formula != null && this.tile.formula.isValidFormula();
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected ItemStack checkValidity(int slotIndex) {
      int i = slotIndex - 21;
      if (i >= 0 && this.tile.formula != null && this.tile.formula.isValidFormula()) {
         ItemStack stack = (ItemStack)this.tile.formula.input.get(i);
         if (!stack.m_41619_()) {
            Slot slot = (Slot)((MekanismTileContainer)this.f_97732_).f_38839_.get(slotIndex);
            if (slot.m_7993_().m_41619_() || !this.tile.formula.isIngredientInPos(this.tile.m_58904_(), slot.m_7993_(), i)) {
               return stack;
            }
         }
      }

      return ItemStack.f_41583_;
   }

   @Override
   protected void m_7286_(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      super.m_7286_(guiGraphics, partialTick, mouseX, mouseY);
      SlotOverlay overlay = this.tile.hasRecipe() ? SlotOverlay.CHECK : SlotOverlay.X;
      guiGraphics.m_280163_(
         overlay.getTexture(),
         this.f_97735_ + 88,
         this.f_97736_ + 22,
         0.0F,
         0.0F,
         overlay.getWidth(),
         overlay.getHeight(),
         overlay.getWidth(),
         overlay.getHeight()
      );
   }

   private boolean canEncode() {
      if ((this.tile.formula == null || !this.tile.formula.isValidFormula()) && !this.tile.getFormulaSlot().isEmpty()) {
         ItemStack formulaStack = this.tile.getFormulaSlot().getStack();
         return formulaStack.m_41720_() instanceof ItemCraftingFormula formula && !formula.hasInventory(formulaStack);
      } else {
         return false;
      }
   }
}
