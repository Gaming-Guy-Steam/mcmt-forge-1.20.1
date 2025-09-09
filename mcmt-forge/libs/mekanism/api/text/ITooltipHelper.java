package mekanism.api.text;

import java.util.ServiceLoader;
import mekanism.api.math.FloatingLong;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ITooltipHelper {
   ITooltipHelper INSTANCE = ServiceLoader.load(ITooltipHelper.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for ITooltipHelper found"));

   Component getEnergyPerMBDisplayShort(FloatingLong var1);

   Component getRadioactivityDisplayShort(double var1);

   String getFormattedNumber(long var1);

   Component getPercent(double var1);
}
