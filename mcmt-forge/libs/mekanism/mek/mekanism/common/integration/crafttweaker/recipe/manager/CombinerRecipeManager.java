package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CombinerIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Combining")
public class CombinerRecipeManager extends MekanismRecipeManager<CombinerRecipe> {
   public static final CombinerRecipeManager INSTANCE = new CombinerRecipeManager();

   private CombinerRecipeManager() {
      super(MekanismRecipeType.COMBINING);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient mainInput, ItemStackIngredient extraInput, IItemStack output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), mainInput, extraInput, output));
   }

   public final CombinerRecipe makeRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, IItemStack output) {
      return new CombinerIRecipe(id, mainInput, extraInput, this.getAndValidateNotEmpty(output));
   }

   protected String describeOutputs(CombinerRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
   }
}
