package mekanism.common.item.predicate;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class CustomItemPredicate extends ItemPredicate {
   protected CustomItemPredicate() {
   }

   protected abstract ResourceLocation getID();

   public abstract boolean m_45049_(@NotNull ItemStack stack);

   @NotNull
   public JsonObject serializeToJson() {
      JsonObject object = new JsonObject();
      object.addProperty("type", this.getID().toString());
      return object;
   }
}
