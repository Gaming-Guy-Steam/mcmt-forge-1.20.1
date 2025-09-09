package mekanism.api.radial;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class RadialData<MODE extends IRadialMode> {
   private final ResourceLocation identifier;

   protected RadialData(ResourceLocation identifier) {
      this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null.");
   }

   public final ResourceLocation getIdentifier() {
      return this.identifier;
   }

   @Nullable
   public INestedRadialMode fromIdentifier(ResourceLocation identifier) {
      for (MODE mode : this.getModes()) {
         if (mode instanceof INestedRadialMode nested && nested.hasNestedData() && identifier.equals(nested.nestedData().getIdentifier())) {
            return nested;
         }
      }

      return null;
   }

   public abstract List<MODE> getModes();

   @Nullable
   public MODE getDefaultMode(List<MODE> modes) {
      return null;
   }

   public int index(List<MODE> modes, MODE mode) {
      return modes.indexOf(mode);
   }

   public final int indexNullable(List<MODE> modes, @Nullable MODE mode) {
      return mode == null ? -1 : this.index(modes, mode);
   }

   public int tryGetNetworkRepresentation(IRadialMode mode) {
      return -1;
   }

   public int getNetworkRepresentation(MODE mode) {
      return -1;
   }

   @Nullable
   public MODE fromNetworkRepresentation(int networkRepresentation) {
      return null;
   }

   @Override
   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else {
         return other != null && this.getClass() == other.getClass() ? this.identifier.equals(((RadialData)other).identifier) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.identifier);
   }
}
