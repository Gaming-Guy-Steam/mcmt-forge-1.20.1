package mekanism.client.render.item;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ChemicalFluidBarDecorator implements IItemDecorator {
   private final Capability<? extends IChemicalHandler<?, ?>>[] chemicalCaps;
   private final boolean showFluid;
   private final Predicate<ItemStack> visibleFor;

   @SafeVarargs
   public ChemicalFluidBarDecorator(boolean showFluid, Predicate<ItemStack> visibleFor, Capability<? extends IChemicalHandler<?, ?>>... chemicalCaps) {
      this.showFluid = showFluid;
      this.chemicalCaps = chemicalCaps;
      this.visibleFor = visibleFor;
   }

   public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
      if (!this.visibleFor.test(stack)) {
         return false;
      } else {
         yOffset += 12;

         for (Capability<? extends IChemicalHandler<?, ?>> chemicalCap : this.chemicalCaps) {
            Optional<? extends IChemicalHandler<?, ?>> capabilityInstance = stack.getCapability(chemicalCap).resolve();
            if (capabilityInstance.isPresent()) {
               IChemicalHandler<?, ?> chemicalHandler = (IChemicalHandler<?, ?>)capabilityInstance.get();
               int tank = this.getDisplayTank(chemicalHandler.getTanks());
               if (tank != -1) {
                  ChemicalStack<?> chemicalInTank = chemicalHandler.getChemicalInTank(tank);
                  renderBar(
                     guiGraphics,
                     xOffset,
                     yOffset,
                     chemicalInTank.getAmount(),
                     chemicalHandler.getTankCapacity(tank),
                     chemicalInTank.getChemicalColorRepresentation()
                  );
                  yOffset--;
               }
            }
         }

         if (this.showFluid) {
            Optional<IFluidHandlerItem> capabilityInstance = FluidUtil.getFluidHandler(stack).resolve();
            if (capabilityInstance.isPresent()) {
               IFluidHandlerItem fluidHandler = capabilityInstance.get();
               int tank = this.getDisplayTank(fluidHandler.getTanks());
               if (tank != -1) {
                  FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                  renderBar(
                     guiGraphics,
                     xOffset,
                     yOffset,
                     fluidInTank.getAmount(),
                     fluidHandler.getTankCapacity(tank),
                     FluidUtils.getRGBDurabilityForDisplay(stack).orElse(-1)
                  );
               }
            }
         }

         return true;
      }
   }

   protected static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, long amount, long capacity, int color) {
      int pixelWidth = convertWidth(StorageUtils.getRatio(amount, capacity));
      GuiUtils.fill(guiGraphics, RenderType.m_286086_(), stackXPos + 2 + pixelWidth, yPos, 13 - pixelWidth, 1, -16777216);
      GuiUtils.fill(guiGraphics, RenderType.m_286086_(), stackXPos + 2, yPos, pixelWidth, 1, color | 0xFF000000);
   }

   private static int convertWidth(double width) {
      return MathUtils.clampToInt(Math.round(13.0 * width));
   }

   private int getDisplayTank(int tanks) {
      if (tanks == 0) {
         return -1;
      } else {
         return tanks > 1 && Minecraft.m_91087_().f_91073_ != null ? (int)(Minecraft.m_91087_().f_91073_.m_46467_() / 20L) % tanks : 0;
      }
   }
}
