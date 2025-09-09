package mekanism.api.gear.config;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public sealed class ModuleIntegerData implements ModuleConfigData<Integer> permits ModuleColorData {
   private int value;

   protected ModuleIntegerData(int def) {
      this.value = def;
   }

   protected int sanitizeValue(int value) {
      return value;
   }

   public Integer get() {
      return this.value;
   }

   public void set(Integer val) {
      Objects.requireNonNull(val, "Value cannot be null.");
      this.value = this.sanitizeValue(val);
   }

   @Override
   public void read(String name, CompoundTag tag) {
      Objects.requireNonNull(tag, "Tag cannot be null.");
      Objects.requireNonNull(name, "Name cannot be null.");
      this.value = this.sanitizeValue(tag.m_128451_(name));
   }

   @Override
   public void write(String name, CompoundTag tag) {
      Objects.requireNonNull(tag, "Tag cannot be null.");
      Objects.requireNonNull(name, "Name cannot be null.");
      tag.m_128405_(name, this.value);
   }
}
