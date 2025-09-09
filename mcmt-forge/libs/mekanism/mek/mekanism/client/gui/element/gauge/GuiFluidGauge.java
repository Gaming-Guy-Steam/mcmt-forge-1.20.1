package mekanism.client.gui.element.gauge;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class GuiFluidGauge extends GuiTankGauge<FluidStack, IExtendedFluidTank> {
   private Component label;

   public GuiFluidGauge(GuiTankGauge.ITankInfoHandler<IExtendedFluidTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
      super(type, gui, x, y, sizeX, sizeY, handler, PacketDropperUse.TankType.FLUID_TANK);
      this.setDummyType(FluidStack.EMPTY);
   }

   public GuiFluidGauge(
      Supplier<IExtendedFluidTank> tankSupplier, Supplier<List<IExtendedFluidTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y
   ) {
      this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
   }

   public GuiFluidGauge(
      Supplier<IExtendedFluidTank> tankSupplier,
      Supplier<List<IExtendedFluidTank>> tanksSupplier,
      GaugeType type,
      IGuiWrapper gui,
      int x,
      int y,
      int sizeX,
      int sizeY
   ) {
      this(new GuiTankGauge.ITankInfoHandler<IExtendedFluidTank>() {
         @Nullable
         public IExtendedFluidTank getTank() {
            return tankSupplier.get();
         }

         @Override
         public int getTankIndex() {
            IExtendedFluidTank tank = this.getTank();
            return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
         }
      }, type, gui, x, y, sizeX, sizeY);
   }

   public GuiFluidGauge setLabel(Component label) {
      this.label = label;
      return this;
   }

   public static GuiFluidGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
      GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
      gauge.dummy = true;
      return gauge;
   }

   @Override
   public TransmissionType getTransmission() {
      return TransmissionType.FLUID;
   }

   @Override
   public int getScaledLevel() {
      if (this.dummy) {
         return this.f_93619_ - 2;
      } else {
         IExtendedFluidTank tank = this.getTank();
         if (tank != null && !tank.isEmpty() && tank.getCapacity() != 0) {
            if (tank.getFluidAmount() == Integer.MAX_VALUE) {
               return this.f_93619_ - 2;
            } else {
               float scale = (float)tank.getFluidAmount() / tank.getCapacity();
               return Math.round(scale * (this.f_93619_ - 2));
            }
         } else {
            return 0;
         }
      }
   }

   @Nullable
   @Override
   public TextureAtlasSprite getIcon() {
      if (this.dummy) {
         return MekanismRenderer.getFluidTexture(this.dummyType, MekanismRenderer.FluidTextureType.STILL);
      } else {
         IExtendedFluidTank tank = this.getTank();
         return tank != null && !tank.isEmpty() ? MekanismRenderer.getFluidTexture(tank.getFluid(), MekanismRenderer.FluidTextureType.STILL) : null;
      }
   }

   @Override
   public Component getLabel() {
      return this.label;
   }

   @Override
   public List<Component> getTooltipText() {
      if (this.dummy) {
         return Collections.singletonList(TextComponentUtil.build(this.dummyType));
      } else {
         IExtendedFluidTank tank = this.getTank();
         if (tank != null && !tank.isEmpty()) {
            int amount = tank.getFluidAmount();
            FluidStack fluidStack = tank.getFluid();
            return amount == Integer.MAX_VALUE
               ? Collections.singletonList(MekanismLang.GENERIC_STORED.translate(new Object[]{fluidStack, MekanismLang.INFINITE}))
               : Collections.singletonList(MekanismLang.GENERIC_STORED_MB.translate(new Object[]{fluidStack, TextUtils.format((long)amount)}));
         } else {
            return Collections.singletonList(MekanismLang.EMPTY.translate(new Object[0]));
         }
      }
   }

   @Override
   protected void applyRenderColor(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, !this.dummy && this.getTank() != null ? this.getTank().getFluid() : this.dummyType);
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      return this.getTank().isEmpty() ? Optional.empty() : Optional.of(this.getTank().getFluid());
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      return new Rect2i(this.m_252754_() + 1, this.m_252907_() + 1, this.f_93618_ - 2, this.f_93619_ - 2);
   }
}
