package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ItemStackToPigmentRecipeSerializer<RECIPE extends ItemStackToPigmentRecipe>
   extends ItemStackToChemicalRecipeSerializer<Pigment, PigmentStack, RECIPE> {
   public ItemStackToPigmentRecipeSerializer(ItemStackToChemicalRecipeSerializer.IFactory<Pigment, PigmentStack, RECIPE> factory) {
      super(factory);
   }

   protected PigmentStack fromJson(@NotNull JsonObject json, @NotNull String key) {
      return SerializerHelper.getPigmentStack(json, key);
   }

   protected PigmentStack fromBuffer(@NotNull FriendlyByteBuf buffer) {
      return PigmentStack.readFromPacket(buffer);
   }
}
