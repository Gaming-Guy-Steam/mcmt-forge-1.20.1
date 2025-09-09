package mekanism.client.gui.element.bar;

import java.util.Optional;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiMergedChemicalBar<HANDLER extends IGasTracker & IInfusionTracker & IPigmentTracker & ISlurryTracker>
   extends GuiBar<GuiBar.IBarInfoHandler>
   implements IJEIIngredientHelper {
   private final MergedChemicalTank chemicalTank;
   private final GuiChemicalBar<Gas, GasStack> gasBar;
   private final GuiChemicalBar<InfuseType, InfusionStack> infusionBar;
   private final GuiChemicalBar<Pigment, PigmentStack> pigmentBar;
   private final GuiChemicalBar<Slurry, SlurryStack> slurryBar;

   public GuiMergedChemicalBar(IGuiWrapper gui, HANDLER handler, MergedChemicalTank chemicalTank, int x, int y, int width, int height, boolean horizontal) {
      super(
         TextureAtlas.f_118259_,
         gui,
         new GuiBar.IBarInfoHandler() {
            @Nullable
            private IChemicalTank<?, ?> getCurrentTank() {
               return (IChemicalTank<?, ?>)(switch (chemicalTank.getCurrent()) {
                  case EMPTY -> null;
                  case GAS -> chemicalTank.getGasTank();
                  case INFUSION -> chemicalTank.getInfusionTank();
                  case PIGMENT -> chemicalTank.getPigmentTank();
                  case SLURRY -> chemicalTank.getSlurryTank();
               });
            }

            @Override
            public Component getTooltip() {
               IChemicalTank<?, ?> currentTank = this.getCurrentTank();
               if (currentTank == null) {
                  return MekanismLang.EMPTY.translate(new Object[0]);
               } else {
                  return currentTank.getStored() == Long.MAX_VALUE
                     ? MekanismLang.GENERIC_STORED.translate(new Object[]{currentTank.getType(), MekanismLang.INFINITE})
                     : MekanismLang.GENERIC_STORED_MB.translate(new Object[]{currentTank.getType(), TextUtils.format(currentTank.getStored())});
               }
            }

            @Override
            public double getLevel() {
               IChemicalTank<?, ?> currentTank = this.getCurrentTank();
               return currentTank == null ? 0.0 : (double)currentTank.getStored() / currentTank.getCapacity();
            }
         },
         x,
         y,
         width,
         height,
         horizontal
      );
      this.chemicalTank = chemicalTank;
      this.gasBar = this.addPositionOnlyChild(
         new GuiChemicalBar<>(gui, GuiChemicalBar.getProvider(this.chemicalTank.getGasTank(), handler.getGasTanks(null)), x, y, width, height, horizontal)
      );
      this.infusionBar = this.addPositionOnlyChild(
         new GuiChemicalBar<>(
            gui, GuiChemicalBar.getProvider(this.chemicalTank.getInfusionTank(), handler.getInfusionTanks(null)), x, y, width, height, horizontal
         )
      );
      this.pigmentBar = this.addPositionOnlyChild(
         new GuiChemicalBar<>(
            gui, GuiChemicalBar.getProvider(this.chemicalTank.getPigmentTank(), handler.getPigmentTanks(null)), x, y, width, height, horizontal
         )
      );
      this.slurryBar = this.addPositionOnlyChild(
         new GuiChemicalBar<>(gui, GuiChemicalBar.getProvider(this.chemicalTank.getSlurryTank(), handler.getSlurryTanks(null)), x, y, width, height, horizontal)
      );
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      GuiChemicalBar<?, ?> currentBar = this.getCurrentBarNoFallback();
      if (currentBar == null) {
         super.renderToolTip(guiGraphics, mouseX, mouseY);
      } else {
         currentBar.renderToolTip(guiGraphics, mouseX, mouseY);
      }
   }

   @Override
   void drawContentsChecked(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel, boolean warning) {
      GuiChemicalBar<?, ?> currentBar = this.getCurrentBarNoFallback();
      if (currentBar != null) {
         currentBar.drawContentsChecked(guiGraphics, mouseX, mouseY, partialTicks, handlerLevel, warning);
      }
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
   }

   @Nullable
   @Override
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      GuiChemicalBar<?, ?> currentBar = this.getCurrentBarNoFallback();
      if (currentBar == null) {
         boolean clicked = this.gasBar.m_6375_(mouseX, mouseY, button)
            | this.infusionBar.m_6375_(mouseX, mouseY, button)
            | this.pigmentBar.m_6375_(mouseX, mouseY, button)
            | this.slurryBar.m_6375_(mouseX, mouseY, button);
         return clicked ? this : null;
      } else {
         return currentBar.mouseClickedNested(mouseX, mouseY, button);
      }
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      GuiChemicalBar<?, ?> currentBar = this.getCurrentBarNoFallback();
      return currentBar == null ? Optional.empty() : currentBar.getIngredient(mouseX, mouseY);
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      GuiChemicalBar<?, ?> currentBar = this.getCurrentBarNoFallback();
      return currentBar == null
         ? new Rect2i(this.m_252754_() + 1, this.m_252907_() + 1, this.f_93618_ - 2, this.f_93619_ - 2)
         : currentBar.getIngredientBounds(mouseX, mouseY);
   }

   @Nullable
   private GuiChemicalBar<?, ?> getCurrentBarNoFallback() {
      return switch (this.chemicalTank.getCurrent()) {
         case GAS -> this.gasBar;
         case INFUSION -> this.infusionBar;
         case PIGMENT -> this.pigmentBar;
         case SLURRY -> this.slurryBar;
         default -> null;
      };
   }
}
