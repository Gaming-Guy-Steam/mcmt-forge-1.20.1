package mekanism.api.math;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface FloatingLongConsumer extends Consumer<FloatingLong> {
   void accept(@NotNull FloatingLong var1);
}
