package mekanism.api.math;

import java.util.function.Supplier;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface FloatingLongSupplier extends Supplier<FloatingLong>, NonNullSupplier<FloatingLong> {
   @NotNull
   FloatingLong get();
}
