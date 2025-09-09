package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MergedChemicalInventorySlot<MERGED extends MergedChemicalTank> extends BasicInventorySlot {
   protected final MERGED mergedTank;

   private static boolean hasCapability(@NotNull ItemStack stack) {
      return stack.getCapability(Capabilities.GAS_HANDLER).isPresent()
         || stack.getCapability(Capabilities.INFUSION_HANDLER).isPresent()
         || stack.getCapability(Capabilities.PIGMENT_HANDLER).isPresent()
         || stack.getCapability(Capabilities.SLURRY_HANDLER).isPresent();
   }

   public static MergedChemicalInventorySlot<MergedChemicalTank> drain(MergedChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(chemicalTank, "Merged chemical tank cannot be null");
      Predicate<ItemStack> gasInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getGasTank(), GasInventorySlot::getCapability);
      Predicate<ItemStack> infusionInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(
         chemicalTank.getInfusionTank(), InfusionInventorySlot::getCapability
      );
      Predicate<ItemStack> pigmentInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(
         chemicalTank.getPigmentTank(), PigmentInventorySlot::getCapability
      );
      Predicate<ItemStack> slurryInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(
         chemicalTank.getSlurryTank(), SlurryInventorySlot::getCapability
      );
      BiPredicate<ItemStack, AutomationType> insertPredicate = (stack, automationType) -> {
         return switch (chemicalTank.getCurrent()) {
            case GAS -> gasInsertPredicate.test(stack);
            case INFUSION -> infusionInsertPredicate.test(stack);
            case PIGMENT -> pigmentInsertPredicate.test(stack);
            case SLURRY -> slurryInsertPredicate.test(stack);
            case EMPTY -> gasInsertPredicate.test(stack)
               || infusionInsertPredicate.test(stack)
               || pigmentInsertPredicate.test(stack)
               || slurryInsertPredicate.test(stack);
         };
      };
      return new MergedChemicalInventorySlot<>(
         chemicalTank,
         (stack, automationType) -> automationType == AutomationType.MANUAL || !insertPredicate.test(stack, automationType),
         insertPredicate,
         MergedChemicalInventorySlot::hasCapability,
         listener,
         x,
         y
      );
   }

   public static MergedChemicalInventorySlot<MergedChemicalTank> fill(MergedChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(chemicalTank, "Merged chemical tank cannot be null");
      Predicate<ItemStack> gasExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getGasTank(), GasInventorySlot::getCapability);
      Predicate<ItemStack> infusionExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(
         chemicalTank.getInfusionTank(), InfusionInventorySlot::getCapability
      );
      Predicate<ItemStack> pigmentExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(
         chemicalTank.getPigmentTank(), PigmentInventorySlot::getCapability
      );
      Predicate<ItemStack> slurryExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(
         chemicalTank.getSlurryTank(), SlurryInventorySlot::getCapability
      );
      Predicate<ItemStack> gasInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getGasTank(), GasInventorySlot.getCapability(stack));
      Predicate<ItemStack> infusionInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(
         chemicalTank.getInfusionTank(), InfusionInventorySlot.getCapability(stack)
      );
      Predicate<ItemStack> pigmentInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(
         chemicalTank.getPigmentTank(), PigmentInventorySlot.getCapability(stack)
      );
      Predicate<ItemStack> slurryInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(
         chemicalTank.getSlurryTank(), SlurryInventorySlot.getCapability(stack)
      );
      return new MergedChemicalInventorySlot<>(
         chemicalTank,
         (stack, automationType) -> {
            if (automationType == AutomationType.MANUAL) {
               return true;
            } else {
               return switch (chemicalTank.getCurrent()) {
                  case GAS -> gasExtractPredicate.test(stack);
                  case INFUSION -> infusionExtractPredicate.test(stack);
                  case PIGMENT -> pigmentExtractPredicate.test(stack);
                  case SLURRY -> slurryExtractPredicate.test(stack);
                  case EMPTY -> gasExtractPredicate.test(stack)
                     && infusionExtractPredicate.test(stack)
                     && pigmentExtractPredicate.test(stack)
                     && slurryExtractPredicate.test(stack);
               };
            }
         },
         (stack, automationType) -> {
            return switch (chemicalTank.getCurrent()) {
               case GAS -> gasInsertPredicate.test(stack);
               case INFUSION -> infusionInsertPredicate.test(stack);
               case PIGMENT -> pigmentInsertPredicate.test(stack);
               case SLURRY -> slurryInsertPredicate.test(stack);
               case EMPTY -> gasInsertPredicate.test(stack)
                  || infusionInsertPredicate.test(stack)
                  || pigmentInsertPredicate.test(stack)
                  || slurryInsertPredicate.test(stack);
            };
         },
         MergedChemicalInventorySlot::hasCapability,
         listener,
         x,
         y
      );
   }

   protected MergedChemicalInventorySlot(
      MERGED mergedTank,
      BiPredicate<ItemStack, AutomationType> canExtract,
      BiPredicate<ItemStack, AutomationType> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(canExtract, canInsert, validator, listener, x, y);
      this.setSlotType(ContainerSlotType.EXTRA);
      this.mergedTank = mergedTank;
   }

   public void drainChemicalTanks() {
      this.drainChemicalTank(MergedTank.CurrentType.GAS);
      this.drainChemicalTank(MergedTank.CurrentType.INFUSION);
      this.drainChemicalTank(MergedTank.CurrentType.PIGMENT);
      this.drainChemicalTank(MergedTank.CurrentType.SLURRY);
   }

   public void drainChemicalTank(MergedTank.CurrentType type) {
      if (type == MergedTank.CurrentType.GAS) {
         ChemicalInventorySlot.drainChemicalTank(this, this.mergedTank.getGasTank(), GasInventorySlot.getCapability(this.current));
      } else if (type == MergedTank.CurrentType.INFUSION) {
         ChemicalInventorySlot.drainChemicalTank(this, this.mergedTank.getInfusionTank(), InfusionInventorySlot.getCapability(this.current));
      } else if (type == MergedTank.CurrentType.PIGMENT) {
         ChemicalInventorySlot.drainChemicalTank(this, this.mergedTank.getPigmentTank(), PigmentInventorySlot.getCapability(this.current));
      } else if (type == MergedTank.CurrentType.SLURRY) {
         ChemicalInventorySlot.drainChemicalTank(this, this.mergedTank.getSlurryTank(), SlurryInventorySlot.getCapability(this.current));
      }
   }

   public void fillChemicalTanks() {
      this.fillChemicalTank(MergedTank.CurrentType.GAS);
      this.fillChemicalTank(MergedTank.CurrentType.INFUSION);
      this.fillChemicalTank(MergedTank.CurrentType.PIGMENT);
      this.fillChemicalTank(MergedTank.CurrentType.SLURRY);
   }

   public void fillChemicalTank(MergedTank.CurrentType type) {
      if (type == MergedTank.CurrentType.GAS) {
         ChemicalInventorySlot.fillChemicalTank(this, this.mergedTank.getGasTank(), GasInventorySlot.getCapability(this.current));
      } else if (type == MergedTank.CurrentType.INFUSION) {
         ChemicalInventorySlot.fillChemicalTank(this, this.mergedTank.getInfusionTank(), InfusionInventorySlot.getCapability(this.current));
      } else if (type == MergedTank.CurrentType.PIGMENT) {
         ChemicalInventorySlot.fillChemicalTank(this, this.mergedTank.getPigmentTank(), PigmentInventorySlot.getCapability(this.current));
      } else if (type == MergedTank.CurrentType.SLURRY) {
         ChemicalInventorySlot.fillChemicalTank(this, this.mergedTank.getSlurryTank(), SlurryInventorySlot.getCapability(this.current));
      }
   }
}
