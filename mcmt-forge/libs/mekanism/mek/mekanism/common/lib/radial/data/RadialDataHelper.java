package mekanism.common.lib.radial.data;

import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class RadialDataHelper implements IRadialDataHelper {
   @Override
   public <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(ResourceLocation identifier, MODE defaultMode) {
      return (RadialData<MODE>)(defaultMode instanceof IDisableableEnum
         ? new DisableableEnumRadialData(identifier, defaultMode)
         : new EnumRadialData<>(identifier, defaultMode));
   }

   @Override
   public <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(ResourceLocation identifier, Class<MODE> enumClass) {
      return (RadialData<MODE>)(IDisableableEnum.class.isAssignableFrom(enumClass)
         ? new DisableableEnumRadialData(identifier, enumClass)
         : new EnumRadialData<>(identifier, enumClass));
   }

   @Override
   public <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForTruncated(ResourceLocation identifier, int accessibleValues, MODE defaultMode) {
      return new TruncatedEnumRadialData<>(identifier, accessibleValues, defaultMode);
   }

   @Override
   public RadialData<IRadialMode> booleanBasedData(ResourceLocation identifier, IRadialDataHelper.BooleanRadialModes modes, boolean defaultValue) {
      return new BooleanRadialData(identifier, modes, defaultValue);
   }
}
