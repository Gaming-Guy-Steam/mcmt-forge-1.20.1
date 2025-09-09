package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyInventorySlot extends BasicInventorySlot {
   private final Supplier<Level> worldSupplier;
   private final IEnergyContainer energyContainer;

   private static FloatingLong getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
      ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findTypeBasedRecipe(world, itemStack);
      return foundRecipe == null ? FloatingLong.ZERO : foundRecipe.getOutput(itemStack);
   }

   public static EnergyInventorySlot fillOrConvert(
      IEnergyContainer energyContainer, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y
   ) {
      Objects.requireNonNull(energyContainer, "Energy container cannot be null");
      Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
      return new EnergyInventorySlot(
         energyContainer,
         worldSupplier,
         stack -> !fillInsertCheck(stack) && getPotentialConversion(worldSupplier.get(), stack).isZero(),
         stack -> fillInsertCheck(stack) ? true : !getPotentialConversion(worldSupplier.get(), stack).isZero(),
         stack -> EnergyCompatUtils.hasStrictEnergyHandler(stack) || !getPotentialConversion(worldSupplier.get(), stack).isZero(),
         listener,
         x,
         y
      );
   }

   public static EnergyInventorySlot fill(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(energyContainer, "Energy container cannot be null");
      return new EnergyInventorySlot(
         energyContainer, stack -> !fillInsertCheck(stack), EnergyInventorySlot::fillInsertCheck, EnergyCompatUtils::hasStrictEnergyHandler, listener, x, y
      );
   }

   public static EnergyInventorySlot drain(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(energyContainer, "Energy container cannot be null");
      Predicate<ItemStack> insertPredicate = stack -> {
         IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
         if (itemEnergyHandler == null) {
            return false;
         } else {
            FloatingLong storedEnergy = energyContainer.getEnergy();
            if (storedEnergy.isZero()) {
               for (int container = 0; container < itemEnergyHandler.getEnergyContainerCount(); container++) {
                  if (!itemEnergyHandler.getNeededEnergy(container).isZero()) {
                     return true;
                  }
               }

               return false;
            } else {
               return itemEnergyHandler.insertEnergy(storedEnergy, Action.SIMULATE).smallerThan(storedEnergy);
            }
         }
      };
      return new EnergyInventorySlot(energyContainer, insertPredicate.negate(), insertPredicate, EnergyCompatUtils::hasStrictEnergyHandler, listener, x, y);
   }

   private static boolean fillInsertCheck(ItemStack stack) {
      IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
      return itemEnergyHandler != null && !itemEnergyHandler.extractEnergy(FloatingLong.MAX_VALUE, Action.SIMULATE).isZero();
   }

   private EnergyInventorySlot(
      IEnergyContainer energyContainer,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      this(energyContainer, () -> null, canExtract, canInsert, validator, listener, x, y);
   }

   private EnergyInventorySlot(
      IEnergyContainer energyContainer,
      Supplier<Level> worldSupplier,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(canExtract, canInsert, validator, listener, x, y);
      this.energyContainer = energyContainer;
      this.worldSupplier = worldSupplier;
      this.setSlotType(ContainerSlotType.POWER);
      this.setSlotOverlay(SlotOverlay.POWER);
   }

   public void fillContainerOrConvert() {
      if (!this.isEmpty() && !this.energyContainer.getNeeded().isZero() && !this.fillContainerFromItem()) {
         ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findFirstRecipe(this.worldSupplier.get(), this.current);
         if (foundRecipe != null) {
            ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(this.current);
            if (!itemInput.m_41619_()) {
               FloatingLong output = foundRecipe.getOutput(itemInput);
               if (!output.isZero() && this.energyContainer.insert(output, Action.SIMULATE, AutomationType.MANUAL).isZero()) {
                  MekanismUtils.logExpectedZero(this.energyContainer.insert(output, Action.EXECUTE, AutomationType.MANUAL));
                  int amountUsed = itemInput.m_41613_();
                  MekanismUtils.logMismatchedStackSize(this.shrinkStack(amountUsed, Action.EXECUTE), amountUsed);
               }
            }
         }
      }
   }

   public void fillContainer() {
      if (!this.isEmpty() && !this.energyContainer.getNeeded().isZero()) {
         this.fillContainerFromItem();
      }
   }

   private boolean fillContainerFromItem() {
      IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(this.current);
      if (itemEnergyHandler != null) {
         boolean didTransfer = false;

         for (int container = 0; container < itemEnergyHandler.getEnergyContainerCount(); container++) {
            FloatingLong energyInItem = itemEnergyHandler.getEnergy(container);
            if (!energyInItem.isZero()) {
               FloatingLong simulatedRemainder = this.energyContainer.insert(energyInItem, Action.SIMULATE, AutomationType.INTERNAL);
               if (simulatedRemainder.smallerThan(energyInItem)) {
                  FloatingLong extractedEnergy = itemEnergyHandler.extractEnergy(container, energyInItem.subtract(simulatedRemainder), Action.EXECUTE);
                  if (!extractedEnergy.isZero()) {
                     MekanismUtils.logExpectedZero(this.energyContainer.insert(extractedEnergy, Action.EXECUTE, AutomationType.INTERNAL));
                     didTransfer = true;
                     if (this.energyContainer.getNeeded().isZero()) {
                        break;
                     }
                  }
               }
            }
         }

         if (didTransfer) {
            this.onContentsChanged();
            return true;
         }
      }

      return false;
   }

   public void drainContainer() {
      if (!this.isEmpty() && !this.energyContainer.isEmpty()) {
         IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(this.current);
         if (itemEnergyHandler != null) {
            FloatingLong storedEnergy = this.energyContainer.getEnergy();
            FloatingLong simulatedRemainder = itemEnergyHandler.insertEnergy(storedEnergy, Action.SIMULATE);
            if (simulatedRemainder.smallerThan(storedEnergy)) {
               FloatingLong extractedEnergy = this.energyContainer.extract(storedEnergy.subtract(simulatedRemainder), Action.EXECUTE, AutomationType.INTERNAL);
               if (!extractedEnergy.isZero()) {
                  MekanismUtils.logExpectedZero(itemEnergyHandler.insertEnergy(extractedEnergy, Action.EXECUTE));
                  this.onContentsChanged();
               }
            }
         }
      }
   }
}
