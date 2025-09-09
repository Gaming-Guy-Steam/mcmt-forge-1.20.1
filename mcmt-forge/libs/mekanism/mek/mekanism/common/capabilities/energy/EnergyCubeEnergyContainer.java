package mekanism.common.capabilities.energy;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.tier.EnergyCubeTier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyCubeEnergyContainer extends BasicEnergyContainer {
   private final boolean isCreative;
   private final FloatingLongSupplier rate;

   public static EnergyCubeEnergyContainer create(EnergyCubeTier tier, @Nullable IContentsListener listener) {
      Objects.requireNonNull(tier, "Energy cube tier cannot be null");
      return new EnergyCubeEnergyContainer(tier, listener);
   }

   private EnergyCubeEnergyContainer(EnergyCubeTier tier, @Nullable IContentsListener listener) {
      super(tier.getMaxEnergy(), alwaysTrue, alwaysTrue, listener);
      this.isCreative = tier == EnergyCubeTier.CREATIVE;
      this.rate = tier::getOutput;
   }

   @Override
   protected FloatingLong getRate(@Nullable AutomationType automationType) {
      return automationType == AutomationType.INTERNAL ? this.rate.get() : super.getRate(automationType);
   }

   @Override
   public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
      return super.insert(amount, action.combine(!this.isCreative), automationType);
   }

   @Override
   public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
      return super.extract(amount, action.combine(!this.isCreative), automationType);
   }
}
