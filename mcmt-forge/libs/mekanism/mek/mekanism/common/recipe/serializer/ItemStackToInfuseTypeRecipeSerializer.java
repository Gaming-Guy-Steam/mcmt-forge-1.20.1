package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ItemStackToInfuseTypeRecipeSerializer<RECIPE extends ItemStackToInfuseTypeRecipe>
   extends ItemStackToChemicalRecipeSerializer<InfuseType, InfusionStack, RECIPE> {
   public ItemStackToInfuseTypeRecipeSerializer(ItemStackToChemicalRecipeSerializer.IFactory<InfuseType, InfusionStack, RECIPE> factory) {
      super(factory);
   }

   protected InfusionStack fromJson(@NotNull JsonObject json, @NotNull String key) {
      return SerializerHelper.getInfusionStack(json, key);
   }

   protected InfusionStack fromBuffer(@NotNull FriendlyByteBuf buffer) {
      return InfusionStack.readFromPacket(buffer);
   }
}
