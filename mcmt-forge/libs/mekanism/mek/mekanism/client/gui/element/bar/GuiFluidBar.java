package mekanism.client.gui.element.bar;

import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class GuiFluidBar extends GuiTankBar<FluidStack> {
   public GuiFluidBar(IGuiWrapper gui, GuiTankBar.TankInfoProvider<FluidStack> infoProvider, int x, int y, int width, int height, boolean horizontal) {
      super(gui, infoProvider, x, y, width, height, horizontal);
   }

   protected boolean isEmpty(FluidStack stack) {
      return stack.isEmpty();
   }

   protected PacketDropperUse.TankType getType(FluidStack stack) {
      return PacketDropperUse.TankType.FLUID_TANK;
   }

   protected void applyRenderColor(GuiGraphics guiGraphics, FluidStack stack) {
      MekanismRenderer.color(guiGraphics, stack);
   }

   protected TextureAtlasSprite getIcon(FluidStack stack) {
      return MekanismRenderer.getFluidTexture(stack, MekanismRenderer.FluidTextureType.STILL);
   }

   public static GuiTankBar.TankInfoProvider<FluidStack> getProvider(IExtendedFluidTank tank, List<IExtendedFluidTank> tanks) {
      return new GuiTankBar.TankInfoProvider<FluidStack>() {
         @NotNull
         public FluidStack getStack() {
            return tank.getFluid();
         }

         @Override
         public int getTankIndex() {
            return tanks.indexOf(tank);
         }

         @Override
         public Component getTooltip() {
            if (tank.isEmpty()) {
               return MekanismLang.EMPTY.translate(new Object[0]);
            } else {
               return tank.getFluidAmount() == Integer.MAX_VALUE
                  ? MekanismLang.GENERIC_STORED.translate(new Object[]{tank.getFluid(), MekanismLang.INFINITE})
                  : MekanismLang.GENERIC_STORED_MB.translate(new Object[]{tank.getFluid(), TextUtils.format((long)tank.getFluidAmount())});
            }
         }

         @Override
         public double getLevel() {
            return (double)tank.getFluidAmount() / tank.getCapacity();
         }
      };
   }
}
