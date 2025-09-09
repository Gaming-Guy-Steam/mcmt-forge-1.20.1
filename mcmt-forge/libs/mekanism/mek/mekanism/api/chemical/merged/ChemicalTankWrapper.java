package mekanism.api.chemical.merged;

import java.util.function.BooleanSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public abstract class ChemicalTankWrapper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalTank<CHEMICAL, STACK> {
   private final IChemicalTank<CHEMICAL, STACK> internal;
   private final BooleanSupplier insertCheck;
   private final MergedChemicalTank mergedTank;

   protected ChemicalTankWrapper(MergedChemicalTank mergedTank, IChemicalTank<CHEMICAL, STACK> internal, BooleanSupplier insertCheck) {
      this.mergedTank = mergedTank;
      this.internal = internal;
      this.insertCheck = insertCheck;
   }

   public MergedChemicalTank getMergedTank() {
      return this.mergedTank;
   }

   @Override
   public STACK getStack() {
      return this.internal.getStack();
   }

   @Override
   public void setStack(STACK stack) {
      this.internal.setStack(stack);
   }

   @Override
   public void setStackUnchecked(STACK stack) {
      this.internal.setStackUnchecked(stack);
   }

   @Override
   public STACK insert(STACK stack, Action action, AutomationType automationType) {
      return this.insertCheck.getAsBoolean() ? this.internal.insert(stack, action, automationType) : stack;
   }

   @Override
   public STACK extract(long amount, Action action, AutomationType automationType) {
      return this.internal.extract(amount, action, automationType);
   }

   @Override
   public long getCapacity() {
      return this.internal.getCapacity();
   }

   @Override
   public boolean isValid(STACK stack) {
      return this.internal.isValid(stack);
   }

   @Override
   public void onContentsChanged() {
      this.internal.onContentsChanged();
   }

   @Override
   public long setStackSize(long amount, Action action) {
      return this.internal.setStackSize(amount, action);
   }

   @Override
   public long growStack(long amount, Action action) {
      return this.internal.growStack(amount, action);
   }

   @Override
   public long shrinkStack(long amount, Action action) {
      return this.internal.shrinkStack(amount, action);
   }

   @Override
   public boolean isEmpty() {
      return this.internal.isEmpty();
   }

   @Override
   public void setEmpty() {
      this.internal.setEmpty();
   }

   @Override
   public long getStored() {
      return this.internal.getStored();
   }

   @Override
   public long getNeeded() {
      return this.internal.getNeeded();
   }

   @Override
   public CHEMICAL getType() {
      return this.internal.getType();
   }

   @Override
   public boolean isTypeEqual(STACK other) {
      return this.internal.isTypeEqual(other);
   }

   @Override
   public boolean isTypeEqual(CHEMICAL other) {
      return this.internal.isTypeEqual(other);
   }

   @Override
   public ChemicalAttributeValidator getAttributeValidator() {
      return this.internal.getAttributeValidator();
   }

   @Override
   public CompoundTag serializeNBT() {
      return this.internal.serializeNBT();
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.internal.deserializeNBT(nbt);
   }
}
