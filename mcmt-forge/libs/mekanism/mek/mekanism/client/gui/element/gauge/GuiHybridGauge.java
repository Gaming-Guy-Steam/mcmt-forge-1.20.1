package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GuiHybridGauge extends GuiGauge<Void> implements IJEIIngredientHelper {
   private final Supplier<IGasTank> gasTankSupplier;
   private final GuiGasGauge gasGauge;
   private final GuiFluidGauge fluidGauge;
   private Component label;

   public GuiHybridGauge(
      Supplier<IGasTank> gasTankSupplier,
      Supplier<List<IGasTank>> gasTanksSupplier,
      Supplier<IExtendedFluidTank> fluidTankSupplier,
      Supplier<List<IExtendedFluidTank>> fluidTanksSupplier,
      GaugeType type,
      IGuiWrapper gui,
      int x,
      int y
   ) {
      this(
         gasTankSupplier,
         gasTanksSupplier,
         fluidTankSupplier,
         fluidTanksSupplier,
         type,
         gui,
         x,
         y,
         type.getGaugeOverlay().getWidth() + 2,
         type.getGaugeOverlay().getHeight() + 2
      );
   }

   public GuiHybridGauge(
      Supplier<IGasTank> gasTankSupplier,
      Supplier<List<IGasTank>> gasTanksSupplier,
      Supplier<IExtendedFluidTank> fluidTankSupplier,
      Supplier<List<IExtendedFluidTank>> fluidTanksSupplier,
      GaugeType type,
      IGuiWrapper gui,
      int x,
      int y,
      int width,
      int height
   ) {
      super(type, gui, x, y, width, height);
      this.gasTankSupplier = gasTankSupplier;
      this.gasGauge = this.addPositionOnlyChild(new GuiGasGauge(gasTankSupplier, gasTanksSupplier, type, gui, x, y, width, height));
      this.fluidGauge = this.addPositionOnlyChild(new GuiFluidGauge(fluidTankSupplier, fluidTanksSupplier, type, gui, x, y, width, height));
   }

   public GuiHybridGauge setLabel(Component label) {
      this.label = label;
      return this;
   }

   @Nullable
   @Override
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      boolean clicked = this.gasGauge.m_6375_(mouseX, mouseY, button) | this.fluidGauge.m_6375_(mouseX, mouseY, button);
      return clicked ? this : null;
   }

   @Override
   protected void applyRenderColor(GuiGraphics guiGraphics) {
      this.gasGauge.applyRenderColor(guiGraphics);
      this.fluidGauge.applyRenderColor(guiGraphics);
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      Optional<?> gasIngredient = this.gasGauge.getIngredient(mouseX, mouseY);
      return gasIngredient.isPresent() ? gasIngredient : this.fluidGauge.getIngredient(mouseX, mouseY);
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      Optional<?> gasIngredient = this.gasGauge.getIngredient(mouseX, mouseY);
      return gasIngredient.isPresent() ? this.gasGauge.getIngredientBounds(mouseX, mouseY) : this.fluidGauge.getIngredientBounds(mouseX, mouseY);
   }

   @Override
   public int getScaledLevel() {
      return Math.max(this.gasGauge.getScaledLevel(), this.fluidGauge.getScaledLevel());
   }

   @Nullable
   @Override
   public TextureAtlasSprite getIcon() {
      return this.gasTankSupplier.get() != null && !this.gasTankSupplier.get().isEmpty() ? this.gasGauge.getIcon() : this.fluidGauge.getIcon();
   }

   @Override
   public List<Component> getTooltipText() {
      return this.gasTankSupplier.get() != null && !this.gasTankSupplier.get().isEmpty() ? this.gasGauge.getTooltipText() : this.fluidGauge.getTooltipText();
   }

   @Override
   public Component getLabel() {
      return this.label;
   }

   @Override
   public TransmissionType getTransmission() {
      return this.gasTankSupplier.get() != null && this.gasTankSupplier.get().isEmpty() ? TransmissionType.FLUID : TransmissionType.GAS;
   }
}
