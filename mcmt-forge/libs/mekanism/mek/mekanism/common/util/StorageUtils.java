package mekanism.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageUtils {
   private StorageUtils() {
   }

   public static void addStoredEnergy(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap) {
      addStoredEnergy(stack, tooltip, showMissingCap, MekanismLang.STORED_ENERGY);
   }

   public static void addStoredEnergy(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, ILangEntry langEntry) {
      Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY).resolve();
      if (capability.isPresent()) {
         IStrictEnergyHandler energyHandlerItem = capability.get();
         int energyContainerCount = energyHandlerItem.getEnergyContainerCount();

         for (int container = 0; container < energyContainerCount; container++) {
            tooltip.add(
               langEntry.translateColored(
                  EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(energyHandlerItem.getEnergy(container), energyHandlerItem.getMaxEnergy(container))
               )
            );
         }
      } else if (showMissingCap) {
         tooltip.add(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.ZERO));
      }
   }

   public static void addStoredGas(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, boolean showAttributes) {
      addStoredGas(stack, tooltip, showMissingCap, showAttributes, MekanismLang.NO_GAS);
   }

   public static void addStoredGas(
      @NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, boolean showAttributes, ILangEntry emptyLangEntry
   ) {
      addStoredChemical(
         stack,
         tooltip,
         showMissingCap,
         showAttributes,
         emptyLangEntry,
         stored -> stored.isEmpty()
            ? emptyLangEntry.translateColored(EnumColor.GRAY)
            : MekanismLang.STORED
               .translateColored(
                  EnumColor.ORANGE,
                  new Object[]{EnumColor.ORANGE, stored, EnumColor.GRAY, MekanismLang.GENERIC_MB.translate(new Object[]{TextUtils.format(stored.getAmount())})}
               ),
         Capabilities.GAS_HANDLER
      );
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void addStoredChemical(
      @NotNull ItemStack stack,
      @NotNull List<Component> tooltip,
      boolean showMissingCap,
      boolean showAttributes,
      ILangEntry emptyLangEntry,
      Function<STACK, Component> storedFunction,
      Capability<HANDLER> capability
   ) {
      Optional<HANDLER> cap = stack.getCapability(capability).resolve();
      if (cap.isPresent()) {
         HANDLER handler = (HANDLER)cap.get();
         int tank = 0;

         for (int tanks = handler.getTanks(); tank < tanks; tank++) {
            STACK chemicalInTank = handler.getChemicalInTank(tank);
            tooltip.add(storedFunction.apply(chemicalInTank));
            if (showAttributes) {
               ChemicalUtil.addAttributeTooltips(tooltip, chemicalInTank.getType());
            }
         }
      } else if (showMissingCap) {
         tooltip.add(emptyLangEntry.translate());
      }
   }

   public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap) {
      addStoredFluid(stack, tooltip, showMissingCap, MekanismLang.NO_FLUID_TOOLTIP);
   }

   public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, ILangEntry emptyLangEntry) {
      addStoredFluid(
         stack,
         tooltip,
         showMissingCap,
         emptyLangEntry,
         stored -> stored.isEmpty()
            ? emptyLangEntry.translateColored(EnumColor.GRAY)
            : MekanismLang.STORED
               .translateColored(
                  EnumColor.ORANGE,
                  new Object[]{
                     EnumColor.ORANGE, stored, EnumColor.GRAY, MekanismLang.GENERIC_MB.translate(new Object[]{TextUtils.format((long)stored.getAmount())})
                  }
               )
      );
   }

   public static void addStoredFluid(
      @NotNull ItemStack stack,
      @NotNull List<Component> tooltip,
      boolean showMissingCap,
      ILangEntry emptyLangEntry,
      Function<FluidStack, Component> storedFunction
   ) {
      Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
      if (cap.isPresent()) {
         IFluidHandlerItem handler = cap.get();
         int tank = 0;

         for (int tanks = handler.getTanks(); tank < tanks; tank++) {
            tooltip.add(storedFunction.apply(handler.getFluidInTank(tank)));
         }
      } else if (showMissingCap) {
         tooltip.add(emptyLangEntry.translate());
      }
   }

   public static void addStoredSubstance(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean isCreative) {
      FluidStack fluidStack = getStoredFluidFromNBT(stack);
      GasStack gasStack = getStoredGasFromNBT(stack);
      InfusionStack infusionStack = getStoredInfusionFromNBT(stack);
      PigmentStack pigmentStack = getStoredPigmentFromNBT(stack);
      SlurryStack slurryStack = getStoredSlurryFromNBT(stack);
      if (fluidStack.isEmpty() && gasStack.isEmpty() && infusionStack.isEmpty() && pigmentStack.isEmpty() && slurryStack.isEmpty()) {
         tooltip.add(MekanismLang.EMPTY.translate(new Object[0]));
      } else {
         ILangEntry type;
         Object contents;
         long amount;
         if (!fluidStack.isEmpty()) {
            contents = fluidStack;
            amount = fluidStack.getAmount();
            type = MekanismLang.LIQUID;
         } else {
            ChemicalStack<?> chemicalStack;
            if (!gasStack.isEmpty()) {
               chemicalStack = gasStack;
               type = MekanismLang.GAS;
            } else if (!infusionStack.isEmpty()) {
               chemicalStack = infusionStack;
               type = MekanismLang.INFUSE_TYPE;
            } else if (!pigmentStack.isEmpty()) {
               chemicalStack = pigmentStack;
               type = MekanismLang.PIGMENT;
            } else {
               if (slurryStack.isEmpty()) {
                  throw new IllegalStateException("Unknown chemical");
               }

               chemicalStack = slurryStack;
               type = MekanismLang.SLURRY;
            }

            contents = chemicalStack;
            amount = chemicalStack.getAmount();
         }

         if (isCreative) {
            tooltip.add(
               type.translateColored(
                  EnumColor.YELLOW, EnumColor.ORANGE, MekanismLang.GENERIC_STORED.translate(new Object[]{contents, EnumColor.GRAY, MekanismLang.INFINITE})
               )
            );
         } else {
            tooltip.add(
               type.translateColored(
                  EnumColor.YELLOW,
                  EnumColor.ORANGE,
                  MekanismLang.GENERIC_STORED_MB.translate(new Object[]{contents, EnumColor.GRAY, TextUtils.format(amount)})
               )
            );
         }
      }
   }

   @NotNull
   public static FluidStack getStoredFluidFromNBT(ItemStack stack) {
      BasicFluidTank tank = BasicFluidTank.create(Integer.MAX_VALUE, null);
      ItemDataUtils.readContainers(stack, "FluidTanks", Collections.singletonList(tank));
      return tank.getFluid();
   }

   @NotNull
   public static GasStack getStoredGasFromNBT(ItemStack stack) {
      return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.GAS.createDummy(Long.MAX_VALUE), "GasTanks");
   }

   @NotNull
   public static InfusionStack getStoredInfusionFromNBT(ItemStack stack) {
      return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.INFUSION.createDummy(Long.MAX_VALUE), "InfusionTanks");
   }

   @NotNull
   public static PigmentStack getStoredPigmentFromNBT(ItemStack stack) {
      return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.PIGMENT.createDummy(Long.MAX_VALUE), "PigmentTanks");
   }

   @NotNull
   public static SlurryStack getStoredSlurryFromNBT(ItemStack stack) {
      return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.SLURRY.createDummy(Long.MAX_VALUE), "SlurryTanks");
   }

   @NotNull
   private static <STACK extends ChemicalStack<?>> STACK getStoredChemicalFromNBT(ItemStack stack, IChemicalTank<?, STACK> tank, String tag) {
      ItemDataUtils.readContainers(stack, tag, Collections.singletonList(tank));
      return tank.getStack();
   }

   public static FloatingLong getStoredEnergyFromNBT(ItemStack stack) {
      BasicEnergyContainer container = BasicEnergyContainer.create(FloatingLong.MAX_VALUE, null);
      ItemDataUtils.readContainers(stack, "EnergyContainers", Collections.singletonList(container));
      return container.getEnergy();
   }

   public static ItemStack getFilledEnergyVariant(ItemStack toFill, CachedFloatingLongValue capacity) {
      return getFilledEnergyVariant(toFill, capacity.getOrDefault());
   }

   public static ItemStack getFilledEnergyVariant(ItemStack toFill, FloatingLong capacity) {
      BasicEnergyContainer container = BasicEnergyContainer.create(capacity, null);
      container.setEnergy(capacity);
      ItemDataUtils.writeContainers(toFill, "EnergyContainers", Collections.singletonList(container));
      return toFill;
   }

   @Nullable
   public static IEnergyContainer getEnergyContainer(ItemStack stack, int container) {
      if (!stack.m_41619_()) {
         Optional<IStrictEnergyHandler> energyCapability = stack.getCapability(Capabilities.STRICT_ENERGY).resolve();
         if (energyCapability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = energyCapability.get();
            if (energyHandlerItem instanceof IMekanismStrictEnergyHandler energyHandler) {
               return energyHandler.getEnergyContainer(container, null);
            }
         }
      }

      return null;
   }

   public static double getEnergyRatio(ItemStack stack) {
      IEnergyContainer container = getEnergyContainer(stack, 0);
      double ratio = 0.0;
      if (container != null) {
         ratio = container.getEnergy().divideToLevel(container.getMaxEnergy());
      }

      return ratio;
   }

   public static Component getEnergyPercent(ItemStack stack, boolean colorText) {
      return getStoragePercent(getEnergyRatio(stack), colorText);
   }

   public static Component getStoragePercent(double ratio, boolean colorText) {
      Component text = TextUtils.getPercent(ratio);
      if (!colorText) {
         return text;
      } else {
         EnumColor color;
         if (ratio < 0.01F) {
            color = EnumColor.DARK_RED;
         } else if (ratio < 0.1F) {
            color = EnumColor.RED;
         } else if (ratio < 0.25) {
            color = EnumColor.ORANGE;
         } else if (ratio < 0.5) {
            color = EnumColor.YELLOW;
         } else {
            color = EnumColor.BRIGHT_GREEN;
         }

         return TextComponentUtil.build(color, text);
      }
   }

   public static int getBarWidth(ItemStack stack) {
      return MathUtils.clampToInt(Math.round(13.0 - 13.0 * getDurabilityForDisplay(stack)));
   }

   private static double getDurabilityForDisplay(ItemStack stack) {
      double bestRatio = 0.0;
      bestRatio = calculateRatio(stack, bestRatio, Capabilities.GAS_HANDLER);
      bestRatio = calculateRatio(stack, bestRatio, Capabilities.INFUSION_HANDLER);
      bestRatio = calculateRatio(stack, bestRatio, Capabilities.PIGMENT_HANDLER);
      bestRatio = calculateRatio(stack, bestRatio, Capabilities.SLURRY_HANDLER);
      Optional<IFluidHandlerItem> fluidCapability = FluidUtil.getFluidHandler(stack).resolve();
      if (fluidCapability.isPresent()) {
         IFluidHandlerItem fluidHandlerItem = fluidCapability.get();
         int tanks = fluidHandlerItem.getTanks();

         for (int tank = 0; tank < tanks; tank++) {
            bestRatio = Math.max(bestRatio, getRatio(fluidHandlerItem.getFluidInTank(tank).getAmount(), fluidHandlerItem.getTankCapacity(tank)));
         }
      }

      return 1.0 - bestRatio;
   }

   public static int getEnergyBarWidth(ItemStack stack) {
      return MathUtils.clampToInt(Math.round(13.0 - 13.0 * getEnergyDurabilityForDisplay(stack)));
   }

   private static double getEnergyDurabilityForDisplay(ItemStack stack) {
      double bestRatio = 0.0;
      Optional<IStrictEnergyHandler> energyCapability = stack.getCapability(Capabilities.STRICT_ENERGY).resolve();
      if (energyCapability.isPresent()) {
         IStrictEnergyHandler energyHandlerItem = energyCapability.get();
         int containers = energyHandlerItem.getEnergyContainerCount();

         for (int container = 0; container < containers; container++) {
            bestRatio = Math.max(bestRatio, energyHandlerItem.getEnergy(container).divideToLevel(energyHandlerItem.getMaxEnergy(container)));
         }
      }

      return 1.0 - bestRatio;
   }

   private static double calculateRatio(ItemStack stack, double bestRatio, Capability<? extends IChemicalHandler<?, ?>> capability) {
      Optional<? extends IChemicalHandler<?, ?>> cap = stack.getCapability(capability).resolve();
      if (cap.isPresent()) {
         IChemicalHandler<?, ?> handler = (IChemicalHandler<?, ?>)cap.get();
         int tank = 0;

         for (int tanks = handler.getTanks(); tank < tanks; tank++) {
            bestRatio = Math.max(bestRatio, getRatio(handler.getChemicalInTank(tank).getAmount(), handler.getTankCapacity(tank)));
         }
      }

      return bestRatio;
   }

   public static double getRatio(long amount, long capacity) {
      return capacity == 0L ? 1.0 : (double)amount / capacity;
   }

   public static void mergeFluidTanks(List<IExtendedFluidTank> tanks, List<IExtendedFluidTank> toAdd, List<FluidStack> rejects) {
      validateSizeMatches(tanks, toAdd, "tank");

      for (int i = 0; i < toAdd.size(); i++) {
         IExtendedFluidTank mergeTank = toAdd.get(i);
         if (!mergeTank.isEmpty()) {
            IExtendedFluidTank tank = tanks.get(i);
            FluidStack mergeStack = mergeTank.getFluid();
            if (tank.isEmpty()) {
               int capacity = tank.getCapacity();
               if (mergeStack.getAmount() <= capacity) {
                  tank.setStack(mergeStack);
               } else {
                  tank.setStack(new FluidStack(mergeStack, capacity));
                  int remaining = mergeStack.getAmount() - capacity;
                  if (remaining > 0) {
                     rejects.add(new FluidStack(mergeStack, remaining));
                  }
               }
            } else if (tank.isFluidEqual(mergeStack)) {
               int amount = tank.growStack(mergeStack.getAmount(), Action.EXECUTE);
               int remaining = mergeStack.getAmount() - amount;
               if (remaining > 0) {
                  rejects.add(new FluidStack(mergeStack, remaining));
               }
            } else {
               rejects.add(mergeStack);
            }
         }
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void mergeTanks(
      List<TANK> tanks, List<TANK> toAdd, List<STACK> rejects
   ) {
      validateSizeMatches(tanks, toAdd, "tank");

      for (int i = 0; i < toAdd.size(); i++) {
         TANK mergeTank = (TANK)toAdd.get(i);
         if (!mergeTank.isEmpty()) {
            TANK tank = (TANK)tanks.get(i);
            STACK mergeStack = mergeTank.getStack();
            if (tank.isEmpty()) {
               long capacity = tank.getCapacity();
               if (mergeStack.getAmount() <= capacity) {
                  tank.setStack(mergeStack);
               } else {
                  tank.setStack(ChemicalUtil.copyWithAmount(mergeStack, capacity));
                  long remaining = mergeStack.getAmount() - capacity;
                  if (remaining > 0L) {
                     rejects.add(ChemicalUtil.copyWithAmount(mergeStack, remaining));
                  }
               }
            } else if (tank.isTypeEqual(mergeStack)) {
               long amount = tank.growStack(mergeStack.getAmount(), Action.EXECUTE);
               long remaining = mergeStack.getAmount() - amount;
               if (remaining > 0L) {
                  rejects.add(ChemicalUtil.copyWithAmount(mergeStack, remaining));
               }
            } else {
               rejects.add(mergeStack);
            }
         }
      }
   }

   public static void mergeEnergyContainers(List<IEnergyContainer> containers, List<IEnergyContainer> toAdd) {
      validateSizeMatches(containers, toAdd, "energy container");

      for (int i = 0; i < toAdd.size(); i++) {
         IEnergyContainer container = containers.get(i);
         IEnergyContainer mergeContainer = toAdd.get(i);
         container.setEnergy(container.getEnergy().add(mergeContainer.getEnergy()));
      }
   }

   public static void mergeHeatCapacitors(List<IHeatCapacitor> capacitors, List<IHeatCapacitor> toAdd) {
      validateSizeMatches(capacitors, toAdd, "heat capacitor");

      for (int i = 0; i < toAdd.size(); i++) {
         IHeatCapacitor capacitor = capacitors.get(i);
         IHeatCapacitor mergeCapacitor = toAdd.get(i);
         capacitor.setHeat(capacitor.getHeat() + mergeCapacitor.getHeat());
         if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
            heatCapacitor.setHeatCapacity(capacitor.getHeatCapacity() + mergeCapacitor.getHeatCapacity(), false);
         }
      }
   }

   public static <T> void validateSizeMatches(List<T> base, List<T> toAdd, String type) {
      if (base.size() != toAdd.size()) {
         throw new IllegalArgumentException("Mismatched " + type + " count, orig: " + base.size() + ", toAdd: " + toAdd.size());
      }
   }
}
