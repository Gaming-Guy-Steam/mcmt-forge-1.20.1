package mekanism.api.gear.config;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public final class ModuleBooleanData implements ModuleConfigData<Boolean> {
   private boolean value;

   public ModuleBooleanData() {
      this(true);
   }

   public ModuleBooleanData(boolean def) {
      this.value = def;
   }

   public Boolean get() {
      return this.value;
   }

   public void set(Boolean val) {
      this.value = Objects.requireNonNull(val, "Value cannot be null.");
   }

   @Override
   public void read(String name, CompoundTag tag) {
      Objects.requireNonNull(tag, "Tag cannot be null.");
      Objects.requireNonNull(name, "Name cannot be null.");
      this.value = tag.m_128471_(name);
   }

   @Override
   public void write(String name, CompoundTag tag) {
      Objects.requireNonNull(tag, "Tag cannot be null.");
      Objects.requireNonNull(name, "Name cannot be null.");
      tag.m_128379_(name, this.value);
   }
}
