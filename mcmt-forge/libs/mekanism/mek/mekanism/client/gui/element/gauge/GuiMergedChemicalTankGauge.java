package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GuiMergedChemicalTankGauge<HANDLER extends IGasTracker & IInfusionTracker & IPigmentTracker & ISlurryTracker>
   extends GuiGauge<Void>
   implements IJEIIngredientHelper {
   private final Supplier<MergedChemicalTank> mergedTankSupplier;
   private final Supplier<HANDLER> handlerSupplier;
   private final GuiGasGauge gasGauge;
   private final GuiInfusionGauge infusionGauge;
   private final GuiPigmentGauge pigmentGauge;
   private final GuiSlurryGauge slurryGauge;
   private Component label;

   public GuiMergedChemicalTankGauge(
      Supplier<MergedChemicalTank> mergedTankSupplier, Supplier<HANDLER> handlerSupplier, GaugeType type, IGuiWrapper gui, int x, int y
   ) {
      this(mergedTankSupplier, handlerSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
   }

   public GuiMergedChemicalTankGauge(
      Supplier<MergedChemicalTank> mergedTankSupplier, Supplier<HANDLER> handlerSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int width, int height
   ) {
      super(type, gui, x, y, width, height);
      this.mergedTankSupplier = mergedTankSupplier;
      this.handlerSupplier = handlerSupplier;
      this.gasGauge = this.addPositionOnlyChild(
         new GuiGasGauge(() -> this.mergedTankSupplier.get().getGasTank(), () -> this.handlerSupplier.get().getGasTanks(null), type, gui, x, y, width, height)
      );
      this.infusionGauge = this.addPositionOnlyChild(
         new GuiInfusionGauge(
            () -> this.mergedTankSupplier.get().getInfusionTank(), () -> this.handlerSupplier.get().getInfusionTanks(null), type, gui, x, y, width, height
         )
      );
      this.pigmentGauge = this.addPositionOnlyChild(
         new GuiPigmentGauge(
            () -> this.mergedTankSupplier.get().getPigmentTank(), () -> this.handlerSupplier.get().getPigmentTanks(null), type, gui, x, y, width, height
         )
      );
      this.slurryGauge = this.addPositionOnlyChild(
         new GuiSlurryGauge(
            () -> this.mergedTankSupplier.get().getSlurryTank(), () -> this.handlerSupplier.get().getSlurryTanks(null), type, gui, x, y, width, height
         )
      );
   }

   public GuiMergedChemicalTankGauge<HANDLER> setLabel(Component label) {
      this.label = label;
      return this;
   }

   @Override
   public GaugeOverlay getGaugeOverlay() {
      return this.getCurrentGauge().getGaugeOverlay();
   }

   @Override
   protected GaugeInfo getGaugeColor() {
      return this.getCurrentGauge().getGaugeColor();
   }

   @Nullable
   @Override
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      GuiTankGauge<?, ?> currentGauge = this.getCurrentGaugeNoFallback();
      if (currentGauge == null) {
         boolean clicked = this.gasGauge.m_6375_(mouseX, mouseY, button)
            | this.infusionGauge.m_6375_(mouseX, mouseY, button)
            | this.pigmentGauge.m_6375_(mouseX, mouseY, button)
            | this.slurryGauge.m_6375_(mouseX, mouseY, button);
         return clicked ? this : null;
      } else {
         return currentGauge.mouseClickedNested(mouseX, mouseY, button);
      }
   }

   @Override
   protected void applyRenderColor(GuiGraphics guiGraphics) {
      GuiTankGauge<?, ?> currentGauge = this.getCurrentGaugeNoFallback();
      if (currentGauge != null) {
         currentGauge.applyRenderColor(guiGraphics);
      }
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      GuiTankGauge<?, ?> currentGauge = this.getCurrentGaugeNoFallback();
      return currentGauge == null ? Optional.empty() : currentGauge.getIngredient(mouseX, mouseY);
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      GuiTankGauge<?, ?> currentGauge = this.getCurrentGaugeNoFallback();
      return currentGauge == null
         ? new Rect2i(this.m_252754_() + 1, this.m_252907_() + 1, this.f_93618_ - 2, this.f_93619_ - 2)
         : currentGauge.getIngredientBounds(mouseX, mouseY);
   }

   @Override
   public int getScaledLevel() {
      GuiTankGauge<?, ?> currentGauge = this.getCurrentGaugeNoFallback();
      return currentGauge == null ? 0 : currentGauge.getScaledLevel();
   }

   @Nullable
   @Override
   public TextureAtlasSprite getIcon() {
      return this.getCurrentGauge().getIcon();
   }

   @Override
   public List<Component> getTooltipText() {
      return this.getCurrentGauge().getTooltipText();
   }

   @Override
   public Component getLabel() {
      return this.label;
   }

   @Override
   public TransmissionType getTransmission() {
      return this.getCurrentGauge().getTransmission();
   }

   private GuiTankGauge<?, ?> getCurrentGauge() {
      GuiTankGauge<?, ?> currentGauge = this.getCurrentGaugeNoFallback();
      return (GuiTankGauge<?, ?>)(currentGauge == null ? this.gasGauge : currentGauge);
   }

   @Nullable
   private GuiTankGauge<?, ?> getCurrentGaugeNoFallback() {
      MergedChemicalTank mergedTank = this.mergedTankSupplier.get();

      return (GuiTankGauge<?, ?>)(switch (mergedTank.getCurrent()) {
         case GAS -> this.gasGauge;
         case INFUSION -> this.infusionGauge;
         case PIGMENT -> this.pigmentGauge;
         case SLURRY -> this.slurryGauge;
         default -> null;
      });
   }
}
