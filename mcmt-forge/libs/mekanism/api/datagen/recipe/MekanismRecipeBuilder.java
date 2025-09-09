package mekanism.api.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class MekanismRecipeBuilder<BUILDER extends MekanismRecipeBuilder<BUILDER>> {
   protected final List<ICondition> conditions = new ArrayList<>();
   protected final Builder advancementBuilder = Builder.m_138353_();
   protected final ResourceLocation serializerName;

   protected static ResourceLocation mekSerializer(String name) {
      return new ResourceLocation("mekanism", name);
   }

   protected MekanismRecipeBuilder(ResourceLocation serializerName) {
      this.serializerName = serializerName;
   }

   public BUILDER addCriterion(RecipeCriterion criterion) {
      return this.addCriterion(criterion.name(), criterion.criterion());
   }

   public BUILDER addCriterion(String name, CriterionTriggerInstance criterion) {
      this.advancementBuilder.m_138386_(name, criterion);
      return (BUILDER)this;
   }

   public BUILDER addCondition(ICondition condition) {
      this.conditions.add(condition);
      return (BUILDER)this;
   }

   protected boolean hasCriteria() {
      return !this.advancementBuilder.m_138405_().isEmpty();
   }

   protected abstract MekanismRecipeBuilder<BUILDER>.RecipeResult getResult(ResourceLocation var1);

   protected void validate(ResourceLocation id) {
   }

   public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
      this.validate(id);
      if (this.hasCriteria()) {
         this.advancementBuilder
            .m_138396_(new ResourceLocation("recipes/root"))
            .m_138386_("has_the_recipe", RecipeUnlockedTrigger.m_63728_(id))
            .m_138354_(net.minecraft.advancements.AdvancementRewards.Builder.m_10009_(id))
            .m_138360_(RequirementsStrategy.f_15979_);
      }

      consumer.accept(this.getResult(id));
   }

   protected void build(Consumer<FinishedRecipe> consumer, ItemLike output) {
      ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(output.m_5456_());
      if (registryName == null) {
         throw new IllegalStateException("Could not retrieve registry name for output.");
      } else {
         this.build(consumer, registryName);
      }
   }

   protected abstract class RecipeResult implements FinishedRecipe {
      private final ResourceLocation id;

      public RecipeResult(ResourceLocation id) {
         this.id = id;
      }

      public JsonObject m_125966_() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("type", MekanismRecipeBuilder.this.serializerName.toString());
         if (!MekanismRecipeBuilder.this.conditions.isEmpty()) {
            JsonArray conditionsArray = new JsonArray();

            for (ICondition condition : MekanismRecipeBuilder.this.conditions) {
               conditionsArray.add(CraftingHelper.serialize(condition));
            }

            jsonObject.add("conditions", conditionsArray);
         }

         this.m_7917_(jsonObject);
         return jsonObject;
      }

      @NotNull
      public RecipeSerializer<?> m_6637_() {
         return (RecipeSerializer<?>)ForgeRegistries.RECIPE_SERIALIZERS.getValue(MekanismRecipeBuilder.this.serializerName);
      }

      @NotNull
      public ResourceLocation m_6445_() {
         return this.id;
      }

      @Nullable
      public JsonObject m_5860_() {
         return MekanismRecipeBuilder.this.hasCriteria() ? MekanismRecipeBuilder.this.advancementBuilder.m_138400_() : null;
      }

      @Nullable
      public ResourceLocation m_6448_() {
         return new ResourceLocation(this.id.m_135827_(), "recipes/" + this.id.m_135815_());
      }
   }
}
