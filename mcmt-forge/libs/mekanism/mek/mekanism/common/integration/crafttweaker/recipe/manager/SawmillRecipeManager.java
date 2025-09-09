package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import com.blamejared.crafttweaker.api.util.random.Percentaged;
import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.SawmillIRecipe;
import mekanism.common.util.text.TextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Sawing")
public class SawmillRecipeManager extends MekanismRecipeManager<SawmillRecipe> {
   public static final SawmillRecipeManager INSTANCE = new SawmillRecipeManager();

   private SawmillRecipeManager() {
      super(MekanismRecipeType.SAWING);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient input, Percentaged<IItemStack> output) {
      this.addRecipe(name, input, (IItemStack)output.getData(), output.getPercentage());
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient input, IItemStack output, double chance) {
      if (chance < 1.0) {
         this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output, chance));
      } else if (chance == 1.0) {
         this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output));
      } else {
         if (!(chance < 2.0)) {
            throw new IllegalArgumentException("This sawing recipe should just have the amount increased or explicitly use the two output method.");
         }

         this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output, output.copy(), chance - 1.0));
      }
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient input, IItemStack mainOutput, Percentaged<IItemStack> secondaryOutput) {
      this.addRecipe(name, input, mainOutput, (IItemStack)secondaryOutput.getData(), secondaryOutput.getPercentage());
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient input, IItemStack mainOutput, IItemStack secondaryOutput, double secondaryChance) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, mainOutput, secondaryOutput, secondaryChance));
   }

   public final SawmillRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, IItemStack mainOutput) {
      return new SawmillIRecipe(id, input, this.getAndValidateNotEmpty(mainOutput), ItemStack.f_41583_, 0.0);
   }

   public final SawmillRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, IItemStack secondaryOutput, double secondaryChance) {
      return new SawmillIRecipe(
         id, input, ItemStack.f_41583_, this.getAndValidateNotEmpty(secondaryOutput), this.getAndValidateSecondaryChance(secondaryChance)
      );
   }

   public final SawmillRecipe makeRecipe(
      ResourceLocation id, ItemStackIngredient input, IItemStack mainOutput, IItemStack secondaryOutput, double secondaryChance
   ) {
      return new SawmillIRecipe(
         id, input, this.getAndValidateNotEmpty(mainOutput), this.getAndValidateNotEmpty(secondaryOutput), this.getAndValidateSecondaryChance(secondaryChance)
      );
   }

   private double getAndValidateSecondaryChance(double secondaryChance) {
      if (!(secondaryChance <= 0.0) && !(secondaryChance > 1.0)) {
         return secondaryChance;
      } else {
         throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
      }
   }

   protected String describeOutputs(SawmillRecipe recipe) {
      StringBuilder builder = new StringBuilder();
      List<ItemStack> mainOutputs = recipe.getMainOutputDefinition();
      if (!mainOutputs.isEmpty()) {
         builder.append("main: ").append(CrTUtils.describeOutputs(mainOutputs, ItemStackUtil::getCommandString));
      }

      if (recipe.getSecondaryChance() > 0.0) {
         if (!mainOutputs.isEmpty()) {
            builder.append("; ");
         }

         if (recipe.getSecondaryChance() == 1.0) {
            builder.append("secondary: ");
         } else {
            builder.append("secondary with chance ").append(TextUtils.getPercent(recipe.getSecondaryChance())).append(": ");
         }

         builder.append(CrTUtils.describeOutputs(recipe.getSecondaryOutputDefinition(), ItemStackUtil::getCommandString));
      }

      return builder.toString();
   }
}
