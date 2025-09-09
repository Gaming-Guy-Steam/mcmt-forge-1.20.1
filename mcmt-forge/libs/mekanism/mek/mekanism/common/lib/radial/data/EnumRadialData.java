package mekanism.common.lib.radial.data;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.ClassBasedRadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnumRadialData<MODE extends Enum<MODE> & IRadialMode> extends ClassBasedRadialData<MODE> {
   private final List<MODE> modes = List.of(this.clazz.getEnumConstants());
   @Nullable
   private final MODE defaultMode;

   EnumRadialData(ResourceLocation identifier, MODE defaultMode) {
      super(identifier, Objects.<MODE>requireNonNull(defaultMode, "Default mode cannot be null.").getDeclaringClass());
      this.defaultMode = defaultMode;
   }

   EnumRadialData(ResourceLocation identifier, Class<MODE> enumClass) {
      super(identifier, enumClass);
      this.defaultMode = this.modes.isEmpty() ? null : this.modes.get(0);
   }

   @Nullable
   public MODE getDefaultMode(List<MODE> modes) {
      return this.defaultMode;
   }

   @Override
   public List<MODE> getModes() {
      return this.modes;
   }

   public int index(List<MODE> modes, MODE mode) {
      return mode.ordinal();
   }

   public int getNetworkRepresentation(MODE mode) {
      return mode.ordinal();
   }

   public MODE fromNetworkRepresentation(int networkRepresentation) {
      return MathUtils.getByIndexMod(this.modes, networkRepresentation);
   }
}
