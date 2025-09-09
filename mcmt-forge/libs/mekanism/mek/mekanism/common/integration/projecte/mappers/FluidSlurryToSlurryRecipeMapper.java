package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

@RecipeTypeMapper
public class FluidSlurryToSlurryRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekFluidSlurryToSlurry";
   }

   public String getDescription() {
      return "Maps Mekanism washing recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.WASHING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof FluidSlurryToSlurryRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         List fluidRepresentations = recipe.getFluidInput().getRepresentations();
         List slurryRepresentations = recipe.getChemicalInput().getRepresentations();

         for (FluidStack fluidRepresentation : fluidRepresentations) {
            NormalizedSimpleStack nssFluid = NSSFluid.createFluid(fluidRepresentation);

            for (SlurryStack slurryRepresentation : slurryRepresentations) {
               SlurryStack output = recipe.getOutput(fluidRepresentation, slurryRepresentation);
               if (!output.isEmpty()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(nssFluid, fluidRepresentation.getAmount());
                  ingredientHelper.put(slurryRepresentation);
                  if (ingredientHelper.addAsConversion(output)) {
                     handled = true;
                  }
               }
            }
         }

         return handled;
      }
   }
}
