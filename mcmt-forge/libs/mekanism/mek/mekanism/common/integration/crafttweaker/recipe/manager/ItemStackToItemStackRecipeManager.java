package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CrushingIRecipe;
import mekanism.common.recipe.impl.EnrichingIRecipe;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ItemStackToItemStack")
public abstract class ItemStackToItemStackRecipeManager extends MekanismRecipeManager<ItemStackToItemStackRecipe> {
   protected ItemStackToItemStackRecipeManager(IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient input, IItemStack output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output));
   }

   public final ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, IItemStack output) {
      return this.makeRecipe(id, input, this.getAndValidateNotEmpty(output));
   }

   protected abstract ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output);

   protected String describeOutputs(ItemStackToItemStackRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToItemStack.Crushing")
   public static class CrusherRecipeManager extends ItemStackToItemStackRecipeManager {
      public static final ItemStackToItemStackRecipeManager.CrusherRecipeManager INSTANCE = new ItemStackToItemStackRecipeManager.CrusherRecipeManager();

      private CrusherRecipeManager() {
         super(MekanismRecipeType.CRUSHING);
      }

      @Override
      protected ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
         return new CrushingIRecipe(id, input, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToItemStack.Smelting")
   public static class EnergizedSmelterRecipeManager extends ItemStackToItemStackRecipeManager {
      public static final ItemStackToItemStackRecipeManager.EnergizedSmelterRecipeManager INSTANCE = new ItemStackToItemStackRecipeManager.EnergizedSmelterRecipeManager();

      private EnergizedSmelterRecipeManager() {
         super(MekanismRecipeType.SMELTING);
      }

      @Override
      protected ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
         return new SmeltingIRecipe(id, input, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToItemStack.Enriching")
   public static class EnrichmentChamberRecipeManager extends ItemStackToItemStackRecipeManager {
      public static final ItemStackToItemStackRecipeManager.EnrichmentChamberRecipeManager INSTANCE = new ItemStackToItemStackRecipeManager.EnrichmentChamberRecipeManager();

      private EnrichmentChamberRecipeManager() {
         super(MekanismRecipeType.ENRICHING);
      }

      @Override
      protected ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
         return new EnrichingIRecipe(id, input, output);
      }
   }
}
