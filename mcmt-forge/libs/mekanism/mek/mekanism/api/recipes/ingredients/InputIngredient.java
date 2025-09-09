package mekanism.api.recipes.ingredients;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public interface InputIngredient<TYPE> extends Predicate<TYPE> {
   boolean testType(@NotNull TYPE var1);

   TYPE getMatchingInstance(TYPE var1);

   long getNeededAmount(TYPE var1);

   default boolean hasNoMatchingInstances() {
      return this.getRepresentations().isEmpty();
   }

   default void logMissingTags() {
   }

   List<TYPE> getRepresentations();

   void write(FriendlyByteBuf var1);

   JsonElement serialize();
}
