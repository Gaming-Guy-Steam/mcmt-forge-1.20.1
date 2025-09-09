package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiEnergyTab extends GuiTexturedElement {
   private static final Map<UnitDisplayUtils.EnergyUnit, ResourceLocation> ICONS = new EnumMap<>(UnitDisplayUtils.EnergyUnit.class);
   private final GuiTexturedElement.IInfoHandler infoHandler;

   public GuiEnergyTab(IGuiWrapper gui, GuiTexturedElement.IInfoHandler handler) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_TAB, "energy_info.png"), gui, -26, 137, 26, 26);
      this.infoHandler = handler;
   }

   public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, FloatingLongSupplier lastEnergyUsed) {
      this(
         gui,
         () -> List.of(
            MekanismLang.USING.translate(new Object[]{EnergyDisplay.of(lastEnergyUsed.get())}),
            MekanismLang.NEEDED.translate(new Object[]{EnergyDisplay.of(energyContainer.getNeeded())})
         )
      );
   }

   public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, BooleanSupplier isActive) {
      this(
         gui,
         () -> {
            FloatingLong using = isActive.getAsBoolean() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
            return List.of(
               MekanismLang.USING.translate(new Object[]{EnergyDisplay.of(using)}),
               MekanismLang.NEEDED.translate(new Object[]{EnergyDisplay.of(energyContainer.getNeeded())})
            );
         }
      );
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280163_(this.getResource(), this.relativeX, this.relativeY, 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.f_93618_, this.f_93619_);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      List<Component> info = new ArrayList<>(this.infoHandler.getInfo());
      info.add(MekanismLang.UNIT.translate(new Object[]{UnitDisplayUtils.EnergyUnit.getConfigured()}));
      this.displayTooltips(guiGraphics, mouseX, mouseY, info);
   }

   @Override
   protected ResourceLocation getResource() {
      return ICONS.computeIfAbsent(
         UnitDisplayUtils.EnergyUnit.getConfigured(),
         type -> MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_TAB, "energy_info_" + type.getTabName() + ".png")
      );
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.updateEnergyUnit(IIncrementalEnum::getNext);
      } else if (button == 1) {
         this.updateEnergyUnit(IIncrementalEnum::getPrevious);
      }
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }

   private void updateEnergyUnit(UnaryOperator<UnitDisplayUtils.EnergyUnit> converter) {
      UnitDisplayUtils.EnergyUnit current = UnitDisplayUtils.EnergyUnit.getConfigured();
      UnitDisplayUtils.EnergyUnit updated = converter.apply(current);
      if (current != updated) {
         MekanismConfig.common.energyUnit.set(updated);
         MekanismConfig.common.save();
      }
   }
}
