package mekanism.common.config.value;

import mekanism.api.functions.ByteSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedByteValue extends CachedValue<Byte> implements ByteSupplier {
   private boolean resolved;
   private byte cachedValue;

   private CachedByteValue(IMekanismConfig config, ConfigValue<Byte> internal) {
      super(config, internal);
   }

   public static CachedByteValue wrap(IMekanismConfig config, ConfigValue<Byte> internal) {
      return new CachedByteValue(config, internal);
   }

   public byte getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Byte)this.internal.getDefault() : this.get();
   }

   public byte get() {
      if (!this.resolved) {
         this.cachedValue = (Byte)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   @Override
   public byte getAsByte() {
      return this.get();
   }

   public void set(byte value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         byte oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
