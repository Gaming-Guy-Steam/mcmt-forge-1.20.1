package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CompressingIRecipe;
import mekanism.common.recipe.impl.InjectingIRecipe;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import mekanism.common.recipe.impl.PaintingIRecipe;
import mekanism.common.recipe.impl.PurifyingIRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ItemStackChemicalToItemStack")
public abstract class ItemStackChemicalToItemStackRecipeManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
   extends MekanismRecipeManager<RECIPE> {
   protected ItemStackChemicalToItemStackRecipeManager(IMekanismRecipeTypeProvider<RECIPE, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient itemInput, INGREDIENT chemicalInput, IItemStack output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), itemInput, chemicalInput, output));
   }

   public final RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, IItemStack output) {
      return this.makeRecipe(id, itemInput, chemicalInput, this.getAndValidateNotEmpty(output));
   }

   protected abstract RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output);

   protected String describeOutputs(RECIPE recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackChemicalToItemStack.Injecting")
   public static class ChemicalInjectionRecipeManager
      extends ItemStackChemicalToItemStackRecipeManager<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ItemStackGasToItemStackRecipe> {
      public static final ItemStackChemicalToItemStackRecipeManager.ChemicalInjectionRecipeManager INSTANCE = new ItemStackChemicalToItemStackRecipeManager.ChemicalInjectionRecipeManager();

      private ChemicalInjectionRecipeManager() {
         super(MekanismRecipeType.INJECTING);
      }

      protected ItemStackGasToItemStackRecipe makeRecipe(
         ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output
      ) {
         return new InjectingIRecipe(id, itemInput, gasInput, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackChemicalToItemStack.MetallurgicInfusing")
   public static class MetallurgicInfuserRecipeManager
      extends ItemStackChemicalToItemStackRecipeManager<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient, MetallurgicInfuserRecipe> {
      public static final ItemStackChemicalToItemStackRecipeManager.MetallurgicInfuserRecipeManager INSTANCE = new ItemStackChemicalToItemStackRecipeManager.MetallurgicInfuserRecipeManager();

      private MetallurgicInfuserRecipeManager() {
         super(MekanismRecipeType.METALLURGIC_INFUSING);
      }

      protected MetallurgicInfuserRecipe makeRecipe(
         ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.InfusionStackIngredient infusionInput, ItemStack output
      ) {
         return new MetallurgicInfuserIRecipe(id, itemInput, infusionInput, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackChemicalToItemStack.Compressing")
   public static class OsmiumCompressorRecipeManager
      extends ItemStackChemicalToItemStackRecipeManager<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ItemStackGasToItemStackRecipe> {
      public static final ItemStackChemicalToItemStackRecipeManager.OsmiumCompressorRecipeManager INSTANCE = new ItemStackChemicalToItemStackRecipeManager.OsmiumCompressorRecipeManager();

      private OsmiumCompressorRecipeManager() {
         super(MekanismRecipeType.COMPRESSING);
      }

      protected ItemStackGasToItemStackRecipe makeRecipe(
         ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output
      ) {
         return new CompressingIRecipe(id, itemInput, gasInput, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackChemicalToItemStack.Painting")
   public static class PaintingRecipeManager
      extends ItemStackChemicalToItemStackRecipeManager<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, PaintingRecipe> {
      public static final ItemStackChemicalToItemStackRecipeManager.PaintingRecipeManager INSTANCE = new ItemStackChemicalToItemStackRecipeManager.PaintingRecipeManager();

      private PaintingRecipeManager() {
         super(MekanismRecipeType.PAINTING);
      }

      protected PaintingRecipe makeRecipe(
         ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.PigmentStackIngredient pigmentInput, ItemStack output
      ) {
         return new PaintingIRecipe(id, itemInput, pigmentInput, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackChemicalToItemStack.Purifying")
   public static class PurificationRecipeManager
      extends ItemStackChemicalToItemStackRecipeManager<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ItemStackGasToItemStackRecipe> {
      public static final ItemStackChemicalToItemStackRecipeManager.PurificationRecipeManager INSTANCE = new ItemStackChemicalToItemStackRecipeManager.PurificationRecipeManager();

      private PurificationRecipeManager() {
         super(MekanismRecipeType.PURIFYING);
      }

      protected ItemStackGasToItemStackRecipe makeRecipe(
         ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output
      ) {
         return new PurifyingIRecipe(id, itemInput, gasInput, output);
      }
   }
}
