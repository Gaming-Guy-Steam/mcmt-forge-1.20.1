package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.inventory.slot.chemical.PigmentInventorySlot;
import mekanism.common.inventory.slot.chemical.SlurryInventorySlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HybridInventorySlot extends MergedChemicalInventorySlot<MergedTank> implements IFluidHandlerSlot {
   private boolean isDraining;
   private boolean isFilling;

   private static boolean hasCapability(@NotNull ItemStack stack) {
      return FluidUtil.getFluidHandler(stack).isPresent()
         || stack.getCapability(Capabilities.GAS_HANDLER).isPresent()
         || stack.getCapability(Capabilities.INFUSION_HANDLER).isPresent()
         || stack.getCapability(Capabilities.PIGMENT_HANDLER).isPresent()
         || stack.getCapability(Capabilities.SLURRY_HANDLER).isPresent();
   }

   public static HybridInventorySlot inputOrDrain(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
      Predicate<ItemStack> fluidInsertPredicate = FluidInventorySlot.getInputPredicate(mergedTank.getFluidTank());
      Predicate<ItemStack> gasInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(mergedTank.getGasTank(), GasInventorySlot::getCapability);
      Predicate<ItemStack> infusionInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(
         mergedTank.getInfusionTank(), InfusionInventorySlot::getCapability
      );
      Predicate<ItemStack> pigmentInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(
         mergedTank.getPigmentTank(), PigmentInventorySlot::getCapability
      );
      Predicate<ItemStack> slurryInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(mergedTank.getSlurryTank(), SlurryInventorySlot::getCapability);
      BiPredicate<ItemStack, AutomationType> insertPredicate = (stack, automationType) -> {
         return switch (mergedTank.getCurrentType()) {
            case FLUID -> fluidInsertPredicate.test(stack);
            case GAS -> gasInsertPredicate.test(stack);
            case INFUSION -> infusionInsertPredicate.test(stack);
            case PIGMENT -> pigmentInsertPredicate.test(stack);
            case SLURRY -> slurryInsertPredicate.test(stack);
            case EMPTY -> fluidInsertPredicate.test(stack)
               || gasInsertPredicate.test(stack)
               || infusionInsertPredicate.test(stack)
               || pigmentInsertPredicate.test(stack)
               || slurryInsertPredicate.test(stack);
         };
      };
      return new HybridInventorySlot(
         mergedTank,
         (stack, automationType) -> automationType == AutomationType.MANUAL || !insertPredicate.test(stack, automationType),
         insertPredicate,
         HybridInventorySlot::hasCapability,
         listener,
         x,
         y
      );
   }

   public static HybridInventorySlot outputOrFill(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
      Predicate<ItemStack> gasExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(mergedTank.getGasTank(), GasInventorySlot::getCapability);
      Predicate<ItemStack> infusionExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(
         mergedTank.getInfusionTank(), InfusionInventorySlot::getCapability
      );
      Predicate<ItemStack> pigmentExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(
         mergedTank.getPigmentTank(), PigmentInventorySlot::getCapability
      );
      Predicate<ItemStack> slurryExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(
         mergedTank.getSlurryTank(), SlurryInventorySlot::getCapability
      );
      Predicate<ItemStack> gasInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(mergedTank.getGasTank(), GasInventorySlot.getCapability(stack));
      Predicate<ItemStack> infusionInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(
         mergedTank.getInfusionTank(), InfusionInventorySlot.getCapability(stack)
      );
      Predicate<ItemStack> pigmentInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(
         mergedTank.getPigmentTank(), PigmentInventorySlot.getCapability(stack)
      );
      Predicate<ItemStack> slurryInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(
         mergedTank.getSlurryTank(), SlurryInventorySlot.getCapability(stack)
      );
      return new HybridInventorySlot(
         mergedTank,
         (stack, automationType) -> {
            if (automationType == AutomationType.MANUAL) {
               return true;
            } else {
               return switch (mergedTank.getCurrentType()) {
                  case FLUID -> true;
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
            return switch (mergedTank.getCurrentType()) {
               case FLUID -> automationType == AutomationType.INTERNAL;
               case GAS -> gasInsertPredicate.test(stack);
               case INFUSION -> infusionInsertPredicate.test(stack);
               case PIGMENT -> pigmentInsertPredicate.test(stack);
               case SLURRY -> slurryInsertPredicate.test(stack);
               case EMPTY -> automationType == AutomationType.INTERNAL && FluidUtil.getFluidHandler(stack).isPresent()
                  ? true
                  : gasInsertPredicate.test(stack)
                     || infusionInsertPredicate.test(stack)
                     || pigmentInsertPredicate.test(stack)
                     || slurryInsertPredicate.test(stack);
            };
         },
         HybridInventorySlot::hasCapability,
         listener,
         x,
         y
      );
   }

   private HybridInventorySlot(
      MergedTank mergedTank,
      BiPredicate<ItemStack, AutomationType> canExtract,
      BiPredicate<ItemStack, AutomationType> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(mergedTank, canExtract, canInsert, validator, listener, x, y);
   }

   @Override
   public IExtendedFluidTank getFluidTank() {
      return this.mergedTank.getFluidTank();
   }

   @Override
   public boolean isDraining() {
      return this.isDraining;
   }

   @Override
   public boolean isFilling() {
      return this.isFilling;
   }

   @Override
   public void setDraining(boolean draining) {
      this.isDraining = draining;
   }

   @Override
   public void setFilling(boolean filling) {
      this.isFilling = filling;
   }

   @NotNull
   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = super.serializeNBT();
      if (this.isDraining) {
         nbt.m_128379_("draining", true);
      }

      if (this.isFilling) {
         nbt.m_128379_("filling", true);
      }

      return nbt;
   }

   @Override
   public void deserializeNBT(@NotNull CompoundTag nbt) {
      this.isDraining = nbt.m_128471_("draining");
      this.isFilling = nbt.m_128471_("filling");
      super.deserializeNBT(nbt);
   }
}
