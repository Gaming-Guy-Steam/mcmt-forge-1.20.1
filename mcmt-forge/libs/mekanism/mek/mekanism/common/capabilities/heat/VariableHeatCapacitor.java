package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class VariableHeatCapacitor extends BasicHeatCapacitor {
   private final DoubleSupplier conductionCoefficientSupplier;
   private final DoubleSupplier insulationCoefficientSupplier;

   public static VariableHeatCapacitor create(double heatCapacity, @Nullable DoubleSupplier ambientTempSupplier, @Nullable IContentsListener listener) {
      return create(heatCapacity, () -> 1.0, () -> 0.0, ambientTempSupplier, listener);
   }

   public static VariableHeatCapacitor create(
      double heatCapacity,
      DoubleSupplier conductionCoefficient,
      DoubleSupplier insulationCoefficient,
      @Nullable DoubleSupplier ambientTempSupplier,
      @Nullable IContentsListener listener
   ) {
      return new VariableHeatCapacitor(heatCapacity, conductionCoefficient, insulationCoefficient, ambientTempSupplier, listener);
   }

   protected VariableHeatCapacitor(
      double heatCapacity,
      DoubleSupplier conductionCoefficient,
      DoubleSupplier insulationCoefficient,
      @Nullable DoubleSupplier ambientTempSupplier,
      @Nullable IContentsListener listener
   ) {
      super(heatCapacity, conductionCoefficient.getAsDouble(), insulationCoefficient.getAsDouble(), ambientTempSupplier, listener);
      this.conductionCoefficientSupplier = conductionCoefficient;
      this.insulationCoefficientSupplier = insulationCoefficient;
   }

   @Override
   public double getInverseConduction() {
      return Math.max(1.0, this.conductionCoefficientSupplier.getAsDouble());
   }

   @Override
   public double getInverseInsulation() {
      return this.insulationCoefficientSupplier.getAsDouble();
   }
}
