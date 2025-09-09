package mekanism.common.recipe.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import org.jetbrains.annotations.Nullable;

public record ConditionExistsCondition(@Nullable ICondition condition) implements ICondition {
   private static final ResourceLocation NAME = Mekanism.rl("condition_exists");
   private static final ConditionExistsCondition DOES_NOT_EXIST = new ConditionExistsCondition(null);

   public ResourceLocation getID() {
      return NAME;
   }

   public boolean test(IContext context) {
      return this.condition != null && this.condition.test(context);
   }

   @Override
   public String toString() {
      return "condition_exists(" + this.condition + ")";
   }

   public static class Serializer implements IConditionSerializer<ConditionExistsCondition> {
      public static final ConditionExistsCondition.Serializer INSTANCE = new ConditionExistsCondition.Serializer();

      private Serializer() {
      }

      public void write(JsonObject json, ConditionExistsCondition value) {
         if (value.condition != null) {
            json.add("condition", CraftingHelper.serialize(value.condition));
         }
      }

      public ConditionExistsCondition read(JsonObject json) {
         JsonObject condition = GsonHelper.m_13841_(json, "condition", null);
         if (condition == null) {
            return ConditionExistsCondition.DOES_NOT_EXIST;
         } else {
            try {
               return new ConditionExistsCondition(CraftingHelper.getCondition(condition));
            } catch (JsonParseException var4) {
               return ConditionExistsCondition.DOES_NOT_EXIST;
            }
         }
      }

      public ResourceLocation getID() {
         return ConditionExistsCondition.NAME;
      }
   }
}
