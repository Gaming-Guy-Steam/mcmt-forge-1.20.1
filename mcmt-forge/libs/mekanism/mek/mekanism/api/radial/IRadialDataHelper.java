package mekanism.api.radial;

import java.util.Objects;
import java.util.ServiceLoader;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public interface IRadialDataHelper {
   IRadialDataHelper INSTANCE = ServiceLoader.load(IRadialDataHelper.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IRadialDataHelper found"));

   <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(ResourceLocation var1, MODE var2);

   <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(ResourceLocation var1, Class<MODE> var2);

   <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForTruncated(ResourceLocation var1, int var2, MODE var3);

   default RadialData<IRadialMode> booleanBasedData(ResourceLocation identifier, IRadialDataHelper.BooleanRadialModes modes) {
      return this.booleanBasedData(identifier, modes, false);
   }

   RadialData<IRadialMode> booleanBasedData(ResourceLocation var1, IRadialDataHelper.BooleanRadialModes var2, boolean var3);

   public record BooleanRadialModes(IRadialMode falseMode, IRadialMode trueMode) {
      public BooleanRadialModes(IRadialMode falseMode, IRadialMode trueMode) {
         Objects.requireNonNull(falseMode, "Radial mode representing 'false' cannot be null.");
         Objects.requireNonNull(trueMode, "Radial mode representing 'true' cannot be null.");
         this.falseMode = falseMode;
         this.trueMode = trueMode;
      }

      public IRadialMode get(boolean value) {
         return value ? this.trueMode : this.falseMode;
      }
   }
}
