package mekanism.api.gear.config;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public final class ModuleEnumData<TYPE extends Enum<TYPE> & IHasTextComponent> implements ModuleConfigData<TYPE> {
   private final List<TYPE> enumConstants;
   private final TYPE defaultValue;
   private TYPE value;

   public ModuleEnumData(TYPE def) {
      this.value = this.defaultValue = Objects.requireNonNull(def, "Default value cannot be null.");
      this.enumConstants = List.of(this.defaultValue.getDeclaringClass().getEnumConstants());
   }

   public ModuleEnumData(TYPE def, int selectableCount) {
      this.value = this.defaultValue = Objects.requireNonNull(def, "Default value cannot be null.");
      if (selectableCount <= 0) {
         throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
      } else {
         Class<TYPE> enumClass = this.defaultValue.getDeclaringClass();
         TYPE[] constants = enumClass.getEnumConstants();
         if (constants.length < selectableCount) {
            throw new IllegalArgumentException("Selectable count is larger than the number of elements in " + enumClass.getSimpleName());
         } else {
            if (constants.length == selectableCount) {
               this.enumConstants = List.of(constants);
            } else {
               if (this.defaultValue.ordinal() >= selectableCount) {
                  throw new IllegalArgumentException("Invalid default, it is out of range of the selectable values.");
               }

               this.enumConstants = List.of(constants).subList(0, selectableCount);
            }
         }
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "10.3.2"
   )
   public ModuleEnumData(Class<TYPE> enumClass, TYPE def) {
      this(def);
      Objects.requireNonNull(enumClass, "Enum Class cannot be null.");
   }

   @Deprecated(
      forRemoval = true,
      since = "10.3.2"
   )
   public ModuleEnumData(Class<TYPE> enumClass, int selectableCount, TYPE def) {
      this(def, selectableCount);
      Objects.requireNonNull(enumClass, "Enum Class cannot be null.");
   }

   public List<TYPE> getEnums() {
      return this.enumConstants;
   }

   public TYPE get() {
      return this.value;
   }

   public void set(TYPE val) {
      Objects.requireNonNull(val, "Value cannot be null.");
      if (val.ordinal() >= this.enumConstants.size()) {
         throw new IllegalArgumentException("Invalid value, it is out of range of the selectable values.");
      } else {
         this.value = val;
      }
   }

   @Override
   public void read(String name, CompoundTag tag) {
      Objects.requireNonNull(tag, "Tag cannot be null.");
      Objects.requireNonNull(name, "Name cannot be null.");
      int ordinal = tag.m_128451_(name);
      if (ordinal >= 0 && ordinal < this.enumConstants.size()) {
         this.value = this.enumConstants.get(ordinal);
      } else {
         this.value = this.defaultValue;
      }
   }

   @Override
   public void write(String name, CompoundTag tag) {
      Objects.requireNonNull(tag, "Tag cannot be null.");
      Objects.requireNonNull(name, "Name cannot be null.");
      tag.m_128405_(name, this.value.ordinal());
   }
}
