package mekanism.common.lib.radial.data;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BooleanRadialData extends RadialData<IRadialMode> {
   private final List<IRadialMode> modes;
   private final IRadialDataHelper.BooleanRadialModes rawModes;
   private final boolean defaultMode;

   BooleanRadialData(ResourceLocation identifier, IRadialDataHelper.BooleanRadialModes modes, boolean defaultMode) {
      super(identifier);
      this.rawModes = Objects.requireNonNull(modes, "Boolean modes cannot be null.");
      this.modes = List.of(this.rawModes.falseMode(), this.rawModes.trueMode());
      this.defaultMode = defaultMode;
   }

   @Nullable
   @Override
   public IRadialMode getDefaultMode(List<IRadialMode> modes) {
      return this.rawModes.get(this.defaultMode);
   }

   @Override
   public List<IRadialMode> getModes() {
      return this.modes;
   }

   @Override
   public int index(List<IRadialMode> modes, IRadialMode mode) {
      return this.getNetworkRepresentation(mode);
   }

   @Override
   public int tryGetNetworkRepresentation(IRadialMode mode) {
      return this.getNetworkRepresentation(mode);
   }

   @Override
   public int getNetworkRepresentation(IRadialMode mode) {
      if (mode.equals(this.rawModes.falseMode())) {
         return 0;
      } else {
         return mode.equals(this.rawModes.trueMode()) ? 1 : -1;
      }
   }

   @Nullable
   @Override
   public IRadialMode fromNetworkRepresentation(int networkRepresentation) {
      if (networkRepresentation == 0) {
         return this.rawModes.falseMode();
      } else {
         return networkRepresentation == 1 ? this.rawModes.trueMode() : null;
      }
   }

   @Override
   public boolean equals(@Nullable Object other) {
      if (other == this) {
         return true;
      } else {
         return other != null && this.getClass() == other.getClass() && super.equals(other) ? this.rawModes.equals(((BooleanRadialData)other).rawModes) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.rawModes);
   }
}
