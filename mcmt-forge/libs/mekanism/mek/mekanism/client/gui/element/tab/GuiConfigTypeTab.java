package mekanism.client.gui.element.tab;

import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiConfigTypeTab extends GuiInsetElement<Void> {
   private final TransmissionType transmission;
   private final GuiSideConfiguration<?> config;

   public GuiConfigTypeTab(IGuiWrapper gui, TransmissionType type, int x, int y, GuiSideConfiguration<?> config, boolean left) {
      super(getResource(type), gui, null, x, y, 26, 18, left);
      this.config = config;
      this.transmission = type;
   }

   private static ResourceLocation getResource(TransmissionType t) {
      return MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, t.getTransmission() + ".png");
   }

   public TransmissionType getTransmissionType() {
      return this.transmission;
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, switch (this.transmission) {
         case ENERGY -> SpecialColors.TAB_ENERGY_CONFIG;
         case FLUID -> SpecialColors.TAB_FLUID_CONFIG;
         case GAS -> SpecialColors.TAB_GAS_CONFIG;
         case INFUSION -> SpecialColors.TAB_INFUSION_CONFIG;
         case PIGMENT -> SpecialColors.TAB_PIGMENT_CONFIG;
         case SLURRY -> SpecialColors.TAB_SLURRY_CONFIG;
         case ITEM -> SpecialColors.TAB_ITEM_CONFIG;
         case HEAT -> SpecialColors.TAB_HEAT_CONFIG;
      });
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{TextComponentUtil.build(this.transmission)});
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      this.config.setCurrentType(this.transmission);
      this.config.updateTabs();
   }
}
