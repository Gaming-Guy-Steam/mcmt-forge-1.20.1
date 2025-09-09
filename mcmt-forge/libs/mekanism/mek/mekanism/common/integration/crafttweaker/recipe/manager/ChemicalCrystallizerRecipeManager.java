package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalCrystallizerIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Crystallizing")
public class ChemicalCrystallizerRecipeManager extends MekanismRecipeManager<ChemicalCrystallizerRecipe> {
   public static final ChemicalCrystallizerRecipeManager INSTANCE = new ChemicalCrystallizerRecipeManager();

   private ChemicalCrystallizerRecipeManager() {
      super(MekanismRecipeType.CRYSTALLIZING);
   }

   @Method
   public void addRecipe(String name, ChemicalStackIngredient<?, ?> input, IItemStack output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output));
   }

   public final ChemicalCrystallizerRecipe makeRecipe(ResourceLocation id, ChemicalStackIngredient<?, ?> input, IItemStack output) {
      return new ChemicalCrystallizerIRecipe(id, input, this.getAndValidateNotEmpty(output));
   }

   protected String describeOutputs(ChemicalCrystallizerRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
   }
}
