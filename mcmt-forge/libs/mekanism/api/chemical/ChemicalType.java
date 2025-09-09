package mekanism.api.chemical;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.function.Predicate;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ChemicalType implements StringRepresentable {
   GAS("gas", c -> c instanceof Gas),
   INFUSION("infuse_type", c -> c instanceof InfuseType),
   PIGMENT("pigment", c -> c instanceof Pigment),
   SLURRY("slurry", c -> c instanceof Slurry);

   private static final Map<String, ChemicalType> nameToType;
   private final Predicate<Chemical<?>> instanceCheck;
   private final String name;

   private ChemicalType(String name, Predicate<Chemical<?>> instanceCheck) {
      this.name = name;
      this.instanceCheck = instanceCheck;
   }

   @NotNull
   public String m_7912_() {
      return this.name;
   }

   public boolean isInstance(Chemical<?> chemical) {
      return this.instanceCheck.test(chemical);
   }

   public void write(@NotNull CompoundTag nbt) {
      nbt.m_128359_("chemicalType", this.m_7912_());
   }

   @Nullable
   public static ChemicalType fromString(String name) {
      return nameToType.get(name);
   }

   @Nullable
   public static ChemicalType fromNBT(@Nullable CompoundTag nbt) {
      return nbt != null && nbt.m_128425_("chemicalType", 8) ? fromString(nbt.m_128461_("chemicalType")) : null;
   }

   public static ChemicalType getTypeFor(Chemical<?> chemical) {
      if (chemical instanceof Gas) {
         return GAS;
      } else if (chemical instanceof InfuseType) {
         return INFUSION;
      } else if (chemical instanceof Pigment) {
         return PIGMENT;
      } else if (chemical instanceof Slurry) {
         return SLURRY;
      } else {
         throw new IllegalStateException("Unknown chemical type");
      }
   }

   public static ChemicalType getTypeFor(ChemicalStack<?> stack) {
      return getTypeFor(stack.getType());
   }

   public static ChemicalType getTypeFor(ChemicalStackIngredient<?, ?> ingredient) {
      if (ingredient instanceof ChemicalStackIngredient.GasStackIngredient) {
         return GAS;
      } else if (ingredient instanceof ChemicalStackIngredient.InfusionStackIngredient) {
         return INFUSION;
      } else if (ingredient instanceof ChemicalStackIngredient.PigmentStackIngredient) {
         return PIGMENT;
      } else if (ingredient instanceof ChemicalStackIngredient.SlurryStackIngredient) {
         return SLURRY;
      } else {
         throw new IllegalStateException("Unknown chemical ingredient type");
      }
   }

   static {
      ChemicalType[] values = values();
      nameToType = new Object2ObjectArrayMap(values.length);

      for (ChemicalType type : values) {
         nameToType.put(type.m_7912_(), type);
      }
   }
}
