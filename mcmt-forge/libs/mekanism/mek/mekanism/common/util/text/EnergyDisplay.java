package mekanism.common.util.text;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;

@NothingNullByDefault
public class EnergyDisplay implements IHasTextComponent {
   public static final EnergyDisplay ZERO = of(FloatingLong.ZERO);
   private final FloatingLong energy;
   private final FloatingLong max;

   private EnergyDisplay(FloatingLong energy, FloatingLong max) {
      this.energy = energy;
      this.max = max;
   }

   public static EnergyDisplay of(IEnergyContainer container) {
      return of(container.getEnergy(), container.getMaxEnergy());
   }

   public static EnergyDisplay of(FloatingLong energy, FloatingLong max) {
      return new EnergyDisplay(energy, max);
   }

   public static EnergyDisplay of(FloatingLong energy) {
      return of(energy, FloatingLong.ZERO);
   }

   @Override
   public Component getTextComponent() {
      if (this.energy.equals(FloatingLong.MAX_VALUE)) {
         return MekanismLang.INFINITE.translate(new Object[0]);
      } else {
         return (Component)(this.max.isZero()
            ? MekanismUtils.getEnergyDisplayShort(this.energy)
            : MekanismLang.GENERIC_FRACTION.translate(new Object[]{MekanismUtils.getEnergyDisplayShort(this.energy), of(this.max)}));
      }
   }
}
