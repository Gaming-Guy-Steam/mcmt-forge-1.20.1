package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.registries.MekanismFluids;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeRegistryHelper {
   private RecipeRegistryHelper() {
   }

   public static void registerCondensentrator(IRecipeRegistration registry) {
      List<RotaryRecipe> condensentratorRecipes = new ArrayList<>();
      List<RotaryRecipe> decondensentratorRecipes = new ArrayList<>();

      for (RotaryRecipe recipe : MekanismRecipeType.ROTARY.getRecipes(getWorld())) {
         if (recipe.hasGasToFluid()) {
            condensentratorRecipes.add(recipe);
         }

         if (recipe.hasFluidToGas()) {
            decondensentratorRecipes.add(recipe);
         }
      }

      register(registry, MekanismJEIRecipeType.CONDENSENTRATING, condensentratorRecipes);
      register(registry, MekanismJEIRecipeType.DECONDENSENTRATING, decondensentratorRecipes);
   }

   public static <RECIPE extends MekanismRecipe> void register(
      IRecipeRegistration registry, MekanismJEIRecipeType<RECIPE> recipeType, IMekanismRecipeTypeProvider<RECIPE, ?> type
   ) {
      register(registry, recipeType, type.getRecipes(getWorld()));
   }

   public static <RECIPE> void register(IRecipeRegistration registry, MekanismJEIRecipeType<RECIPE> recipeType, List<RECIPE> recipes) {
      registry.addRecipes(MekanismJEI.recipeType(recipeType), recipes);
   }

   public static void registerNutritionalLiquifier(IRecipeRegistration registry) {
      List<ItemStackToFluidRecipe> list = new ArrayList<>();

      for (Item item : ForgeRegistries.ITEMS.getValues()) {
         if (item.m_41472_()) {
            ItemStack stack = new ItemStack(item);
            FoodProperties food = stack.getFoodProperties(null);
            if (food != null && food.m_38744_() > 0) {
               list.add(
                  new NutritionalLiquifierIRecipe(
                     item, IngredientCreatorAccess.item().from(stack), MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(food.m_38744_() * 50)
                  )
               );
            }
         }
      }

      register(registry, MekanismJEIRecipeType.NUTRITIONAL_LIQUIFICATION, list);
   }

   public static void addAnvilRecipes(IRecipeRegistration registry, IItemProvider item, Function<Item, ItemStack[]> repairMaterials) {
      IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();
      ItemStack damaged2 = item.getItemStack();
      damaged2.m_41721_(damaged2.m_41776_() * 3 / 4);
      ItemStack damaged3 = item.getItemStack();
      damaged3.m_41721_(damaged3.m_41776_() * 2 / 4);
      registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(damaged2, List.of(damaged2), List.of(damaged3))));
      ItemStack[] repairStacks = repairMaterials.apply(item.m_5456_());
      if (repairStacks != null && repairStacks.length > 0) {
         ItemStack damaged1 = item.getItemStack();
         damaged1.m_41721_(damaged1.m_41776_());
         registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(damaged1, List.of(repairStacks), List.of(damaged2))));
      }
   }

   private static ClientLevel getWorld() {
      return Minecraft.m_91087_().f_91073_;
   }
}
