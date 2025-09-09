package mekanism.api.radial;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ClassBasedRadialData<MODE extends IRadialMode> extends RadialData<MODE> {
   protected final Class<MODE> clazz;

   protected ClassBasedRadialData(ResourceLocation identifier, Class<MODE> clazz) {
      super(identifier);
      this.clazz = Objects.requireNonNull(clazz, "Radial mode class type cannot be null.");
   }

   @Nullable
   @Override
   public INestedRadialMode fromIdentifier(ResourceLocation identifier) {
      return INestedRadialMode.class.isAssignableFrom(this.clazz) ? super.fromIdentifier(identifier) : null;
   }

   @Override
   public int tryGetNetworkRepresentation(IRadialMode mode) {
      return this.clazz.isInstance(mode) ? this.getNetworkRepresentation(this.clazz.cast(mode)) : 0;
   }

   @Override
   public boolean equals(@Nullable Object other) {
      if (other == this) {
         return true;
      } else {
         return other != null && this.getClass() == other.getClass() && super.equals(other) ? this.clazz == ((ClassBasedRadialData)other).clazz : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.clazz);
   }
}
