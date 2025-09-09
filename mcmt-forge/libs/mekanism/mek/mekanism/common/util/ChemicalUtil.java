package mekanism.common.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalUtil {
   private ChemicalUtil() {
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, ?>> Capability<HANDLER> getCapabilityForChemical(
      CHEMICAL chemical
   ) {
      if (chemical instanceof Gas) {
         return (Capability<HANDLER>)Capabilities.GAS_HANDLER;
      } else if (chemical instanceof InfuseType) {
         return (Capability<HANDLER>)Capabilities.INFUSION_HANDLER;
      } else if (chemical instanceof Pigment) {
         return (Capability<HANDLER>)Capabilities.PIGMENT_HANDLER;
      } else if (chemical instanceof Slurry) {
         return (Capability<HANDLER>)Capabilities.SLURRY_HANDLER;
      } else {
         throw new IllegalStateException("Unknown Chemical Type: " + chemical.getClass().getName());
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER> getCapabilityForChemical(
      STACK stack
   ) {
      return getCapabilityForChemical(stack.getType());
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER> getCapabilityForChemical(
      IChemicalTank<CHEMICAL, STACK> tank
   ) {
      return getCapabilityForChemical(tank.getEmptyStack());
   }

   public static ChemicalStack<?> getEmptyStack(ChemicalType chemicalType) {
      return (ChemicalStack<?>)(switch (chemicalType) {
         case GAS -> GasStack.EMPTY;
         case INFUSION -> InfusionStack.EMPTY;
         case PIGMENT -> PigmentStack.EMPTY;
         case SLURRY -> SlurryStack.EMPTY;
      });
   }

   public static <STACK extends ChemicalStack<?>> STACK getEmptyStack(STACK stack) {
      if (stack instanceof GasStack) {
         return (STACK)GasStack.EMPTY;
      } else if (stack instanceof InfusionStack) {
         return (STACK)InfusionStack.EMPTY;
      } else if (stack instanceof PigmentStack) {
         return (STACK)PigmentStack.EMPTY;
      } else if (stack instanceof SlurryStack) {
         return (STACK)SlurryStack.EMPTY;
      } else {
         throw new IllegalStateException("Unknown Chemical Type: " + stack.getType().getClass().getName());
      }
   }

   public static boolean compareTypes(ChemicalType chemicalType, MergedChemicalTank.Current current) {
      return current == switch (chemicalType) {
         case GAS -> MergedChemicalTank.Current.GAS;
         case INFUSION -> MergedChemicalTank.Current.INFUSION;
         case PIGMENT -> MergedChemicalTank.Current.PIGMENT;
         case SLURRY -> MergedChemicalTank.Current.SLURRY;
      };
   }

   public static <STACK extends ChemicalStack<?>> STACK copy(STACK stack) {
      return (STACK)stack.copy();
   }

   public static <STACK extends ChemicalStack<?>> STACK copyWithAmount(STACK stack, long amount) {
      if (!stack.isEmpty() && amount > 0L) {
         STACK result = copy(stack);
         result.setAmount(amount);
         return result;
      } else {
         return getEmptyStack(stack);
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK withAmount(
      IChemicalProvider<CHEMICAL> provider, long amount
   ) {
      return (STACK)provider.getStack(amount);
   }

   public static ItemStack getFullChemicalTank(ChemicalTankTier tier, @NotNull Chemical<?> chemical) {
      return getFilledVariant(getEmptyChemicalTank(tier), tier.getStorage(), chemical);
   }

   private static ItemStack getEmptyChemicalTank(ChemicalTankTier tier) {
      return (switch (tier) {
         case BASIC -> MekanismBlocks.BASIC_CHEMICAL_TANK;
         case ADVANCED -> MekanismBlocks.ADVANCED_CHEMICAL_TANK;
         case ELITE -> MekanismBlocks.ELITE_CHEMICAL_TANK;
         case ULTIMATE -> MekanismBlocks.ULTIMATE_CHEMICAL_TANK;
         case CREATIVE -> MekanismBlocks.CREATIVE_CHEMICAL_TANK;
      }).getItemStack();
   }

   public static ItemStack getFilledVariant(ItemStack toFill, CachedLongValue capacity, IChemicalProvider<?> provider) {
      return getFilledVariant(toFill, capacity.getOrDefault(), provider);
   }

   public static ItemStack getFilledVariant(ItemStack toFill, long capacity, IChemicalProvider<?> provider) {
      if (provider instanceof IGasProvider gasProvider) {
         return getFilledVariant(toFill, ChemicalTankBuilder.GAS, capacity, gasProvider, "GasTanks");
      } else if (provider instanceof IInfuseTypeProvider infuseTypeProvider) {
         return getFilledVariant(toFill, ChemicalTankBuilder.INFUSION, capacity, infuseTypeProvider, "InfusionTanks");
      } else if (provider instanceof IPigmentProvider pigmentProvider) {
         return getFilledVariant(toFill, ChemicalTankBuilder.PIGMENT, capacity, pigmentProvider, "PigmentTanks");
      } else if (provider instanceof ISlurryProvider slurryProvider) {
         return getFilledVariant(toFill, ChemicalTankBuilder.SLURRY, capacity, slurryProvider, "SlurryTanks");
      } else {
         throw new IllegalStateException("Unknown Chemical Type: " + provider.getChemical().getClass().getName());
      }
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> ItemStack getFilledVariant(
      ItemStack toFill, ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder, long capacity, IChemicalProvider<CHEMICAL> provider, String key
   ) {
      TANK dummyTank = tankBuilder.createDummy(capacity);
      dummyTank.setStack(withAmount(provider, dummyTank.getCapacity()));
      ItemDataUtils.writeContainers(toFill, key, Collections.singletonList(dummyTank));
      return toFill;
   }

   public static int getRGBDurabilityForDisplay(ItemStack stack) {
      GasStack gasStack = StorageUtils.getStoredGasFromNBT(stack);
      if (!gasStack.isEmpty()) {
         return gasStack.getChemicalColorRepresentation();
      } else {
         InfusionStack infusionStack = StorageUtils.getStoredInfusionFromNBT(stack);
         if (!infusionStack.isEmpty()) {
            return infusionStack.getChemicalColorRepresentation();
         } else {
            PigmentStack pigmentStack = StorageUtils.getStoredPigmentFromNBT(stack);
            if (!pigmentStack.isEmpty()) {
               return pigmentStack.getChemicalColorRepresentation();
            } else {
               SlurryStack slurryStack = StorageUtils.getStoredSlurryFromNBT(stack);
               return !slurryStack.isEmpty() ? slurryStack.getChemicalColorRepresentation() : 0;
            }
         }
      }
   }

   public static boolean hasGas(ItemStack stack) {
      return hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.GAS_HANDLER);
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean hasChemical(ItemStack stack, CHEMICAL type) {
      Capability<IChemicalHandler<CHEMICAL, STACK>> capability = getCapabilityForChemical(type);
      return hasChemical(stack, s -> s.isTypeEqual(type), capability);
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean hasChemical(
      ItemStack stack, Predicate<STACK> validityCheck, Capability<HANDLER> capability
   ) {
      Optional<HANDLER> cap = stack.getCapability(capability).resolve();
      if (cap.isPresent()) {
         HANDLER handler = (HANDLER)cap.get();

         for (int tank = 0; tank < handler.getTanks(); tank++) {
            STACK chemicalStack = handler.getChemicalInTank(tank);
            if (!chemicalStack.isEmpty() && validityCheck.test(chemicalStack)) {
               return true;
            }
         }
      }

      return false;
   }

   public static void addAttributeTooltips(List<Component> tooltips, Chemical<?> chemical) {
      chemical.getAttributes().forEach(attr -> attr.addTooltipText(tooltips));
   }

   public static void addChemicalDataToTooltip(List<Component> tooltips, Chemical<?> chemical, boolean advanced) {
      if (!chemical.isEmptyType()) {
         addAttributeTooltips(tooltips, chemical);
         if (chemical instanceof Gas gas && MekanismTags.Gases.WASTE_BARREL_DECAY_LOOKUP.contains(gas)) {
            tooltips.add(MekanismLang.DECAY_IMMUNE.translateColored(EnumColor.AQUA, new Object[0]));
         }

         if (advanced) {
            tooltips.add(TextComponentUtil.build(ChatFormatting.DARK_GRAY, chemical.getRegistryName()));
         }
      }
   }

   public static void emit(IChemicalTank<?, ?> tank, BlockEntity from) {
      emit(EnumSet.allOf(Direction.class), tank, from);
   }

   public static void emit(Set<Direction> outputSides, IChemicalTank<?, ?> tank, BlockEntity from) {
      emit(outputSides, tank, from, tank.getCapacity());
   }

   public static void emit(Set<Direction> outputSides, IChemicalTank<?, ?> tank, BlockEntity from, long maxOutput) {
      if (!tank.isEmpty() && maxOutput > 0L) {
         tank.extract(emit(outputSides, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> long emit(
      Set<Direction> sides, @NotNull STACK stack, BlockEntity from
   ) {
      if (!stack.isEmpty() && !sides.isEmpty()) {
         Capability<IChemicalHandler<CHEMICAL, STACK>> capability = getCapabilityForChemical(stack);
         ChemicalHandlerTarget<CHEMICAL, STACK, IChemicalHandler<CHEMICAL, STACK>> target = new ChemicalHandlerTarget<>(stack, 6);
         EmitUtils.forEachSide(
            from.m_58904_(),
            from.m_58899_(),
            sides,
            (acceptor, side) -> CapabilityUtils.getCapability(acceptor, capability, side.m_122424_()).ifPresent(handler -> {
               if (canInsert(handler, stack)) {
                  target.addHandler(handler);
               }
            })
         );
         return target.getHandlerCount() > 0 ? EmitUtils.sendToAcceptors(target, stack.getAmount(), copy(stack)) : 0L;
      } else {
         return 0L;
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean canInsert(
      HANDLER handler, @NotNull STACK stack
   ) {
      return handler.insertChemical(stack, Action.SIMULATE).getAmount() < stack.getAmount();
   }

   public static Gas gas(GasBuilder builder, @Nullable Integer colorRepresentation) {
      if (colorRepresentation == null) {
         return new Gas(builder);
      } else {
         final int color = colorRepresentation;
         return new Gas(builder) {
            @Override
            public int getColorRepresentation() {
               return color;
            }
         };
      }
   }

   public static InfuseType infuseType(InfuseTypeBuilder builder, @Nullable Integer colorRepresentation) {
      if (colorRepresentation == null) {
         return new InfuseType(builder);
      } else {
         final int color = colorRepresentation;
         return new InfuseType(builder) {
            @Override
            public int getColorRepresentation() {
               return color;
            }
         };
      }
   }

   public static Pigment pigment(PigmentBuilder builder, @Nullable Integer colorRepresentation) {
      if (colorRepresentation == null) {
         return new Pigment(builder);
      } else {
         final int color = colorRepresentation;
         return new Pigment(builder) {
            @Override
            public int getColorRepresentation() {
               return color;
            }
         };
      }
   }

   public static Slurry slurry(SlurryBuilder builder, @Nullable Integer colorRepresentation) {
      if (colorRepresentation == null) {
         return new Slurry(builder);
      } else {
         final int color = colorRepresentation;
         return new Slurry(builder) {
            @Override
            public int getColorRepresentation() {
               return color;
            }
         };
      }
   }
}
