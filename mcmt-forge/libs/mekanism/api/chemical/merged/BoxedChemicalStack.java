package mekanism.api.chemical.merged;

import java.util.Objects;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoxedChemicalStack implements IHasTextComponent {
   public static final BoxedChemicalStack EMPTY = new BoxedChemicalStack(ChemicalType.GAS, GasStack.EMPTY);
   private final ChemicalType chemicalType;
   private final ChemicalStack<?> chemicalStack;

   public static BoxedChemicalStack box(ChemicalStack<?> chemicalStack) {
      return chemicalStack.isEmpty() ? EMPTY : new BoxedChemicalStack(ChemicalType.getTypeFor(chemicalStack), chemicalStack);
   }

   public static BoxedChemicalStack read(@Nullable CompoundTag nbt) {
      ChemicalType chemicalType = ChemicalType.fromNBT(nbt);
      if (chemicalType == null) {
         return EMPTY;
      } else {
         return new BoxedChemicalStack(chemicalType, (ChemicalStack<?>)(switch (chemicalType) {
            case GAS -> GasStack.readFromNBT(nbt);
            case INFUSION -> InfusionStack.readFromNBT(nbt);
            case PIGMENT -> PigmentStack.readFromNBT(nbt);
            case SLURRY -> SlurryStack.readFromNBT(nbt);
         }));
      }
   }

   private BoxedChemicalStack(ChemicalType chemicalType, ChemicalStack<?> chemicalStack) {
      this.chemicalType = chemicalType;
      this.chemicalStack = chemicalStack;
   }

   public BoxedChemical getType() {
      return this.isEmpty() ? BoxedChemical.EMPTY : new BoxedChemical(this.chemicalType, this.chemicalStack.getType());
   }

   public ChemicalType getChemicalType() {
      return this.chemicalType;
   }

   public boolean isEmpty() {
      return this == EMPTY || this.chemicalStack.isEmpty();
   }

   public CompoundTag write(CompoundTag nbt) {
      this.chemicalType.write(nbt);
      this.chemicalStack.write(nbt);
      return nbt;
   }

   public ChemicalStack<?> getChemicalStack() {
      return this.chemicalStack;
   }

   @NotNull
   @Override
   public Component getTextComponent() {
      return this.chemicalStack.getTextComponent();
   }

   public BoxedChemicalStack copy() {
      return new BoxedChemicalStack(this.chemicalType, this.chemicalStack.copy());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BoxedChemicalStack other = (BoxedChemicalStack)o;
         return this.chemicalType == other.chemicalType && this.chemicalStack.equals(other.chemicalStack);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.chemicalType, this.chemicalStack);
   }
}
