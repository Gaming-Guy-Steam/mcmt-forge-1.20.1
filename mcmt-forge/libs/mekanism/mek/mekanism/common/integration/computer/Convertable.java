package mekanism.common.integration.computer;

import java.util.function.BiFunction;
import mekanism.api.chemical.ChemicalStack;
import net.minecraftforge.fluids.FluidStack;

public final class Convertable<RAW> {
   private final RAW value;
   private final BiFunction<BaseComputerHelper, RAW, Object> converter;

   private Convertable(RAW value, BiFunction<BaseComputerHelper, RAW, Object> converter) {
      this.value = value;
      this.converter = converter;
   }

   public Object convert(BaseComputerHelper helper) {
      return this.converter.apply(helper, this.value);
   }

   public static <RAW> Convertable<RAW> of(RAW value, BiFunction<BaseComputerHelper, RAW, Object> converter) {
      return new Convertable<>(value, converter);
   }

   public static Convertable<FluidStack> of(FluidStack value) {
      return of(value, BaseComputerHelper::convert);
   }

   public static Convertable<ChemicalStack<?>> of(ChemicalStack<?> value) {
      return of(value, BaseComputerHelper::convert);
   }
}
