package mekanism.api.recipes.ingredients.creator;

import com.google.gson.JsonElement;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IIngredientCreator<TYPE, STACK, INGREDIENT extends InputIngredient<STACK>> {
   INGREDIENT from(STACK var1);

   INGREDIENT from(TYPE var1, int var2);

   INGREDIENT from(TagKey<TYPE> var1, int var2);

   INGREDIENT read(FriendlyByteBuf var1);

   INGREDIENT deserialize(@Nullable JsonElement var1);

   INGREDIENT createMulti(INGREDIENT... var1);

   INGREDIENT from(Stream<INGREDIENT> var1);
}
