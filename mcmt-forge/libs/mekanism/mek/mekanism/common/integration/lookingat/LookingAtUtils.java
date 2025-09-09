package mekanism.common.integration.lookingat;

import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.merged.ChemicalTankWrapper;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler;
import mekanism.common.entity.EntityRobit;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LookingAtUtils {
   public static final ResourceLocation ENERGY = Mekanism.rl("energy");
   public static final ResourceLocation FLUID = Mekanism.rl("fluid");
   public static final ResourceLocation GAS = Mekanism.rl("gas");
   public static final ResourceLocation INFUSE_TYPE = Mekanism.rl("infuse_type");
   public static final ResourceLocation PIGMENT = Mekanism.rl("pigment");
   public static final ResourceLocation SLURRY = Mekanism.rl("slurry");

   private LookingAtUtils() {
   }

   @Nullable
   private static MultiblockData getMultiblock(@NotNull BlockEntity tile) {
      if (tile instanceof IMultiblock<?> multiblock) {
         return multiblock.getMultiblock();
      } else {
         if (tile instanceof IStructuralMultiblock multiblock) {
            for (Entry<MultiblockManager<?>, Structure> entry : multiblock.getStructureMap().entrySet()) {
               if (entry.getKey() != null) {
                  Structure s = entry.getValue();
                  if (s.isValid()) {
                     return s.getMultiblockData();
                  }
               }
            }
         }

         return null;
      }
   }

   public static void addInfo(LookingAtHelper info, @NotNull Entity entity) {
      if (entity instanceof EntityRobit robit) {
         displayEnergy(info, robit);
      }
   }

   public static void addInfo(LookingAtHelper info, @NotNull BlockEntity tile, boolean displayTanks, boolean displayFluidTanks) {
      MultiblockData structure = getMultiblock(tile);
      Optional<IStrictEnergyHandler> energyCapability = CapabilityUtils.getCapability(tile, Capabilities.STRICT_ENERGY, null).resolve();
      if (energyCapability.isPresent()) {
         displayEnergy(info, energyCapability.get());
      } else if (structure != null && structure.isFormed()) {
         displayEnergy(info, structure);
      }

      if (displayTanks) {
         if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
            Optional<IFluidHandler> fluidCapability = CapabilityUtils.getCapability(tile, ForgeCapabilities.FLUID_HANDLER, null).resolve();
            if (fluidCapability.isPresent()) {
               displayFluid(info, fluidCapability.get());
            } else if (structure != null && structure.isFormed()) {
               displayFluid(info, structure);
            }
         }

         addInfo(
            tile,
            structure,
            Capabilities.GAS_HANDLER,
            multiblock -> multiblock.getGasTanks(null),
            info,
            MekanismLang.GAS,
            MergedChemicalTank.Current.GAS,
            MergedTank.CurrentType.GAS
         );
         addInfo(
            tile,
            structure,
            Capabilities.INFUSION_HANDLER,
            multiblock -> multiblock.getInfusionTanks(null),
            info,
            MekanismLang.INFUSE_TYPE,
            MergedChemicalTank.Current.INFUSION,
            MergedTank.CurrentType.INFUSION
         );
         addInfo(
            tile,
            structure,
            Capabilities.PIGMENT_HANDLER,
            multiblock -> multiblock.getPigmentTanks(null),
            info,
            MekanismLang.PIGMENT,
            MergedChemicalTank.Current.PIGMENT,
            MergedTank.CurrentType.PIGMENT
         );
         addInfo(
            tile,
            structure,
            Capabilities.SLURRY_HANDLER,
            multiblock -> multiblock.getSlurryTanks(null),
            info,
            MekanismLang.SLURRY,
            MergedChemicalTank.Current.SLURRY,
            MergedTank.CurrentType.SLURRY
         );
      }
   }

   private static void displayFluid(LookingAtHelper info, IFluidHandler fluidHandler) {
      if (fluidHandler instanceof IMekanismFluidHandler mekFluidHandler) {
         for (IExtendedFluidTank fluidTank : mekFluidHandler.getFluidTanks(null)) {
            if (fluidTank instanceof FluidTankWrapper wrapper) {
               MergedTank mergedTank = wrapper.getMergedTank();
               MergedTank.CurrentType currentType = mergedTank.getCurrentType();
               if (currentType != MergedTank.CurrentType.EMPTY && currentType != MergedTank.CurrentType.FLUID) {
                  continue;
               }
            }

            addFluidInfo(info, fluidTank.getFluid(), fluidTank.getCapacity());
         }
      } else {
         for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            addFluidInfo(info, fluidHandler.getFluidInTank(tank), fluidHandler.getTankCapacity(tank));
         }
      }
   }

   private static void addFluidInfo(LookingAtHelper info, FluidStack fluidInTank, int capacity) {
      if (!fluidInTank.isEmpty()) {
         info.addText(MekanismLang.LIQUID.translate(new Object[]{fluidInTank}));
      }

      info.addFluidElement(fluidInTank, capacity);
   }

   private static void displayEnergy(LookingAtHelper info, IStrictEnergyHandler energyHandler) {
      int containers = energyHandler.getEnergyContainerCount();

      for (int container = 0; container < containers; container++) {
         info.addEnergyElement(energyHandler.getEnergy(container), energyHandler.getMaxEnergy(container));
      }
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void addInfo(
      BlockEntity tile,
      @Nullable MultiblockData structure,
      Capability<HANDLER> capability,
      Function<MultiblockData, List<TANK>> multiBlockToTanks,
      LookingAtHelper info,
      ILangEntry langEntry,
      MergedChemicalTank.Current matchingCurrent,
      MergedTank.CurrentType matchingCurrentType
   ) {
      Optional<HANDLER> cap = CapabilityUtils.getCapability(tile, capability, null).resolve();
      if (cap.isPresent()) {
         HANDLER handler = (HANDLER)cap.get();
         if (handler instanceof ProxyChemicalHandler) {
            List<TANK> tanks = ((ProxyChemicalHandler)handler).getTanksIfMekanism();
            if (!tanks.isEmpty()) {
               for (TANK tank : tanks) {
                  addChemicalTankInfo(info, langEntry, tank, matchingCurrent, matchingCurrentType);
               }

               return;
            }
         }

         if (handler instanceof IMekanismChemicalHandler<CHEMICAL, STACK, TANK> mekHandler) {
            for (TANK tank : mekHandler.getChemicalTanks(null)) {
               addChemicalTankInfo(info, langEntry, tank, matchingCurrent, matchingCurrentType);
            }
         } else {
            for (int i = 0; i < handler.getTanks(); i++) {
               addChemicalInfo(info, langEntry, handler.getChemicalInTank(i), handler.getTankCapacity(i));
            }
         }
      } else if (structure != null && structure.isFormed()) {
         for (TANK tank : multiBlockToTanks.apply(structure)) {
            addChemicalTankInfo(info, langEntry, tank, matchingCurrent, matchingCurrentType);
         }
      }
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void addChemicalTankInfo(
      LookingAtHelper info, ILangEntry langEntry, TANK chemicalTank, MergedChemicalTank.Current matchingCurrent, MergedTank.CurrentType matchingCurrentType
   ) {
      if (chemicalTank instanceof ChemicalTankWrapper) {
         MergedChemicalTank mergedTank = ((ChemicalTankWrapper)chemicalTank).getMergedTank();
         if (mergedTank instanceof MergedTank tank) {
            if (tank.getCurrentType() != matchingCurrentType) {
               return;
            }
         } else {
            MergedChemicalTank.Current current = mergedTank.getCurrent();
            if (current == MergedChemicalTank.Current.EMPTY) {
               if (matchingCurrent != MergedChemicalTank.Current.GAS) {
                  return;
               }
            } else if (current != matchingCurrent) {
               return;
            }
         }
      }

      addChemicalInfo(info, langEntry, chemicalTank.getStack(), chemicalTank.getCapacity());
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void addChemicalInfo(
      LookingAtHelper info, ILangEntry langEntry, STACK chemicalInTank, long capacity
   ) {
      if (!chemicalInTank.isEmpty()) {
         info.addText(langEntry.translate(chemicalInTank.getType()));
      }

      info.addChemicalElement(chemicalInTank, capacity);
   }
}
