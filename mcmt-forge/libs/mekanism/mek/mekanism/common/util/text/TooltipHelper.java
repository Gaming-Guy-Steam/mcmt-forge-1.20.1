package mekanism.common.util.text;

import mekanism.api.math.FloatingLong;
import mekanism.api.text.ITooltipHelper;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.network.chat.Component;

public class TooltipHelper implements ITooltipHelper {
   @Override
   public Component getEnergyPerMBDisplayShort(FloatingLong energy) {
      return MekanismLang.GENERIC_PER_MB.translate(new Object[]{MekanismUtils.getEnergyDisplayShort(energy)});
   }

   @Override
   public Component getRadioactivityDisplayShort(double radioactivity) {
      return UnitDisplayUtils.getDisplayShort(radioactivity, UnitDisplayUtils.RadiationUnit.SVH, 2);
   }

   @Override
   public String getFormattedNumber(long number) {
      return TextUtils.format(number);
   }

   @Override
   public Component getPercent(double ratio) {
      return TextUtils.getPercent(ratio);
   }
}
