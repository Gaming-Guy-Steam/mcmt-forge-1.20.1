package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import mekanism.api.IIncrementalEnum;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiHeatTab extends GuiTexturedElement {
   private static final Map<UnitDisplayUtils.TemperatureUnit, ResourceLocation> ICONS = new EnumMap<>(UnitDisplayUtils.TemperatureUnit.class);
   private final GuiTexturedElement.IInfoHandler infoHandler;

   public GuiHeatTab(IGuiWrapper gui, GuiTexturedElement.IInfoHandler handler) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_TAB, "heat_info.png"), gui, -26, 109, 26, 26);
      this.infoHandler = handler;
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
      info.add(MekanismLang.UNIT.translate(new Object[]{MekanismConfig.common.tempUnit.get()}));
      this.displayTooltips(guiGraphics, mouseX, mouseY, info);
   }

   @Override
   protected ResourceLocation getResource() {
      return ICONS.computeIfAbsent(
         MekanismConfig.common.tempUnit.get(), type -> MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_TAB, "heat_info_" + type.getTabName() + ".png")
      );
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.updateTemperatureUnit(IIncrementalEnum::getNext);
      } else if (button == 1) {
         this.updateTemperatureUnit(IIncrementalEnum::getPrevious);
      }
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }

   private void updateTemperatureUnit(UnaryOperator<UnitDisplayUtils.TemperatureUnit> converter) {
      UnitDisplayUtils.TemperatureUnit current = MekanismConfig.common.tempUnit.get();
      UnitDisplayUtils.TemperatureUnit updated = converter.apply(current);
      if (current != updated) {
         MekanismConfig.common.tempUnit.set(updated);
         MekanismConfig.common.save();
      }
   }
}
