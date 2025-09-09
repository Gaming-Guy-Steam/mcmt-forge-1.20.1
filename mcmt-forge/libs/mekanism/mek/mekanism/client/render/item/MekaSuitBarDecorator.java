package mekanism.client.render.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.FluidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class MekaSuitBarDecorator implements IItemDecorator {
   public static final MekaSuitBarDecorator INSTANCE = new MekaSuitBarDecorator();

   private MekaSuitBarDecorator() {
   }

   public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
      if (!stack.m_41619_() && stack.m_41720_() instanceof ItemMekaSuitArmor armor) {
         yOffset += 12;
         if (this.tryRender(guiGraphics, stack, Capabilities.GAS_HANDLER, xOffset, yOffset, armor.getGasTankSpecs())) {
            yOffset--;
         }

         List<RateLimitMultiTankFluidHandler.FluidTankSpec> fluidTankSpecs = armor.getFluidTankSpecs();
         if (!fluidTankSpecs.isEmpty()) {
            Optional<IFluidHandlerItem> capabilityInstance = FluidUtil.getFluidHandler(stack).resolve();
            if (capabilityInstance.isPresent()) {
               IFluidHandlerItem fluidHandler = capabilityInstance.get();
               int tank = getDisplayTank(fluidTankSpecs, stack, fluidHandler.getTanks());
               if (tank != -1) {
                  FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                  ChemicalFluidBarDecorator.renderBar(
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
      } else {
         return false;
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>> boolean tryRender(
      GuiGraphics guiGraphics,
      ItemStack stack,
      Capability<? extends IChemicalHandler<CHEMICAL, ?>> capability,
      int xOffset,
      int yOffset,
      List<ChemicalTankSpec<CHEMICAL>> chemicalTankSpecs
   ) {
      if (!chemicalTankSpecs.isEmpty() && chemicalTankSpecs.stream().anyMatch(spec -> spec.supportsStack(stack))) {
         Optional<? extends IChemicalHandler<CHEMICAL, ?>> capabilityInstance = stack.getCapability(capability).resolve();
         if (capabilityInstance.isPresent()) {
            IChemicalHandler<CHEMICAL, ?> chemicalHandler = (IChemicalHandler<CHEMICAL, ?>)capabilityInstance.get();
            int tank = getDisplayTank(chemicalTankSpecs, stack, chemicalHandler.getTanks());
            if (tank != -1) {
               ChemicalStack<CHEMICAL> chemicalInTank = chemicalHandler.getChemicalInTank(tank);
               ChemicalFluidBarDecorator.renderBar(
                  guiGraphics,
                  xOffset,
                  yOffset,
                  chemicalInTank.getAmount(),
                  chemicalHandler.getTankCapacity(tank),
                  chemicalInTank.getChemicalColorRepresentation()
               );
               return true;
            }
         }
      }

      return false;
   }

   private static <TYPE> int getDisplayTank(List<? extends GenericTankSpec<TYPE>> tankSpecs, ItemStack stack, int tanks) {
      if (tanks == 0) {
         return -1;
      } else if (tanks > 1 && tanks == tankSpecs.size() && Minecraft.m_91087_().f_91073_ != null) {
         IntList tankIndices = new IntArrayList(tanks);

         for (int i = 0; i < tanks; i++) {
            if (tankSpecs.get(i).supportsStack(stack)) {
               tankIndices.add(i);
            }
         }

         if (tankIndices.isEmpty()) {
            return -1;
         } else {
            return tankIndices.size() == 1
               ? tankIndices.getInt(0)
               : tankIndices.getInt((int)(Minecraft.m_91087_().f_91073_.m_46467_() / 20L) % tankIndices.size());
         }
      } else {
         for (int ix = 0; ix < tanks && ix < tankSpecs.size(); ix++) {
            if (tankSpecs.get(ix).supportsStack(stack)) {
               return ix;
            }
         }

         return -1;
      }
   }
}
