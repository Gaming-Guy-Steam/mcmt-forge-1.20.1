package mekanism.api.chemical.merged;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BoxedChemical implements IHasTextComponent {
   public static final BoxedChemical EMPTY = new BoxedChemical(ChemicalType.GAS, MekanismAPI.EMPTY_GAS);
   private final ChemicalType chemicalType;
   private final Chemical<?> chemical;

   public static BoxedChemical box(Chemical<?> chemical) {
      return chemical.isEmptyType() ? EMPTY : new BoxedChemical(ChemicalType.getTypeFor(chemical), chemical);
   }

   public static BoxedChemical read(FriendlyByteBuf buffer) {
      ChemicalType chemicalType = (ChemicalType)buffer.m_130066_(ChemicalType.class);

      return new BoxedChemical(chemicalType, (Chemical<?>)(switch (chemicalType) {
         case GAS -> (Gas)buffer.readRegistryIdSafe(Gas.class);
         case INFUSION -> (InfuseType)buffer.readRegistryIdSafe(InfuseType.class);
         case PIGMENT -> (Pigment)buffer.readRegistryIdSafe(Pigment.class);
         case SLURRY -> (Slurry)buffer.readRegistryIdSafe(Slurry.class);
      }));
   }

   public static BoxedChemical read(@Nullable CompoundTag nbt) {
      ChemicalType chemicalType = ChemicalType.fromNBT(nbt);
      if (chemicalType == null) {
         return EMPTY;
      } else {
         return new BoxedChemical(chemicalType, (Chemical<?>)(switch (chemicalType) {
            case GAS -> Gas.readFromNBT(nbt);
            case INFUSION -> InfuseType.readFromNBT(nbt);
            case PIGMENT -> Pigment.readFromNBT(nbt);
            case SLURRY -> Slurry.readFromNBT(nbt);
         }));
      }
   }

   protected BoxedChemical(ChemicalType chemicalType, Chemical<?> chemical) {
      this.chemicalType = chemicalType;
      this.chemical = chemical;
   }

   public boolean isEmpty() {
      return this == EMPTY || this.chemical.isEmptyType();
   }

   public ChemicalType getChemicalType() {
      return this.chemicalType;
   }

   public CompoundTag write(CompoundTag nbt) {
      this.chemicalType.write(nbt);
      this.chemical.write(nbt);
      return nbt;
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.chemicalType);
      switch (this.chemicalType) {
         case GAS:
            buffer.writeRegistryId(MekanismAPI.gasRegistry(), this.chemical);
            break;
         case INFUSION:
            buffer.writeRegistryId(MekanismAPI.infuseTypeRegistry(), this.chemical);
            break;
         case PIGMENT:
            buffer.writeRegistryId(MekanismAPI.pigmentRegistry(), this.chemical);
            break;
         case SLURRY:
            buffer.writeRegistryId(MekanismAPI.slurryRegistry(), this.chemical);
      }
   }

   public Chemical<?> getChemical() {
      return this.chemical;
   }

   @Override
   public Component getTextComponent() {
      return this.chemical.getTextComponent();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BoxedChemical other = (BoxedChemical)o;
         return this.chemicalType == other.chemicalType && this.chemical == other.chemical;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.chemicalType, this.chemical);
   }
}
