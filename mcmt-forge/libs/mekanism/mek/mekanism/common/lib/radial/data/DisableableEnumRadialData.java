package mekanism.common.lib.radial.data;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.ClassBasedRadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DisableableEnumRadialData<MODE extends Enum<MODE> & IDisableableEnum<MODE> & IRadialMode> extends ClassBasedRadialData<MODE> {
   private final MODE[] modes = this.clazz.getEnumConstants();
   @Nullable
   private final MODE defaultMode;

   DisableableEnumRadialData(ResourceLocation identifier, MODE defaultMode) {
      super(identifier, Objects.<MODE>requireNonNull(defaultMode, "Default mode cannot be null.").getDeclaringClass());
      this.defaultMode = defaultMode;
   }

   DisableableEnumRadialData(ResourceLocation identifier, Class<MODE> enumClass) {
      super(identifier, enumClass);
      this.defaultMode = this.modes.length == 0 ? null : this.modes[0];
   }

   @Nullable
   public MODE getDefaultMode(List<MODE> modes) {
      return this.defaultMode;
   }

   @Override
   public List<MODE> getModes() {
      return Arrays.stream(this.modes).filter(rec$ -> rec$.isEnabled()).toList();
   }

   public int index(List<MODE> modes, MODE mode) {
      return modes.size() == this.modes.length ? mode.ordinal() : super.index(modes, mode);
   }

   public int getNetworkRepresentation(MODE mode) {
      return mode.isEnabled() ? mode.ordinal() : -1;
   }

   @Nullable
   public MODE fromNetworkRepresentation(int networkRepresentation) {
      MODE mode = MathUtils.getByIndexMod(this.modes, networkRepresentation);
      return mode.isEnabled() ? mode : this.defaultMode;
   }
}
